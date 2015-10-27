(ns lebib.core
  (:gen-class)
  (:require [hiccup.core :as h :refer [html]]
            [clojure.string :as string])
  (:import [org.jbibtex BibTeXParser BibTeXDatabase]))


(def parser (proxy [BibTeXParser][]
              (checkStringResolution [k t] (println :unresolved-string k t))
              (checkCrossReferenceResolution [k t] (println :unresolved-reference k t))))

(def order {:inproceedings [:booktitle :series :volume :number :publisher :page]})

(defn publication [{:keys [type] :as entry}]
  (let [extract-fn (apply juxt (conj (get order type) :year))
        fields (remove nil? (extract-fn entry))]
    (string/join ", " fields)))

(defn render-entry [[k {:keys [title author year] :as e}]]
  (html
   [:div.bibentry
    [:div.author (string/join ", " author)]
    [:div.title title]
    [:div (str (publication e) ".")]]))

(defn parse [filename]
  (with-open [rdr (clojure.java.io/reader filename)]
    (.parse parser rdr)
    #_(let [parser (BibTeXParser.)]
      )))

(defn as-clj [[k v]]
  (let [k (keyword (.getValue k))
        v' (.toUserString v)] [k v']))

(defmulti translate (fn [[k v]] k))
(defmethod translate :year [[k v]] [k (read-string v)])
(defmethod translate :author [[k v]] [k (map #(.trim %) (string/split v #"and"))])
(defmethod translate :default [[k v]] [k v])

(defn entry->clj [entry]
  (into {:type (keyword (.. entry getType getValue))}
        (map (comp translate as-clj) (.getFields entry))))

(defn bib->clj [db]
  (->> db
       (.getEntries)
       (map (fn [[key entry]]
              [(.getValue key) (entry->clj entry)]))
       (into {})))


