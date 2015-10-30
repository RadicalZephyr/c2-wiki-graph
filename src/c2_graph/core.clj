(ns c2-graph.core
  (:require [clojure.repl :refer :all]
            [clojure.java.io :as io]
            [net.cgrand.enlive-html :as html]
            [edgewise.core :as edgewise]
            [edgewise.tgf :as tgf]))

(defn get-c2-pages-seq []
  (-> (io/resource "pages")
      io/as-file
      file-seq))

(defn as-path [file-able]
  (.toPath (io/as-file file-able)))

(defn as-absolute-path [file-able]
  (.toAbsolutePath (as-path file-able)))

(defn absolute-resources-dir []
  (.toPath (.getAbsoluteFile (io/as-file "resources"))))

(defn relative-to [root-file-path]
  (fn [page-path]
    (.relativize root-file-path page-path)))

(defn extract-url [{{link :href} :attrs}]
  link)

(defn wiki-url? [url]
  (boolean (re-find #"^wiki\?" url)))

(defn extract-links [parsed-html]
  (html/select parsed-html [:a]))

(defn get-link-name [link-href]
  (second (re-find #"^wiki\?(.*)$" link-href)))

(defn get-all-link-names [parsed-html]
  (->> (extract-links parsed-html)
       (map extract-url)
       (filter wiki-url?)
       (map get-link-name)))

(def c2-pages (get-c2-pages-seq))

;; (def h (html/html-resource "pages/AalbertTorsius"))

(defn page->link-data [page-name]
  {:page-name page-name
   :links (get-all-link-names (html/html-resource page-name))})

(defn make-file-transform [root-file-path]
  (comp (filter (complement (memfn isDirectory)))
        (map (memfn toPath))
        (map (relative-to root-file-path))
        (map (memfn toString))
        (map page->link-data)))

(defn get-page-name [page-path]
  (second (re-find #"^pages\/(.*)$" page-path)))

(defn combine-page-maps [accumulator page-map]
  (assoc accumulator (get-page-name (:page-name page-map)) (:links page-map)))

(defn make-pages-map [files]
  (transduce (make-file-transform (absolute-resources-dir))
             combine-page-maps
             {}
             files))

(defn relative-file-names [files-list]
  (->>
    files-list
    (filter (complement (memfn isDirectory)))
    (map (memfn toPath))
    (map (relative-to (absolute-resources-dir)))
    (map (memfn toString))))

(def page-file-names
  (reduce combine-page-maps
          {}
          (relative-file-names
            (take 100 (get-c2-pages-seq)))))

(defn populate-nodes [g [node list-of-nodes]]
  (reduce #(edgewise/add-vertex %1 %2) g (conj list-of-nodes node)))

(defn get-edges [starting-node list-of-nodes]
  (for [node list-of-nodes]
    [starting-node node]))

(defn populate-edges [g [node list-of-nodes]]
  (reduce (fn [g [n1 n2]] (edgewise/add-edge g n1 n2)) g
          (get-edges node list-of-nodes)))

(defn make-graph [page]
  (-> (edgewise/empty-graph)
    (populate-nodes page)
    (populate-edges page)))


(defn generate-tgf-graph [g name]
  (spit (str "graphs/" name) (tgf/->tgf g) ))
