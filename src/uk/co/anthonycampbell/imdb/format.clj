(ns uk.co.anthonycampbell.imdb.format
    (:use clojure.java.io))

;(defn prep-for-file 
;    "Formats the output so it can be written correctly to the output file"
;    [rec]
;    (apply str (map #(str
;        (format-output (first
;            (uk.co.anthonycampbell.imdb.parser/get-awards-per-year (:href %)))) "\n") rec)))

;(defn -main [& args]
;    "Runs the parser and then writes the results to the output file"
;    (spit "film.txt" 
;        (prep-for-file "")))

(defn write-to-file
    "Formats and then writes the provided media struct to the specified output file"
    [media-struct output-file]
    
    (if (not (nil? media-struct))
        (if (not-empty output-file)
            
            (with-open [file-writer (writer output-file)]
                (.write file-writer "TEST!")
                
                ))))

(defn if-work-not-nil 
    "Formats the book's line like so: title author and WINNER if it
    won as long as work is not nil"
    [work]
    (str (if (not (nil? (:title work))) (str "\n\t" (:title work) " - " (:author work))) 
        (if (not (nil? (:winner work))) (str " (WINNER)") ) ""))

(defn format-nominees [works]
    "formats all winners and nominees for a given award into one string"
    (apply str (map if-work-not-nil works)))

(defn format-output [novels]
    "formats the award section including award title, winners and nominees"
    (format "%s - Best Novel%s\n" (:year novels) (format-nominees (:books novels))))
