(ns uk.co.anthonycampbell.imdb.core
    (:gen-class)
    (:use uk.co.anthonycampbell.imdb.request)
    (:use uk.co.anthonycampbell.imdb.struct)
    (:use uk.co.anthonycampbell.imdb.parser)
    (:use uk.co.anthonycampbell.imdb.format))

(def base-url "http://www.imdb.com")
(def query-url (str base-url "/find?s=all&q="))

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

(defn add-base-to-url
    "Helper method to prepend the base URL if it's not available in the provided URL"
    [url]
    (if (not-empty url)
        (let [base-url-length (count base-url)]
            
            ; Is URL short than base URL?
            (if (>= (count url) base-url-length)
                
                ; Does URL start with base URL?
                (if (= (subs url 0 base-url-length))
                    
                    ; We're ALL good
                    url
                    (str base-url url))
                (str base-url url)))))

(defn perform-search
    "Takes the provided query string and performs a search for the title on IMDB"
    [query-term]
    
    ; Prepare query string
    (if (not-empty query-term)
        (let [url (str query-url (encode-url query-term))]
            
            ; Search for provided title
            (let [search-response (body-resource url)]
                (if (not-empty search-response)
                    
                    ; Construct result
                    (construct-media-struct-from-results
                          (select-media-from-results search-response) base-url))))))

(defn -main
    "Runs the parser and then writes the results to the output file"
    [& args]
    (println (str "Args: " (first args)))
    (println "")
    
    ; Validate
    (if (not-empty args)
        (let [query-term (first args)]
            (if (not-empty query-term)
                
                ; Perform search
                (let [search-response (perform-search query-term)]
                    
                    ; Validate search result
                    (if (not-empty search-response)
                        (let [title-url (add-base-to-url (:href search-response))]
                            (if (not-empty (:href search-response))
                                
                                ; Open title page
                                (let [page-content (body-resource title-url)]
                                    
                                    ; Parse title
                                    (let [media-struct (update-media-struct page-content
                                            ; Parse title overview
                                            (update-media-struct (parse-title-main-details
                                                page-content) search-response))]
                                        
                                        ; Prepare cast URL
                                        (if (not-empty (:cast-href media-struct))
                                            (let [cast-url (add-base-to-url (:cast-href media-struct))]
                                                
                                                ; Open cast page
                                                (let [cast-page-content (body-resource cast-url)]
                                                    (update-media-struct cast-page-content, media-struct)
        )))))))))))))

;(def clash-url "http://www.imdb.com/title/tt0800320")
;                http://www.imdb.com/title/tt0800320/?ref_=fn_al_tt_1
(def clash-url  "http://www.imdb.com/title/tt0800320fullcredits#cast")

; Testing...
(println "\n---Begin---")

(println "")
(println (-main "Clash of the titans"))
(println "")
;(println (update-media-struct (parse-title-main-details (body-resource clash-url)) nil))
;(println "")

(println "----End----")
