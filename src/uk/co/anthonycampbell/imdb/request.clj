(ns uk.co.anthonycampbell.imdb.request
    (:require [clj-http.client :as client])
    (:require [net.cgrand.enlive-html :as html])
    (import (java.io ByteArrayInputStream))
    (import (java.net URLEncoder)))

; Declare available agents
(def chrome-agent
    "Mozilla/5.0 (X11; CrOS i686 2268.111.0) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.57 Safari/536.11")
(def firefox-agent
    "Mozilla/5.0 (Windows NT 6.1; rv:15.0) Gecko/20120716 Firefox/15.0a2")
(def safari-agent
    "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25")
(def ie10-agent
    "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)")
(def ie9-agent
    "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)")
(def ie7-agent
    "Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)")
(def http-agents (list chrome-agent firefox-agent safari-agent, ie7-agent, ie9-agent, ie10-agent))

; Agent selector
(def select-agent
    "Select a random agent from the supported list"
    (nth http-agents (rand-int (count http-agents))))

; HTTP request / response functions
(defn encode-url
    "Ensure the provided URL is safe"
    [url]
    (URLEncoder/encode url))

(defn fetch-body
    "Using the CLJ HTTP client - send the HTTP request."
    [url]
    (:body (client/get url { :headers { "User-Agent" select-agent }})))

(defn body-resource
    "Retrieves the web page specified by the url and makes an html-resource
     out of it which is used by enlive."
    [url]
    (html/html-resource (ByteArrayInputStream. (.getBytes (fetch-body url)))))
