
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
                                            (debug "- First title link:", first-title-link, "\n")
                                            
                                            ; Persit first link in this section
                                            (merge section-title-link first-title-link)))))))
                    (rest section-list)))
            section-title-link)))

(defn parse-title-main-details
    "Selects the main information from the selected title page"
    [page-content]
    (html/select page-content
        [:html :body :div#wrapper :div#root :div#pagecontent :div#content-2-wide
         :div#maindetails_center_top :div.article.title-overview :div#title-overview-widget
         :table#title-overview-widget-layout :tr :td#overview-top]))

(defn parse-title-extended-details
    "Selects the extended information from the selected title page"
    [page-content]
    (html/select page-content
        [:html :body :div#wrapper :div#root :div#pagecontent :div#content-2-wide
         :div#maindetails_center_bottom :div.article]))

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
                            ; Match!
                            (ccstring/trim (:href (:attrs link)))
                            
                            ; Next link
                            (select-href-from-link (rest head-links)))))))))

(defn search-for-summary-within-article
    "Searches through all of the provided article DIV's sub sections until we find the
     storyline section. The selects the first non-empty paragraph available."
    [article-divs]
    (if (not-empty article-divs)
        (let [tag-h2 (html/select article-divs [:h2])]
            (if (not-empty tag-h2)
                
                ; Check whether this is the right article
                (if (= (first (:content (first tag-h2))) "Storyline")
                    
                    ; Extract first paragraph
                    (let [storyline (html/select article-divs [:p])]
                        (if (not-empty storyline)
                            (ccstring/trim (first (:content (first storyline))))))
                    
                    ; Move onto next article
                    (search-for-summary-within-article (rest article-divs)))))))

(defn parse-summary
    "Selects the long description from the selected title page"
    [page-content]
    (if (not-empty page-content)
        (let [extended-details (parse-title-extended-details page-content)]
            (if (not-empty extended-details)
                
                ; Check each sub article available in the extended titles section
                (search-for-summary-within-article extended-details)))))

(defn search-for-company-within-article
    "Searches through all of the provided article DIV's sub sections until we find the
     production company section. Then selects the first company from the available list."
    [article-div]
    (if (not-empty article-div)
        
        ; Grab ALL text blocks for this particular article
        (let [txt-block (html/select article-div [:div.txt-block])]
            (if (not-empty txt-block)
                (let [tag-h4 (html/select (first txt-block) [:h4])]
                    (if (not-empty tag-h4)
                        
                        ; Check whether this is the right text block
                        (if (= (first (:content (first tag-h4))) "Production Co:")
                            
                            ; Extract first company
                            (let [production-link (html/select (first txt-block) [:a])]
                                (if (not-empty production-link)
                                    (ccstring/trim (first (:content (first production-link))))))
                            
                            ; Move onto next text block
                            (search-for-company-within-article (rest txt-block)))))))))

(defn parse-production-company
    "Selects the production companies from the selected title page"
    [page-content]
    (if (not-empty page-content)
        (let [extended-details (parse-title-extended-details page-content)]
            (if (not-empty extended-details)
                
                ; Check each sub article available in the extended titles section
                (let [company-search (search-for-company-within-article extended-details)]
                    (if (not-empty company-search)
                        
                        ; Final tweak
                        (str "© " company-search)))))))

(defn select-cast-crew-link
    "Attempts to select the 'see more case / crew' link from the provided list."
    [page-content]
    (if (not-empty page-content)
        (let [link (first page-content)]
            (if (not-empty link)
                ; Right anchor link?
                (if (= (first (:content link)) "Full cast and crew")
                    ; Match!
                    (ccstring/trim (:href (:attrs link)))
                    
                    ; Next link
                    (select-cast-crew-link (rest page-content)))))))

(defn parse-cast-crew-url
    "Parse the provided page contents and looks for the link to the cast and crew page."
    [page-content]
    (if (not-empty page-content)
        (let [extended-details (parse-title-extended-details page-content)]
            (if (not-empty extended-details)
            
                ; Find ALL 'see-more' DIVs
                (let [see-more-div (html/select extended-details [:div.see-more])]
                    (if (not-empty see-more-div)
                        
                        ; Find each link inside this DIV
                        (let [see-more-links (html/select see-more-div [:a])]
                            
                            ; Attempt grab correct link
                            (select-cast-crew-link see-more-links))))))))

(defn construct-title
    "Construct a title string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (let [h1-header (html/select page-content [:h1.header])]
            (if (not-empty h1-header)
                (ccstring/trim (first (:content (first h1-header))))))))

(defn construct-href
    "Construct a href string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        
        ; Let's try and get href from 'canonical' link
        (let [head-links (html/select page-content [:head :link])]
            (if (not-empty head-links)
                (select-href-from-link head-links)))))

(defn construct-cast-href
    "Construct a href string for the provided page's cast summary"
    [page-content]
    (if (not-empty page-content)
        (let [title-href (construct-href page-content)]
            
            (if (not-empty title-href)
                (str title-href (parse-cast-crew-url page-content))))))

(defn construct-classification
    "Construct a classification string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        
        ; Select title attribute from first infobar span element
        (let [infobar-span (html/select page-content [:div.infobar :span])]
            (if (not-empty infobar-span)
                (:title (:attrs (first infobar-span)))))))

(defn select-genres-from-span-list
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
                                (debug "-----", genre)
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
                (let [genre-string (select-genres-from-span-list itemprops)]
                    (if (not-empty genre-string)
                        (ccstring/trim genre-string)))))))

(defn select-release-date-from-meta-list
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
                                (debug "-----", release-date)
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
                (let [release-date-string (select-release-date-from-meta-list meta-list)]
                    (if (not-empty release-date-string)
                        (ccstring/trim release-date-string)))))))

(defn construct-description
    "Construct a description string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        
        ; Select text from first non-empty paragraph
        (let [paragraph (html/select page-content [:p])]
            (if (not-empty paragraph)
                (let [paragraph-content (:content (first paragraph))]
                    (if (not-empty paragraph-content)
                        (ccstring/trim (first paragraph-content))
                        
                        ; Next paragraph
                        (construct-description (rest paragraph))))))))

(defn construct-long-description
    "Construct a long description string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (parse-summary page-content)))

(defn construct-production-company
    "Construct a production company string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (parse-production-company page-content)))
