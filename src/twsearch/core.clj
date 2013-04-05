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
  (let [get-url (str api-base "search.json" url)]
    (try
      (json/read-str (:body (http/get get-url)))
      (catch Exception e {"refresh_url" url,
                          "results" []}))))

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

(defn weights
  [edges]
  (map (fn [x] (let [[k v] x]
                 (conj k {:weight v})))
       (reduce (fn [x y] (assoc x y (+ (get x y 0) 1)))
               {}
               edges)))

(defn process-results
  "do the results processing"
  ([new x]
  (let [edges (concat x 
                      (reduce concat (map make-edges
                                          new)))
        ]
    edges))
  ([outfile new x]
  (let [edges (process-results new x)
        w (weights edges)]
    (spit outfile (gexf/write (into [] w)))
    edges)))
  
(defn searches
  "searches for the term n times"
  [term n & {:as opts}]
  (let [url (urlify term)
        pf (if (:outfile opts)
             (partial process-results (:outfile opts))
             process-results)
        result-agent (agent '())
        check (if (:forever opts)
                (fn [x] true)
                (partial <= 0))]
    (loop [url url n n]
      (if (check n)
        (let [sr (twitter-search url)]
          (Thread/sleep 20000)
          (send-off result-agent (partial pf (get sr "results")))
          (recur (get sr "refresh_url") 
                 (- n 1)))
        (do 
          (await result-agent)
          (deref result-agent))))))



(defn -main 
  [& args]
  (let [args (into [] args)
        term (get args 0)
        n (java.lang.Integer/parseInt (get args 1))
        outfile (get args 2)]
    (println (format "Searching for %s %s" 
                     term
                     (if (== 0 n)
                       "forever"
                       (format "%s times" n))))
    (searches term 
              n
              :outfile outfile
              :forever (== 0 n))
    (println "done, shutting-down")
    (shutdown-agents)))
  