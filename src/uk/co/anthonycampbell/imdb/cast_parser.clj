
; Copyright 2012 Anthony Campbell (anthonycampbell.co.uk)
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

(defn search-for-cast
    "Searches through all of the provided cast tables until we find the
     cast sub section."
    [cast-tables]
    (if (not-empty cast-tables)
        (let [cast-table (html/select cast-tables [:table.cast])]
            (if (not-empty cast-table)
                cast-table))))

(defn search-for-directors
    "Searches through all of the provided cast tables until we find the
     director sub section."
    [cast-tables]
    (if (not-empty cast-tables)
        (let [table-links (html/select (first cast-tables) [:a])]
            (if (not-empty table-links)
                
                ; Check whether this is the right article
                (if (.contains (str (first (:content (first table-links)))) "Directed by")
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
                (if (.contains (str (first (:content (first table-links)))) "Produced by")
                    ; Return producers
                    (rest (html/select (first cast-tables) [:tr]))
                    
                    ; Move onto next article
                    (search-for-producers (rest cast-tables)))))))

(defn search-for-screen-writers
    "Searches through all of the provided cast tables until we find the
     writing credits sub section."
    [cast-tables]
    (if (not-empty cast-tables)
        (let [table-links (html/select (first cast-tables) [:a])]
            (if (not-empty table-links)
                
                ; Check whether this is the right article
                (if (.contains (str (first (:content (first table-links)))) "Writing credits")
                    ; Return directors
                    (rest table-links)
                    
                    ; Move onto next article
                    (search-for-screen-writers (rest cast-tables)))))))

(defn construct-cast
    "Construct a cast string based on the provided parsed page content"
    [page-content]
    
    (if (not-empty page-content)
        (let [cast-table (search-for-cast (parse-cast-page page-content))]
            
            ; Parse cast list
            (loop [cast-list []
                   cast-rows (html/select cast-table [:tr])]
                (if (not-empty cast-rows)
                    ; Recurrsively compile cast list
                    (recur (concat cast-list
                        (let [cast-name (html/select (first cast-rows) [:td.nm])
                              cast-character (html/select (first cast-rows) [:td.char])]
                            
                            ; Only persist if we're dealing with a 'real' character
                            (if (not-empty cast-character)
                                (let [cast-name-value (first (:content (first cast-name)))
                                      cast-character-value (:tag (first (:content (first cast-character))))]
                                    (if (= (str cast-character-value) ":a")
                                        (if (not-empty cast-name-value)
                                            [(first (:content cast-name-value))]))))))
                        ; Next row
                        (let [cast-character (html/select (first cast-rows) [:td.char])]
                            (if (not-empty cast-character)
                                (let [cast-character-value (:tag (first (:content (first cast-character))))]
                                    
                                    ; We don't want too many cast entries
                                    (if (<= (count cast-list) 20)
                                        (rest cast-rows))))))
                
                ; Convert list to string
                (loop [cast-string ""
                       cast-list-temp cast-list]
                    (if (not-empty cast-list-temp)
                        ; Recurrsively compile cast string
                        (recur (str cast-string (str ", " (first cast-list-temp)))
                            ; Next element
                            (rest cast-list-temp))
                        
                        ; Finaly clean up
                        (if (not-empty cast-string)
                            (ccstring/trim (subs cast-string 2))))))))))

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
                (if (not-empty director-string)
                    (ccstring/trim (subs director-string 2)))))))

(defn construct-producers
    "Construct a producers string based on the provided parsed page content"
    [page-content]
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
                (if (not-empty producer-string)
                    (ccstring/trim (subs producer-string 2)))))))

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
                        (if (not-empty screen-writer-string)
                            (ccstring/trim (subs screen-writer-string 2)))))))))
