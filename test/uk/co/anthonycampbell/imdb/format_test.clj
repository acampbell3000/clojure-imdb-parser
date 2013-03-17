
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

(ns uk.co.anthonycampbell.imdb.format-test
    (:use [clojure.test :as test]
          [uk.co.anthonycampbell.imdb.format :as format]
          [uk.co.anthonycampbell.imdb.log :as log]))

; Test data
(def genre-test-data
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

(deftest format-genres
    ; Iterating through test data is easier than writing a lot of unit tests!
    (loop [result false
           test-data genre-test-data]
        (if (not-empty test-data)
            (recur
                (testing
                    (str "Testing: " (first test-data))
                    (is (=
                        (first (rest (first test-data)))
                        (format-genre (first (first test-data))))))
                
                (rest test-data)))))

(setup-logging)
(run-tests)
