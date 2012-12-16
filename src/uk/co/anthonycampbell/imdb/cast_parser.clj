(ns uk.co.anthonycampbell.imdb.cast-parser
    (:use uk.co.anthonycampbell.imdb.request)
    (:require [clj-http.client :as client])
    (:require [net.cgrand.enlive-html :as html])
    (:require [net.cgrand.xml :as xml])
    (:require [clojure.string])
    (:require [clojure.contrib.string :as ccstring]))

(defn construct-cast
    "Construct a cast string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (str "")))

(defn construct-directors
    "Construct a directors string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (str "")))

(defn construct-producers
    "Construct a producers string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (str "")))

(defn construct-screen-writers
    "Construct a screen-writers string based on the provided parsed page content"
    [page-content]
    (if (not-empty page-content)
        (str "")))