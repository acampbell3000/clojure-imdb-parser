
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

(ns uk.co.anthonycampbell.imdb.core-test
    (:use [clojure.test :as test]
          [uk.co.anthonycampbell.imdb.core :as core]
          [uk.co.anthonycampbell.imdb.log :as log]))

(defn do-integration-test
    "Iterating through test data is easier than writing a lot of unit tests!"
    [test-data-list]
    (loop [result false
           test-data test-data-list]
        (if (not-empty test-data)
            (recur
                (testing
                    (str "Testing: " (first test-data))
                    (is (not-empty (parse (first test-data) ""))))
                
                (rest test-data)))))

; Test data
(def test-title-data
    ["Unforgiven",
    "Tombstone",
    "3:10 Yuma",
    "Open Range",
    "Appaloosa"])

(def test-generic-title-data
    ["James Bond"])

(def test-title-tv-data
    ["Firefly"])

(deftest test-titles
  (do-integration-test test-title-data))

(deftest test-generic-titles
    (do-integration-test test-generic-title-data))

(deftest test-tv-titles
    (do-integration-test test-title-tv-data))

(setup-logging)
(run-tests)
