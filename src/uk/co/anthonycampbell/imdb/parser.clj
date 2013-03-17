
; Copyright 2013 Anthony Campbell (anthonycampbell.co.uk)
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;      http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns uk.co.anthonycampbell.imdb.parser
    (:use uk.co.anthonycampbell.imdb.request)
    (:use clojure.tools.logging)
    (:use clj-logging-config.log4j)
    (:require [clj-http.client :as client])
    (:require [net.cgrand.enlive-html :as html])
    (:require [net.cgrand.xml :as xml])
    (:require [clojure.string])
    (:require [clojure.contrib.string :as ccstring]))

(defn parse-search-results
    "Selects the search result content from the query result"
    [page-content]
    (debug "Parse search results...")
    
    (html/select page-content
        [:html :body :div#wrapper :div#root :div#pagecontent :div#content-2-wide :div#main
         :div.findSection]))

(defn select-title-from-results
    "Attempts to select the first title from the search results"
    [page-content]
    
    (loop [section-title-link {}
           section-list (parse-search-results page-content)]
        (if (empty? section-title-link)
            (if (not-empty section-list)
                
                ; Recurrsively look for the title sub-section in the search result
                (recur
                    (let [section-h3-header (html/select (first section-list) [:h3.findSectionHeader])]
                        (debug "- h3:", section-h3-header)
                        
                        (if (not-empty section-h3-header)
                            ; Got to be careful has these headers can sometimes be links
                            (let [header-text (if (> (count (:content (first section-h3-header))) 1)
                                    (second (:content (first section-h3-header)))
                                    (first (:content (first section-h3-header))))]
                                (debug "- h3 text:", header-text)
                                
                                (if (not-empty header-text)
                                    (if (= header-text "Titles")
                                        (let [first-title-link
                                                (first (html/select (first section-list) [:td.result_text :a]))]
                                            (debug "----" first-title-link, "\n")
                                            
                                            ; Persit first link in this section
                                            (merge section-title-link first-title-link)))))))
                    (rest section-list)))
            section-title-link)))

(defn construct-title
    "Construct a title string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (let [h1-header (html/select page-content [:h1.header])]
            (debug "- Looking for title name...")
            
            (if (not-empty h1-header)
                (let [h1-header-text (ccstring/trim (first (:content (first h1-header))))]
                    (debug "----" h1-header-text)
                    h1-header-text)))))

(defn select-cast-crew-link
    "Attempts to select the 'see more case / crew' link from the provided list."
    [list]
    (if (not-empty list)
        (let [link (first list)]
            (if (not-empty link)
                
                ; Right anchor link?
                (if (= (first (:content link)) "Full cast and crew")
                    (let [match (ccstring/trim (:href (:attrs link)))]
                        ; Match!
                        (debug "----" match)
                        match)
                    
                    ; Next link
                    (select-cast-crew-link (rest list)))))))

(defn search-for-cast-url-within-page-content
    "Parse the provided page contents and looks for the link to the cast and crew page."
    [page-content]
    (if (not-empty page-content)
        (let [see-more-div (html/select page-content [:div.see-more])]
            (if (not-empty see-more-div)
                
                ; Find each link inside this DIV
                (let [see-more-links (html/select see-more-div [:a])]
                    
                    ; Attempt grab correct link
                    (select-cast-crew-link see-more-links))))))

(defn select-href-from-link
    "Attempts to determine the title's URL from the available 'canonical' head link"
    [head-links]
    (if (not-empty head-links)
        
        ; One link at a time
        (let [link (first head-links)]
            (if (not-empty link)
                
                ; Right header link?
                (let [link-rel (:rel (:attrs link))]
                    (if (not-empty link-rel)
                        (if (= link-rel "canonical")
                            (let [match (ccstring/trim (:href (:attrs link)))]
                                ; Match!
                                (debug "----" match)
                                match)
                            
                            ; Next link
                            (select-href-from-link (rest head-links)))))))))

(defn construct-href
    "Construct a href string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        ; Let's try and get href from 'canonical' link
        (let [head-links (html/select page-content [:head :link])]
            (debug "- Looking for title URL...")
            
            (if (not-empty head-links)
                (select-href-from-link head-links)))))

(defn construct-cast-href
    "Construct a href string for the provided page's cast summary"
    [page-content]
    (if (not-empty page-content)
        (let [title-href (construct-href page-content)]
            (debug "- Looking for cast URL...")
            
            (if (not-empty title-href)
                (str title-href (search-for-cast-url-within-page-content page-content))))))

(defn construct-classification
    "Construct a classification string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        ; Select title attribute from first infobar span element
        (let [infobar-span (html/select page-content [:div.infobar :span])]
            (debug "- Looking for classification...")
            
            (if (not-empty infobar-span)
                (let [classification (:title (:attrs (first infobar-span)))]
                    (debug "----" classification)
                    classification)))))

(defn select-genres-from-list
    "Attempts to select all of the span tags which contain a genre string"
    [span-list]
    (loop [genre-string ""
           item-list span-list]
        (if (not-empty item-list)
            ; Recurrsively look for span with genre attributes
            (recur
                (let [item (first item-list)]
                    (let [type (:itemprop (:attrs item))]
                        (if (= "genre" type)
                            (let [genre (first (:content item))]
                                (debug "----" genre)
                                
                                (str (str genre-string " ") genre))
                            genre-string)))
                (rest item-list))
            genre-string)))

(defn construct-genre
    "Construct a genre string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        
        ; Select all itemprop span tags
        (let [itemprops (html/select page-content [:span.itemprop])]
            (debug "- Looking for genres...")
            
            (if (not-empty itemprops)
                (let [genre-string (select-genres-from-list itemprops)]
                    (if (not-empty genre-string)
                        (ccstring/trim genre-string)))))))

(defn select-release-date-from-list
    "Attempts to select all of the tags which contain a datePublished attribute"
    [list]
    (loop [date-string ""
           item-list list]
        (if (not-empty item-list)
            ; Recurrsively look for meta tag with date published attributes
            (recur
                (let [item (first item-list)]
                    (let [link-title (:title (:attrs item))]
                        
                        (if (= "See all release dates" link-title)
                            (let [release-date (first (:content item))]
                                (debug "----" release-date)
                                
                                (str (str date-string " ") release-date))
                            date-string)))
                (rest item-list))
            date-string)))

(defn construct-release-date
    "Construct a release date string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        
        ; Select title attribute from first infobar span element
        (let [meta-list (html/select page-content [:span :a])]
            (debug "- Looking for release dates...")
            
            (if (not-empty meta-list)
                (let [release-date-string (select-release-date-from-list meta-list)]
                    (if (not-empty release-date-string)
                        (ccstring/trim release-date-string)))))))


(defn search-for-description-within-list
    "Searches through all of the provided paragraphs until we find the short
     description. The selects the first non-empty paragraph available."
    [list]
    (if (not-empty list)
        (let [paragraph-content (:content (first list))]
            (if (not-empty paragraph-content)
                (let [paragraph-content-text (ccstring/trim (first paragraph-content))]
                    (debug "----" paragraph-content-text)
                    paragraph-content-text)
                    
                    ; Next paragraph
                    (search-for-description-within-list (rest list))))))

(defn construct-description
    "Construct a description string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (let [paragraphs (html/select page-content [:p])]
            (debug "- Looking for description...")
            
            (search-for-description-within-list paragraphs))))

(defn search-for-long-description-within-list
    "Searches through all of the provided title DIV sub sections until we find the
     storyline section. The selects the first non-empty paragraph available."
    [list]
    (if (not-empty list)
        (let [h2 (html/select list [:h2])]
            (if (not-empty h2)
                ; Check whether this is the right article
                (if (= (first (:content (first h2))) "Storyline")
                    
                    ; Extract first paragraph
                    (let [paragraphs (html/select list [:p])]
                        (if (not-empty paragraphs)
                            (let [storyline (ccstring/trim (first (:content (first paragraphs))))]
                                (debug "----" storyline)
                                
                                storyline)))
                    
                    ; Move onto next article
                    (search-for-long-description-within-list (rest list)))))))

(defn construct-long-description
    "Construct a long description string based on the provided title page content"
    [page-content]
    (if (not-empty page-content)
        (let [text-blocks (html/select page-content [:div])]
            (debug "- Looking for long description...")
            
            (if (not-empty text-blocks)
                (let [long-description (search-for-long-description-within-list text-blocks)]
                    long-description)))))

(defn search-for-company-within-list
    "Searches through all of the provided article DIV's sub sections until we find the
     production company section. Then selects the first company from the available list."
    [list]
    (if (not-empty list)
        (let [h4 (html/select (first list) [:h4])]
            (if (not-empty h4)
                ; Check whether this is the right text block
                (if (= (first (:content (first h4))) "Production Co:")
                    
                    ; Extract first company
                    (let [production-company-link (html/select (first list) [:a])]
                        (let [production-company (first (:content (first production-company-link)))]
                            (debug "----" production-company)
                            
                            (if (not-empty production-company)
                                (if (not-empty (html/select production-company [:span]))
                                    (ccstring/trim (first (:content production-company)))
                                    
                                    (ccstring/trim production-company)))))
                    
                    ; Move onto next item
                    (search-for-company-within-list (rest list)))))))

(defn construct-production-company
    "Construct a production company string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (let [text-blocks (html/select page-content [:div.txt-block])]
            (debug "- Looking for production companies...")
            
            (if (not-empty text-blocks)
                (let [production-company (search-for-company-within-list text-blocks)]
                    (if (not-empty production-company)
                        
                        ; Final tweak
                        (str "© " production-company)))))))
