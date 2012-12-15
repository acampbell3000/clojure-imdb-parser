(ns uk.co.anthonycampbell.imdb.parser
    (:require [net.cgrand.enlive-html :as html])
    (:require [net.cgrand.xml :as xml])
    (:require [clj-http.client :as client])
    (:require [clojure.string])
    (:require [clojure.contrib.string :as ccstring])
    (import (java.io ByteArrayInputStream))
    (import (java.net URLEncoder)))

; Remember to delete me...
(defstruct work :winner :title :author)
(defstruct category :award :books :year)

(defstruct media :title :href :classification :genre :release-date :description :long-description)

(def chrome-agent
    "Mozilla/5.0 (X11; CrOS i686 2268.111.0) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.57 Safari/536.11")
(def firefox-agent
    "Mozilla/5.0 (Windows NT 6.1; rv:15.0) Gecko/20120716 Firefox/15.0a2")
(def safari-agent
    "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25")
(def ie10-agent
    "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)")
(def ie9-agent
    "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)")
(def ie7-agent
    "Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)")
(def http-agents (list chrome-agent firefox-agent safari-agent, ie7-agent, ie9-agent, ie10-agent))

(def select-agent
    "Select a random agent from the supported list"
    (nth http-agents (rand-int (count http-agents))))
;(def select-agent
;    "Select the first agent from the supported list"
;    (first http-agents))

(defn encode-url
    "Ensure the provided URL is safe"
    [url]
    (URLEncoder/encode url))

(defn fetch-body
    "Using the CLJ HTTP client - send the HTTP request."
    [url]
    (:body (client/get url { :headers { "User-Agent" select-agent }})))

(defn body-resource
    "Retrieves the web page specified by the url and makes an html-resource
     out of it which is used by enlive."
    [url]
    (html/html-resource (ByteArrayInputStream. (.getBytes (fetch-body url)))))

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

(defn construct-media-struct-from-results
    "Convert search results into struct"
    [media-from-search-results base-url]
    (struct media
        (ccstring/trim (first (:content media-from-search-results)))
        (ccstring/trim (str base-url (:href (:attrs media-from-search-results))))))

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

(defn parse-summary
    "Selects the long description from the selected title page"
    [page-content]
    (if (not-empty page-content)
        (if (not-empty (parse-title-extended-details page-content))
            (if (> (count (parse-title-extended-details page-content)) 2)
                (ccstring/trim (first (:content (first
                    (html/select (nth (parse-title-extended-details page-content) 3) [:p])))))))))

(defn parse-genre
    "Parse the provide sequence of anchor links and produce of sequence of genres"
    [genre-sequence genre-string]
    (str genre-string
        (if (not-empty genre-sequence)
            (parse-genre (rest genre-sequence)
                         (str (first (:content (first genre-sequence))) " ")) nil)))

(defn construct-title
    "Construct a title string based on the provided parsed page content"
    [page-main-content]
    (if (not-empty page-main-content)
        (if (not-empty (html/select page-main-content [:h1.header]))
          (ccstring/trim (first (:content (first (html/select page-main-content [:h1.header]))))))))

(defn construct-href
    "Construct a href string based on the provided parsed page content"
    [page-main-content]
    (if (not-empty page-main-content)
        (if (not-empty (html/select page-main-content [:div.infobar :a]))
            (last (re-find #"([a-zA-Z-_0-9/]+)/releaseinfo"
                (ccstring/trim (:href (:attrs (last
                    (html/select page-main-content [:div.infobar :a]))))))))))

(defn construct-classification
    "Construct a classification string based on the provided parsed page content"
    [page-main-content]
    (if (not-empty page-main-content)
        (if (not-empty (html/select page-main-content [:div.infobar :span]))
            (:title (:attrs (first (html/select page-main-content [:div.infobar :span])))))))

(defn construct-genre
    "Construct a genre string based on the provided parsed page content"
    [page-main-content]
    (if (not-empty page-main-content)
        (if (not-empty (html/select page-main-content [:div.infobar :a]))
            (ccstring/trim (parse-genre
                (drop-last (html/select page-main-content [:div.infobar :a])) "")))))

(defn construct-release-date
    "Construct a release date string based on the provided parsed page content"
    [page-main-content]
    (if (not-empty page-main-content)
        (if (not-empty (html/select page-main-content [:div.infobar :a]))
            (ccstring/trim (nth (re-find #"([a-zA-Z0-9  ]+)(\(UK\))?"
                (ccstring/trim (first (:content (last
                    (html/select page-main-content [:div.infobar :a])))))) 1)))))

(defn construct-description
    "Construct a description string based on the provided parsed page content"
    [page-main-content]
    (if (not-empty page-main-content)
        (if (not-empty (html/select page-main-content [:p]))
            (ccstring/trim (first (:content (second (html/select page-main-content [:p]))))))))

(defn construct-long-description
    "Construct a long description string based on the provided parsed page content"
    [page-main-content]
    (if (not-empty page-main-content)
        (parse-summary page-main-content)))

(defn update-media-struct
    "Convert search results into populated struct"
    [page-main-content media-struct]
    (struct media
        (if (not (nil? media-struct))
            (if (not (nil? (media-struct :title)))
                (media-struct :title) (construct-title page-main-content))
            (construct-title page-main-content))
        
        (if (not (nil? media-struct))
            (if (not (nil? (media-struct :href)))
                (media-struct :href) (construct-href page-main-content))
            (construct-href page-main-content))
        
        (if (not (nil? media-struct))
            (if (not (nil? (media-struct :classification)))
                (media-struct :classification) (construct-classification page-main-content))
            (construct-classification page-main-content))
        
        (if (not (nil? media-struct))
            (if (not (nil? (media-struct :genre)))
                (media-struct :genre) (construct-genre page-main-content))
            (construct-genre page-main-content))
        
        (if (not (nil? media-struct))
            (if (not (nil? (media-struct :release-date)))
                (media-struct :release-date) (construct-release-date page-main-content))
            (construct-release-date page-main-content))
        
        (if (not (nil? media-struct))
            (if (not (nil? (media-struct :description)))
                (media-struct :description) (construct-description page-main-content))
            (construct-description page-main-content))
        
        (if (not (nil? media-struct))
            (if (not (nil? (media-struct :long-description)))
                (media-struct :long-description) (construct-long-description page-main-content))
            (construct-long-description page-main-content))))

; Testing...
(def clash-url "http://www.imdb.com/title/tt0800320")
(println "\n---Begin---")
;(println "-" (update-media-struct (parse-title-main-details (body-resource clash-url)) nil))
(println "-" (update-media-struct (body-resource clash-url) nil))
(println "----End----")

;////////////////////////////////////////////////////////////////

; Stuff were basing parser on:

(defn select-popular-titles
    "Selects the popular titles from the search results"
    [page-content]
    (html/select page-content
        [:html :body :div#wrapper :div#root :layer :div#pagecontent :div :div#content-2-wide :div#main :table
        (html/nth-of-type 1)]))

(defn split-author-publisher-str
    "Selects the popular titles from the search results"
    [authpubstr]
    (clojure.string/split (ccstring/replace-re #"^," ""
        (ccstring/replace-str "by " ""
            (ccstring/replace-str " by " "" authpubstr))) #"\[|\(" ))

(defn parse-author
    "Grabs the author's name"
    [authstr]
    (ccstring/trim (first (split-author-publisher-str authstr))))

(defn create-work-struct
    [work-data]
    (if (not (nil? (first (:content (first (:content work-data)))))) 
          (struct work (if (not (nil? (:attrs work-data))) (:class (:attrs work-data))) 
              (ccstring/replace-str "\"" ""
                  (ccstring/trim (first (:content (first (:content work-data))))))  
                  (parse-author (second (:content work-data))))))

(defn get-book-info 
    "Formats the book data so that each book has a title which contais 
     the book's title, author, and sometimes the publisher.  I also shows if
     the book was a winner"
    [nominees]
    (map create-work-struct nominees))

(defn parse-award-page 
    "Takes the page data retrieved and formats it in such away that each 
     hugo award group is stored with ((award title) (winner and nominees))"
    [page-content]
    (partition 2 
        (interleave (split-at 4 
            (html/select page-content #{[:div#content :p] [:p html/first-child]})) 
            (map :content (html/select page-content #{[:div#content :ul ] })))))

(defn get-awards-per-year 
    "Retrieves the awards page, parses out the categories, 
     winners and nominees and then formats the data so 
     that it can manipulated more easily."
    [url]
    (let [page-content (body-resource url)
        year (apply str (:content 
            (first (html/select page-content #{[:div#content :h2]}))))]
            (map #(struct category (apply str (first %)) 
                (get-book-info (rest (second %))) year)
                (parse-award-page page-content))))
