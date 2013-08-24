(defproject clojure-imdb-parser "1.0.7"
    :description "Simple project which retrieves media information from IMDB with Clojure"
    :url "https://github.com/acampbell3000/clojure-imdb-parser"
    
    :dependencies [[org.clojure/clojure "1.4.0"]
                   [org.clojure/clojure-contrib "1.2.0"]
                   [enlive "1.0.1"]
                   [clj-http "0.1.2"]
                   [org.clojure/tools.logging "0.2.6"]
                   [clj-logging-config "1.9.10"]
                   [log4j/log4j "1.2.17"
                       :exclusions [javax.mail/mail
                                    javax.jms/jms
                                    com.sun.jdmk/jmxtools
                                    com.sun.jmx/jmxri]]]
    
    :compile-path "target/classes/"
    :resources-path "resources/"
    :test-path "test/"
    :main uk.co.anthonycampbell.imdb.core)
