
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

(ns uk.co.anthonycampbell.imdb.format
    (:use clojure.java.io)
    (:use clojure.tools.logging)
    (:use clj-logging-config.log4j)
    (:require [clojure.string])
    (:require [clojure.contrib.string :as ccstring])
    (import (java.util Date))
    (import (java.text SimpleDateFormat)))

(def genres
    (hash-map
        "action"         "Action & Adventure",
        "adventure"      "Action & Adventure",
        "comedy"         "Comedy",
        "documentary"    "Documentary",
        "drama"          "Drama",
        "horror"         "Horror",
        "kids"           "Kids & Family",
        "family"         "Kids & Family",
        "animation"      "Kids & Family",
        "sci-fi"         "Sci-Fi & Fantasy",
        "fantasy"        "Sci-Fi & Fantasy",
        "sports"         "Sports",
        "thriller"       "Thriller",
        "mystery"        "Thriller",
        "western"        "Western"))

(defn format-date
    "Formats the provided date string into a simple shortdate"
    [date-string]
    
    (if (not-empty date-string)
        (try
            (let [date (. (SimpleDateFormat. "dd MMM yyyy") parse date-string)]
                (. (SimpleDateFormat. "yyyy-MM-dd") format date))
            (catch java.text.ParseException pe nil))))

(defn format-genre
    "Formats the provided genre string into the iTunes compatable genre"
    [genre-string]
    
    (if (not-empty genre-string)
        ; Convert genre string
        (loop [updated-genre ""
               genre-list (clojure.string/split (ccstring/lower-case genre-string) #"\s")]
            (if (not-empty genre-list)
                
                ; Recurrsively search until we have an available conversion
                (if (empty? updated-genre)
                    (recur
                        (str updated-genre (if (contains? genres (first genre-list))
                            (get genres (first genre-list))
                            ""))
                        (rest genre-list))
                    updated-genre)
                updated-genre))))

(defn format-struct
    "Formats and then writes the provided media struct to the specified output file"
    [media-struct output-file]
    
    ; Validate
    (if (not (nil? media-struct))
        (let [builder (new StringBuilder)]
            (.append builder "\n")
                
            (if (not-empty (:title media-struct))
                (.append builder (str (str "Title:                  " (:title media-struct)) "\n\n")))
            (if (not-empty (:href media-struct))
                (.append builder (str (str "URL:                    " (:href media-struct)) "\n")))
            (if (not-empty (:cast-href media-struct))
                (.append builder (str (str "Cast URL:               " (:cast-href media-struct)) "\n")))
            (if (not-empty (:classification media-struct))
                (.append builder (str (str "Classification:         " (:classification media-struct)) "\n")))
            (if (not-empty (:genre media-struct))
                (.append builder (str (str "Genre:                  "
                    (format-genre (:genre media-struct))) "\n")))
            (if (not-empty (:release-date media-struct))
                (.append builder (str (str "Release Date:           "
                    (format-date (clojure.string/replace (:release-date media-struct) #"[  ]" " "))) "\n")))
            (if (not-empty (:production-company media-struct))
                (.append builder (str (str "Production Company:     " (:production-company media-struct)) "\n")))
            (if (not-empty (:description media-struct))
                (.append builder (str (str "\nDescription:            " (:description media-struct)) "\n")))
            (if (not-empty (:long-description media-struct))
                (.append builder (str (str "\nLong Description:       " (:long-description media-struct)) "\n")))
            (if (not-empty (:cast media-struct))
                (.append builder (str (str "\nCast:                   " (:cast media-struct)) "\n")))
            (if (not-empty (:directors media-struct))
                (.append builder (str (str "\nDirector:               " (:directors media-struct)) "\n")))
            (if (not-empty (:producers media-struct))
                (.append builder (str (str "\nProducers:              " (:producers media-struct)) "\n")))
            (if (not-empty (:screen-writers media-struct))
                (.append builder (str (str "\nScreen Writers:         " (:screen-writers media-struct)) "\n")))
            
            (if (not-empty output-file)
                ; Open output file
                (with-open [fw (writer output-file)]
                    (println (str (str "\n    Writing output to file: '", output-file) "'\n"))
                    (.write fw (.toString builder))))
            
            (.toString builder))))
