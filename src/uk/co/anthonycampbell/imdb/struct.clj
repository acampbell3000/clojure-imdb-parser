(ns uk.co.anthonycampbell.imdb.struct
    (:use uk.co.anthonycampbell.imdb.request)
    (:use uk.co.anthonycampbell.imdb.parser)
    (:use uk.co.anthonycampbell.imdb.cast-parser)
    (:require [clojure.contrib.string :as ccstring]))

; Define IMDB title struct
(defstruct media
    :title
    :href
    :cast-href
    :classification
    :genre
    :release-date
    :description
    :long-description
    :production-company
    :cast
    :directors
    :producers
    :screen-writers
    :tv-show
    :tv-season
    :tv-episode-id
    :tv-episode-number
    :tv-network
    :track
    :disk
    :sort-title
    :sort-cast
    :sort-show)

(defn construct-media-struct-from-results
    "Convert the provided search results page into a media struct"
    [media-search-results base-url]
    (struct media
        (if (not-empty media-search-results)
            (ccstring/trim (first (:content media-search-results)))
            nil)
        (if (not-empty media-search-results)
            (ccstring/trim (str base-url (:href (:attrs media-search-results))))
            nil)))

(defn update-media-struct
    "Populate media struct with provided page content. This function will NOT
     override any existing properties."
    [page-content media-struct]
    (struct media
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :title))
                (media-struct :title)
                (construct-title page-content))
            (construct-title page-content))
        
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :href))
                (media-struct :href)
                (construct-href page-content))
            (construct-href page-content))
        
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :cast-href))
                (media-struct :cast-href)
                (construct-cast-href page-content))
            (construct-cast-href page-content))
        
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :classification))
                (media-struct :classification)
                (construct-classification page-content))
            (construct-classification page-content))
        
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :genre))
                (media-struct :genre)
                (construct-genre page-content))
            (construct-genre page-content))
        
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :release-date))
                (media-struct :release-date)
                (construct-release-date page-content))
            (construct-release-date page-content))
        
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :description))
                (media-struct :description)
                (construct-description page-content))
            (construct-description page-content))
        
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :long-description))
                (media-struct :long-description)
                (construct-long-description page-content))
            (construct-long-description page-content))
        
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :production-company))
                (media-struct :production-company)
                (construct-production-company page-content))
            (construct-production-company page-content))
        
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :cast))
                (media-struct :cast)
                (construct-cast page-content))
            (construct-cast page-content))
        
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :directors))
                (media-struct :directors)
                (construct-directors page-content))
            (construct-directors page-content))
        
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :producers))
                (media-struct :producers)
                (construct-producers page-content))
            (construct-producers page-content))
        
        (if (not (nil? media-struct))
            (if (not-empty (media-struct :screen-writers))
                (media-struct :screen-writers)
                (construct-screen-writers page-content))
            (construct-screen-writers page-content))
))


; Testing...
(println "\n---Begin---")

;(def clash-url "http://www.imdb.com/title/tt0800320")
(def clash-url "http://www.imdb.com/title/tt0800320fullcredits#cast")

; Parse title page
;(let [media-struct (update-media-struct (body-resource clash-url) nil)]
;    (if (not-empty media-struct)
;        (if (not-empty (:cast-href media-struct))
;            ; Parse crew cast page
;            (let [media-struct (update-media-struct (body-resource
;                    (str "http://www.imdb.com" (:cast-href media-struct))) media-struct)]
;                (println media-struct)))))

;(println "-" (update-media-struct (parse-title-main-details (body-resource clash-url)) nil))
(println "\n-" (update-media-struct (body-resource clash-url) nil))

(println "----End----")
