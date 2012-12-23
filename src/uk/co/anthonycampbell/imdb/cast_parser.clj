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
    (if (not-empty cast-tables)
        (let [table-links (html/select (first cast-tables) [:a])]
            (if (not-empty table-links)
                
                ; Check whether this is the right article
                (if (= (first (:content (first table-links))) "Directed by")
                    ; Return directors
                    (rest table-links)
                    
                    ; Move onto next article
                    (search-for-directors (rest cast-tables)))))))

(defn search-for-producers
    "Searches through all of the provided cast tables until we find the
     producer sub section."
    [cast-tables]
    (if (not-empty cast-tables)
        (let [table-links (html/select (first cast-tables) [:a])]
            (if (not-empty table-links)
                
                ; Check whether this is the right article
                (if (= (first (:content (first table-links))) "Produced by")
                    ; Return producers
                    (rest (html/select (first cast-tables) [:tr]))
                    
                    ; Move onto next article
                    (search-for-producers (rest cast-tables)))))))

(defn search-for-screen-writers
    "Searches through all of the provided cast tables until we find the
     writing credits sub section."
    [cast-tables]
    ;(println cast-tables)
    
    (if (not-empty cast-tables)
        (let [table-links (html/select (first cast-tables) [:a])]
            (if (not-empty table-links)
                
                ; Check whether this is the right article
                (if (= (first (:content (first table-links))) "Writing credits")
                    ; Return directors
                    (rest table-links)
                    
                    ; Move onto next article
                    (search-for-screen-writers (rest cast-tables)))))))

(defn construct-cast
    "Construct a cast string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        nil))

(defn construct-directors
    "Construct a directors string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        ; Parse list of directors
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
    (println (search-for-producers (parse-cast-page page-content)))
    
    (if (not-empty page-content)
        ; Parse list of producers
        (loop [producer-string ""
               producer-list (search-for-producers (parse-cast-page page-content))]
            (if (not-empty producer-list)
                
                ; Recurrsively compile producer string
                (recur (str producer-string 
                    (let [producers (html/select (first producer-list) [:a])]
                        (if (not-empty producers)
                            
                            ; If we have a type to work with lets validate
                            (if (> (count producers) 1)
                                (let [producer (first (:content (first producers)))
                                      producer-type (first (:content (second producers)))]
                                    
                                    ; Only grab if type is executive or producer
                                    (if (= (ccstring/lower-case producer-type) "producer")
                                        (str ", " producer)
                                        
                                        (if (= (ccstring/lower-case producer-type) "executive producer")
                                            (str ", " producer)
                                            "")))
                                        
                                (first (:content (first producers)))))))
                    (rest producer-list))
                
                ; Final clean up
                (ccstring/trim (subs producer-string 2))))))

(defn construct-screen-writers
    "Construct a screen-writers string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        ; Parse list of screen writers
        (loop [screen-writer-set (sorted-set)
               screen-writer-list (search-for-screen-writers (parse-cast-page page-content))]
            (if (not-empty screen-writer-list)
                
                ; Recurrsively push screen writers into set to de-dupe items
                (recur (conj screen-writer-set (first (:content (first screen-writer-list))))
                    (rest screen-writer-list))
                
                (loop [screen-writer-string ""
                       screen-writer-set (disj screen-writer-set "WGA")]
                    (if (not-empty screen-writer-set)
                        
                        ; Compile screen writer string
                        (recur (str screen-writer-string ", " (first screen-writer-set))
                            (rest screen-writer-set))
                        
                        ; Final clean up
                        (ccstring/trim (subs screen-writer-string 2))))))))
