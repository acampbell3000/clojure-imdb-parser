(defproject clojure-imdb-parser "1.0.2"
    :description "Simple project which retrieves media information from IMDB with Clojure"
    :url "https://github.com/acampbell3000/clojure-imdb-parser"
    :dependencies [[org.clojure/clojure "1.4.0"]
                   [org.clojure/clojure-contrib "1.2.0"]
                   [enlive "1.0.1"]
                   [clj-http "0.1.2"]]
    :compile-path "target/classes/"
    :resources-path "resources/"
    :test-path "test/"
    :main uk.co.anthonycampbell.imdb.core)
