(ns uk.co.anthonycampbell.imdb.parser
    (:use uk.co.anthonycampbell.imdb.request)
    (:require [clj-http.client :as client])
    (:require [net.cgrand.enlive-html :as html])
    (:require [net.cgrand.xml :as xml])
    (:require [clojure.string])
    (:require [clojure.contrib.string :as ccstring]))

(defn parse-search-results
    "Selects the search result content from the query result"
    [page-content]
    (first (html/select page-content
        [:html :body :div#wrapper :div#root :div#pagecontent :div#content-2-wide :div#main
         :div.findSection :table.findList])))

(defn select-media-from-results
    "Attempts to select the correct media from the search results"
    [page-content]
    (first (html/select (parse-search-results page-content) [:td.result_text :a])))

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
     production company section. The selects the first company from the available list."
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

(defn parse-genre
    "Parse the provided sequence of anchor links and produce the genre string"
    [genre-sequence genre-string]
    (str genre-string
        (if (not-empty genre-sequence)
            (parse-genre (rest genre-sequence)
                         (str (first (:content (first genre-sequence))) " ")))))


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
        
        ; Select last infobar anchor link
        (let [infobar-anchor (html/select page-content [:div.infobar :a])]
            (if (not-empty infobar-anchor)
                (last (re-find #"([a-zA-Z-_0-9/]+)/releaseinfo"
                    (ccstring/trim (:href (:attrs (last infobar-anchor))))))))))

(defn construct-cast-href
    "Construct a href string for the provided page's cast summary"
    [page-content]
    (if (not-empty page-content)
        (str (construct-href page-content) (parse-cast-crew-url page-content))))

(defn construct-classification
    "Construct a classification string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        
        ; Select title attribute from first infobar span element
        (let [infobar-span (html/select page-content [:div.infobar :span])]
            (if (not-empty infobar-span)
                (:title (:attrs (first infobar-span)))))))

(defn construct-genre
    "Construct a genre string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        
        ; Select title attribute from first infobar span element
        (let [infobar-anchor (html/select page-content [:div.infobar :a])]
            (if (not-empty infobar-anchor)
                
                ; Don't know why I drop the last??
                (ccstring/trim (parse-genre (drop-last infobar-anchor) ""))))))

(defn construct-release-date
    "Construct a release date string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        
        ; Select title attribute from first infobar span element
        (let [infobar-anchor (html/select page-content [:div.infobar :a])]
            (if (not-empty infobar-anchor)
                (ccstring/trim (nth (re-find #"([a-zA-Z0-9  ]+)(\(UK\))?"
                    (ccstring/trim (first (:content (last infobar-anchor))))) 1))))))

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

;////////////////////////////////////////////////////////////////

; Stuff were basing parser on:

; Remember to delete me...

;(defn select-popular-titles
;    "Selects the popular titles from the search results"
;    [page-content]
;    (html/select page-content
;        [:html :body :div#wrapper :div#root :layer :div#pagecontent :div :div#content-2-wide :div#main :table
;        (html/nth-of-type 1)]))
;
;(defn split-author-publisher-str
;    "Selects the popular titles from the search results"
;    [authpubstr]
;    (clojure.string/split (ccstring/replace-re #"^," ""
;        (ccstring/replace-str "by " ""
;            (ccstring/replace-str " by " "" authpubstr))) #"\[|\(" ))
;
;(defn parse-author
;    "Grabs the author's name"
;    [authstr]
;    (ccstring/trim (first (split-author-publisher-str authstr))))
;
;(defn create-work-struct
;    [work-data]
;    (if (not (nil? (first (:content (first (:content work-data)))))) 
;          (struct work (if (not (nil? (:attrs work-data))) (:class (:attrs work-data))) 
;              (ccstring/replace-str "\"" ""
;                  (ccstring/trim (first (:content (first (:content work-data))))))  
;                  (parse-author (second (:content work-data))))))
;
;(defn get-book-info 
;    "Formats the book data so that each book has a title which contais 
;     the book's title, author, and sometimes the publisher.  I also shows if
;     the book was a winner"
;    [nominees]
;    (map create-work-struct nominees))
;
;(defn parse-award-page 
;    "Takes the page data retrieved and formats it in such away that each 
;     hugo award group is stored with ((award title) (winner and nominees))"
;    [page-content]
;    (partition 2 
;        (interleave (split-at 4 
;            (html/select page-content #{[:div#content :p] [:p html/first-child]})) 
;            (map :content (html/select page-content #{[:div#content :ul ] })))))
;
;(defn get-awards-per-year 
;    "Retrieves the awards page, parses out the categories, 
;     winners and nominees and then formats the data so 
;     that it can manipulated more easily."
;    [url]
;    (let [page-content (body-resource url)
;        year (apply str (:content 
;            (first (html/select page-content #{[:div#content :h2]}))))]
;            (map #(struct category (apply str (first %)) 
;                (get-book-info (rest (second %))) year)
;                (parse-award-page page-content))))
