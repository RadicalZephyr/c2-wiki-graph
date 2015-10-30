(ns c2-graph.core
  (:require [clojure.java.io :as io]
            [net.cgrand.enlive-html :as html]
            [edgewise.core :as edgewise]))

(defn get-c2-pages-seq []
  (-> "pages"
      io/resource
      io/as-file
      file-seq))

(defn relative-to [root-file-path]
  (fn [page-path]
    (.relativize root-file-path page-path)))

(defn transduce-files [root-file-path]
  (->
   (filter (complement (memfn isDirectory)))
   (map (memfn toPath))
   (map (relative-to root-file-path))))
