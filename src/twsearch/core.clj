(ns twsearch.core
  (:gen-class)
  (:require [gexf.core :as gexf]
            [clojure.data.json :as json]
            [clj-http.client :as http])
  (:import java.net.URLEncoder))

(def api-base "https://search.twitter.com/")

(defn urlify
  "change into a twitter search request"
  [s]
  (str "?q=" (java.net.URLEncoder/encode s "UTF-8")))
  
(defn twitter-search
  [url]
  (let [url (str api-base "search.json" url)]
    (json/read-str (:body (http/get url)))))

(defn extract-nodes
  [text]
  (let [words (clojure.string/split text #"[^a-zA-Z0-9-_#@]")]
    (filter (fn [x] (contains?  #{\@ \#} (first x)))
            words)))

(defn make-edges
  [tweet]
  (let [from (clojure.string/lower-case 
              (str \@ (get tweet "from_user")))
        text (get tweet "text")]
    (map (fn [x] [from (clojure.string/lower-case x)]) 
         (extract-nodes text))))

(defn searches
  "searches for the term n times"
  [term n]
  (let [url (urlify term)]
    (loop [url url edges '() n n]
      (if (> n 0)
        (let [sr (twitter-search url)]
          (Thread/sleep 20000)
          (recur (get sr "refresh_url") 
                 (concat edges
                         (reduce concat
                                 (map make-edges 
                                      (get sr "results"))))
                 (- n 1)))
        edges))))

(defn weights
  [edges]
  (map (fn [x] (let [[k v] x]
                 (conj k {:weight v})))
       (reduce (fn [x y] (assoc x y (+ (get x y 0) 1)))
               {}
               edges)))

(defn -main 
  [& args]
  (let [args (into [] args)
        term (get args 0)
        n (java.lang.Integer/parseInt (get args 1))
        outfile (get args 2)]
    (println (format "Searching for %s %s times" term n))
    (spit outfile 
          (gexf/write
           (into [] (weights (searches term n)))))))
  