(ns uk.co.anthonycampbell.imdb.cast-parser
    (:use uk.co.anthonycampbell.imdb.request)
    (:require [clj-http.client :as client])
    (:require [net.cgrand.enlive-html :as html])
    (:require [net.cgrand.xml :as xml])
    (:require [clojure.string])
    (:require [clojure.contrib.string :as ccstring]))

(defn parse-cast-page
    "Selects the cast information from the selected cast crew page"
    [page-content]
    (html/select page-content
        [:html :body :div#wrapper :div#root :div#pagecontent :div#tn15 :div#tn15main :div#tn15content
         :table]))

(defn search-for-directors
    "Searches through all of the provided cast tables until we find the
     director sub section."
    [cast-tables]
    ;(println cast-tables)
    
    (if (not-empty cast-tables)
        (let [table-links (html/select (first cast-tables) [:a])]
            (if (not-empty table-links)
                
                ; Check whether this is the right article
                (if (= (first (:content (first table-links))) "Directed by")
                    ; Return directors
                    (rest table-links)
                    
                    ; Move onto next article
                    (search-for-directors (rest cast-tables)))))))

(defn construct-cast
    "Construct a cast string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (str "")))

(defn construct-directors
    "Construct a directors string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        ; Parse list of directorss
        (loop [director-string ""
               director-list (search-for-directors (parse-cast-page page-content))]
            (if (not-empty director-list)
                ; Recurrsively compile director string
                (recur (str director-string (str ", " (first (:content (first director-list)))))
                    (rest director-list))
                
                ; Final clean up
                (ccstring/trim (subs director-string 2))))))

(defn construct-producers
    "Construct a producers string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (str "")))

(defn construct-screen-writers
    "Construct a screen-writers string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (str "")))
