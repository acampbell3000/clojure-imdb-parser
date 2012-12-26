(ns uk.co.anthonycampbell.imdb.format
    (:use clojure.java.io)
    (:require [clojure.string])
    (import (java.util Date))
    (import (java.text SimpleDateFormat)))

(def genres []
    )

(defn format-date
    "Formats the provided date string into a simple shortdate"
    [date-string]
    
    (if (not-empty date-string)
        (let [date (. (SimpleDateFormat. "dd MMM yyyy") parse date-string)]
            (. (SimpleDateFormat. "yyyy-MM-dd") format date))))

(defn format-genre
    "Formats the provided genre string into the iTunes compatable genre"
    [genre-string]
    
    (if (not-empty genre-string)
        genre-string
        ))

(defn write-to-file
    "Formats and then writes the provided media struct to the specified output file"
    [media-struct output-file]
    
    ; Validate
    (if (not (nil? media-struct))
        (if (not-empty output-file)
            
            ; Open output file
            (with-open [fw (writer output-file)]
                (.write fw "\n")
                
                (if (not-empty (:title media-struct))
                    (.write fw (str (str "Title:\t\t\t\t\t" (:title media-struct)) "\n\n")))
                (if (not-empty (:href media-struct))
                    (.write fw (str (str "URL:\t\t\t\t\t" (:href media-struct)) "\n")))
                (if (not-empty (:cast-href media-struct))
                    (.write fw (str (str "Cast URL:\t\t\t\t" (:cast-href media-struct)) "\n")))
                (if (not-empty (:classification media-struct))
                    (.write fw (str (str "Classification:\t\t\t" (:classification media-struct)) "\n")))
                (if (not-empty (:genre media-struct))
                    (.write fw (str (str "Genre:\t\t\t\t\t"
                        (format-genre (:genre media-struct))) "\n")))
                (if (not-empty (:release-date media-struct))
                    (.write fw (str (str "Release Date:\t\t\t"
                        (format-date (clojure.string/replace (:release-date media-struct) #"[  ]" " "))) "\n")))
                (if (not-empty (:production-company media-struct))
                    (.write fw (str (str "Production Company:\t\t" (:production-company media-struct)) "\n")))
                (if (not-empty (:description media-struct))
                    (.write fw (str (str "\nDescription:\t\t\t" (:description media-struct)) "\n")))
                (if (not-empty (:long-description media-struct))
                    (.write fw (str (str "\nLong Description:\t\t" (:long-description media-struct)) "\n")))
                (if (not-empty (:cast media-struct))
                    (.write fw (str (str "\nCast:\t\t\t\t\t" (:cast media-struct)) "\n")))
                (if (not-empty (:directors media-struct))
                    (.write fw (str (str "\nDirector:\t\t\t\t" (:directors media-struct)) "\n")))
                (if (not-empty (:producers media-struct))
                    (.write fw (str (str "\nProducers:\t\t\t\t" (:producers media-struct)) "\n")))
                (if (not-empty (:screen-writers media-struct))
                    (.write fw (str (str "\nScreen Writers:\t\t\t" (:screen-writers media-struct)) "\n")))))))
