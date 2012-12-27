
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

(ns uk.co.anthonycampbell.imdb.parser-test
    (:use [clojure.test :as test]
        [uk.co.anthonycampbell.imdb.parser :as parser]
        [uk.co.anthonycampbell.imdb.core :as core]))

(def test-url "http://www.imdb.com")
(def test-query-url "http://www.imdb.com/find?s=all&q=clash%20of%20the%20titans")
(def test-title-url "http://www.imdb.com/title/tt0800320")
(def test-agent select-agent)

(println "\nBase URL:" test-url)
(println "Title URL:" test-title-url)
(println "Agent:" test-agent)

(deftest select-random-agent
    (testing
        "Ensure we have a URL to query."
        (is (not (= "" test-url)))))

(deftest select-random-agent
    (testing
        "Ensure we select a random agent."
        (is (not (= "" test-agent)))))

(deftest check-url-encoding
    (testing
        "Ensure our URLs get encoded."
        (is (= "http%3A%2F%2Fwww.imdb.com" (encode-url test-url)))))

(deftest request-body
    (testing
        "Ensure we get a response from the server."
        (is (not (= "" (fetch-body test-url))))))

(deftest check-request-conversion-to-enlive
    (testing
        "Ensure our response from the server is converted to an enlive html-resource."
        (is (not (= "" (body-resource test-url))))))

(deftest check-parse-search-result
    (testing
        "Ensure we successfully parse the first media item from the search result."
        (is (not (= "" (parse-search-results (body-resource test-query-url)))))))

(deftest check-media-result-selection
    (testing
        "Ensure we successfully select media from parsed results."
        (is (not (= "" (select-media-from-results (body-resource test-query-url)))))))

(deftest check-construct-media-struct-from-results
    (testing
        "Ensure we can successfully convert search results into usable struct."
        (is (not (= "" (construct-media-struct-from-results
                           (select-media-from-results (body-resource test-query-url))
                           test-url))))))

(deftest check-parse-title-main-details
    (testing
        "Ensure we can successfully parse the main details from the title page."
        (is (not (= "" (parse-title-main-details (body-resource test-query-url)))))))

(deftest check-construct-classification
    (testing
        "Ensure we successfully construct classification string from parsed title page."
        (is (= "12A"
               (construct-classification
                   (parse-title-main-details (body-resource test-title-url)))))))

(deftest check-parse-genre
    (testing
        "Ensure we can successfully parse the main details from the title page."
        (is (not (= ""
                    (parse-genre
                        (parse-title-main-details (body-resource test-title-url))
                        ""))))))

(deftest check-construct-genre
    (testing
        "Ensure we successfully construct genre string from parsed title page."
        (is (= "Action Adventure Fantasy"
               (construct-genre
                   (parse-title-main-details (body-resource test-title-url)))))))

(deftest check-construct-title
    (testing
        "Ensure we successfully construct a title string from parsed title page."
        (is (= "Clash of the Titans"
               (construct-title
                   (parse-title-main-details (body-resource test-title-url)))))))

(deftest check-construct-href
    (testing
        "Ensure we successfully construct a page reference string from parsed title page."
        (is (= "/title/tt0800320"
               (construct-href
                   (parse-title-main-details (body-resource test-title-url)))))))

(deftest check-construct-release-date
    (testing
        "Ensure we successfully construct a release date string from parsed title page."
        (is (= "2 April 2010 (UK)"
               (construct-release-date
                   (parse-title-main-details (body-resource test-title-url)))))))
        
(deftest check-construct-description
    (testing
        "Ensure we successfully construct a description string from parsed title page."
        (is (= "Perseus, mortal son of Zeus, battles the minions of the underworld to stop them from conquering the Earth and the heavens."
               (construct-description
                   (parse-title-main-details (body-resource test-title-url)))))))

(deftest check-update-media-struct
    (testing
        "Ensure we successfully construct genre string from parsed page details."
        (is (not (nil? (update-media-struct
                           (parse-title-main-details (body-resource test-title-url))
                           nil))))))

(run-tests)
