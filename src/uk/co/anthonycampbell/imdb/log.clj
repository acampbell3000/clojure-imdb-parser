
; Copyright 2013 Anthony Campbell (anthonycampbell.co.uk)
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

(ns uk.co.anthonycampbell.imdb.log
    (:gen-class)
    (:use clojure.tools.logging)
    (:use clj-logging-config.log4j))

(defn setup-logging
    "Prepare log4j root logger with with console appender set to level WARN"
    []
    (org.apache.log4j.BasicConfigurator/configure)
    (.setLevel (org.apache.log4j.Logger/getRootLogger) (org.apache.log4j.Level/WARN)))
