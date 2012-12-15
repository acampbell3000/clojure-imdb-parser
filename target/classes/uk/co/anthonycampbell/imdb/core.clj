(ns uk.co.anthonycampbell.imdb.core
    (:gen-class)
    (:use uk.co.anthonycampbell.imdb.parser)
    (:use uk.co.anthonycampbell.imdb.format))

(def base-url "http://www.imdb.com")
(def query-url (str base-url "find?s=all&q="))

(defn prep-for-file 
    "Formats the output so it can be written correctly to the output file"
    [rec]
    (apply str (map #(str
        (format-output (first
            (uk.co.anthonycampbell.imdb.parser/get-awards-per-year (:href %)))) "\n") rec)))

;(defn -main [& args]
;    "Runs the parser and then writes the results to the output file"
;    (spit "film.txt" 
;        (prep-for-file "")))

(defn -main [& args]
    "Runs the parser and then writes the results to the output file"
    (uk.co.anthonycampbell.imdb.parser/get-awards-per-year (str query-url "Appaloosa")))
