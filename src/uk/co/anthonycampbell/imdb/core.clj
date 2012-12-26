
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

(ns uk.co.anthonycampbell.imdb.core
    (:gen-class)
    (:use uk.co.anthonycampbell.imdb.request)
    (:use uk.co.anthonycampbell.imdb.struct)
    (:use uk.co.anthonycampbell.imdb.parser)
    (:use uk.co.anthonycampbell.imdb.cast-parser)
    (:use uk.co.anthonycampbell.imdb.format))

(def base-url "http://www.imdb.com")
(def query-url (str base-url "/find?s=all&q="))

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

(defn parse-title
    "Takes the provided search response and use the metadata to select and
     parse the title page"
    [search-response]
    
    ; Validate search result
    (if (not-empty search-response)
        (let [title-url (:href search-response)]
            (if (not-empty title-url)
                
                ; Open and parse title page
                (let [page-content (body-resource title-url)]
                    (apply-base-url (update-media-struct page-content search-response) base-url))))))

(defn parse-cast
    "Takes the provided media struct and use the cast href to select and
     parse the cast page"
    [media-struct]
    
    ; Open cast page
    (if (not-empty media-struct)
        (let [cast-url (:cast-href media-struct)]
            (if (not-empty cast-url)
                (let [cast-page-content (body-resource cast-url)]
                    (if (not-empty cast-page-content)
                        
                        ; Parse cast page
                        (update-media-struct cast-page-content, media-struct)))))))

(defn parse
    "Runs the parsers and outputs a media struct. If specified the struct
     will also be written to an output file"
    [query-term output-file]
    
    ; Validate
    (if (not-empty query-term)
        
        ; Perform search
        (let [search-response (perform-search query-term)]
            
            ; Parse title page
            (if (not-empty search-response)
                (let [media-struct (parse-title search-response)]
                    
                    ; Parse cast page
                    (if (not-empty media-struct)
                        (let [media-struct (parse-cast media-struct)]
                            
                            ; Determine output
                            (if (not-empty output-file)
                                
                                ; Finally write to file
                                (write-to-file media-struct output-file))
                            
                            media-struct)))))))

(defn -main
    "Main method
    
     Example usage:
         clojure-imdb-parser.jar "Title Name" output-file.txt
    "
    [& args]
    (println "\n--- Begin ---\n")
    
    (if (not-empty args)
        (let [complete-media-struct (parse (first args) (second args))]
            (println complete-media-struct)))
    
    (println "\n---- End ----"))
