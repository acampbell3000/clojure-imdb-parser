
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

(ns uk.co.anthonycampbell.imdb.struct-test
    (:use [clojure.test :as test]
        [uk.co.anthonycampbell.imdb.struct :as struct]))

(defn trim-descriptions
    "Iterating through test data is easier than writing a lot of unit tests!"
    [test-data-list]
    (loop [result false
           test-data test-data-list]
        (if (not-empty test-data)
            (recur
                (testing
                    (str "Testing: " (first (first test-data)))
                    (is (=
                            (trim-description (first (first test-data)))
                            (second (first test-data)))))
                
                (rest test-data)))))

; Test data
(def test-data
    (hash-map
        ; 100 bytes
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus egestas eleifend dictum
         massa nunc."
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus egestas eleifend dictum
         massa nunc.",
        
        ; 200 bytes
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam lectus diam, sagittis vel
         facilisis non, ullamcorper quis nulla. Proin lacinia feugiat orci a consectetur. Class
         aptent taciti sociosqu id."
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam lectus diam, sagittis vel
         facilisis non, ullamcorper quis nulla. Proin lacinia feugiat orci a consectetur. Class
         aptent taciti sociosqu id.",
        
        ; 250 bytes
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed luctus quam a velit varius
         sagittis malesuada tellus blandit. Cras velit nibh, ornare quis elementum at, viverra
         et velit. Fusce dignissim magna dolor. Nulla sed odio at neque sodales amet."
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed luctus quam a velit varius
         sagittis malesuada tellus blandit. Cras velit nibh, ornare quis elementum at, viverra
         et velit. Fusce dignissim magna dolor.",
        
        ; 300 bytes
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis vel eros vel est vestibulum
         rhoncus. Donec interdum, mi vitae sodales ultrices, risus nibh suscipit dui, non consequat
         arcu est a nulla. Sed vel tellus nec risus sollicitudin porta ac ac est. Fusce quis libero
         at mauris varius massa nunc."
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis vel eros vel est vestibulum
         rhoncus. Donec interdum, mi vitae sodales ultrices, risus nibh suscipit dui, non consequat
         arcu est a nulla.",
        
        ; 500 bytes
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed posuere porta dui, vel egestas
         erat mollis id. Mauris sit amet elit mauris. Nunc eu pretium velit. Donec a gravida nibh.
         Nulla malesuada porttitor interdum. Donec elementum cursus dictum. Cras eu enim vitae leo
         ornare eleifend in quis augue.

         Quisque metus arcu, tincidunt vitae dictum non, semper vitae nisl. Ut ac porta nunc.
         Pellentesque risus velit, aliquet hendrerit aliquet tincidunt, consectetur non ligula.
         Pellentesque metus nullam."
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed posuere porta dui, vel egestas
         erat mollis id. Mauris sit amet elit mauris. Nunc eu pretium velit. Donec a gravida nibh.
         Nulla malesuada porttitor interdum."
        ))

(deftest construct-descriptions
    (trim-descriptions test-data))

(run-tests)
