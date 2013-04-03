(defproject twsearch "0.1.0-SNAPSHOT"
  :description "a small clojure tool to dump twitter search networks"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
    [gexf "0.1.0-SNAPSHOT"]
    [clj-http "0.6.5"]
    [org.clojure/data.json "0.2.1"]]
  :main twsearch.core
  :disable-implicit-clean true)
