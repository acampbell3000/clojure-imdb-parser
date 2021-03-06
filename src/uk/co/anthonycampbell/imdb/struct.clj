
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

(ns uk.co.anthonycampbell.imdb.struct
    (:use uk.co.anthonycampbell.imdb.request)
    (:use uk.co.anthonycampbell.imdb.parser)
    (:use uk.co.anthonycampbell.imdb.cast-parser)
    (:use clojure.tools.logging)
    (:use clj-logging-config.log4j)
    (:require [clojure.string])
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

(defn trim-description
    "Ensure description doesn't go over 255 characters."
    [description]
    (if (not-empty description)
        
        ; Let's get rid of those new lines
        (let [description-length (count description)]
            
            ; Is description longer than MPEG-4 limit
            (if (> description-length 255)
                ; Trim last sentence
                (let [trimmed-description (clojure.string/replace description #"\.[^.]*$" "")]
                    (trim-description trimmed-description))
                
                ; We're all done
                (let [complete-description (ccstring/trim description)]
                    (if (re-find #"\.$" complete-description)
                        complete-description
                        (str complete-description ".")))))))

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
     override any existing properties"
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
                (trim-description (construct-description page-content)))
            (trim-description (construct-description page-content)))
        
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
            (construct-screen-writers page-content))))

(defn add-base-to-url
    "Helper method to prepend the base URL if it's not available in the provided URL"
    [url, base-url]
    (if (not-empty url)
        (if (not-empty base-url)
            (let [base-url-length (count base-url)]
                
                ; Is URL short than base URL?
                (if (>= (count url) base-url-length)
                    
                    ; Does URL start with base URL?
                    (if (= (subs url 0 base-url-length) base-url)
                        ; We're ALL good
                        url
                        (str base-url url))
                    (str base-url url))))))

(defn apply-base-url
    "Ensure the href properties are prepended with the base URL"
    [media-struct base-url]
    (if (not (nil? media-struct))
        (struct media
            (media-struct :title)
            (add-base-to-url (media-struct :href) base-url)
            (add-base-to-url (media-struct :cast-href) base-url)
            (media-struct :classification)
            (media-struct :genre)
            (media-struct :release-date)
            (media-struct :description)
            (media-struct :long-description)
            (media-struct :production-company)
            (media-struct :cast)
            (media-struct :directors)
            (media-struct :producers)
            (media-struct :screen-writers))))
