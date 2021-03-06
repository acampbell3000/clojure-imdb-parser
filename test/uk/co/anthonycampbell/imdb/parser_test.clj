
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
          [uk.co.anthonycampbell.imdb.request :as request]
          [uk.co.anthonycampbell.imdb.cast-parser :as cast-parser]
          [uk.co.anthonycampbell.imdb.parser :as parser]
          [uk.co.anthonycampbell.imdb.struct :as struct]
          [uk.co.anthonycampbell.imdb.core :as core]
          [uk.co.anthonycampbell.imdb.log :as log]))

(def test-url "http://www.imdb.com")
(def test-query-url "http://www.imdb.com/find?s=all&q=clash%20of%20the%20titans")
(def test-title-url "http://www.imdb.com/title/tt0800320")
(def test-agent request/select-agent)

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
        "Ensure we successfully select title from parsed results."
        (is (not (= "" (select-title-from-results (body-resource test-query-url)))))))

(deftest check-construct-media-struct-from-results
    (testing
        "Ensure we can successfully convert search results into usable struct."
        (is (not (= "" (construct-media-struct-from-results
                           (select-title-from-results (body-resource test-query-url))
                           test-url))))))

(setup-logging)
(run-tests)
