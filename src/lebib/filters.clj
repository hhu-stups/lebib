(ns lebib.filters
  (:require [clojure.string :refer [lower-case split]]))

; Filters are functions that filter the content of the publication database by
; different criteria, e.g. Author, year, keyword.
;
; Each filter takes a database (list of publications) as input and returns a
; map entries from the filter-key to the map of entries that match the criteria
; {1998 {cite-key entry}}

(defn- build-year-filter [value]
  (fn [db] [(keyword (str value)) (filter #(= value (:year %)) db)]))

(defn- build-author-filter [author]
  (let [pred (fn [s] (.contains (lower-case s) (lower-case (name author))))]
    (fn [db]
      [author (filter #(some pred (:author %)) db)])))

(defn- build-keyword-filter [kw] (fn [db] [kw (filter #(some #{kw} (:stupskeywords %)) db)]))

(def ^{:private true} authors [:leuschel :bendisposto :schneider :dobrikov
                               :hansen :krings :ladenberger :witulski :clark
                               :höfges :körner :witt :bolz :borgemans :büngener
                               :craig :cuni :elbeshausen :fontaine :fritz
                               :hager :hallerstede :hudson :jastram :luo
                               :plagge :rigo :samia :spermann :weigelt
                               :wiegard :schmidt :rutenkolk :dunkelau :gruteser])
(def ^{:private true} keywords [:advance :prob :pyb :tla])

(def ^{:private true} author-rules (map build-author-filter authors))
(def ^{:private true} year-rules (map build-year-filter (range 1990 (.getValue (java.time.Year/now)))))
(def ^{:private true} keyword-rules (map build-keyword-filter keywords))
(def ^{:private true} all-rule [(fn [db] [:all db])])

(def ^{:private true} rules (concat all-rule author-rules year-rules keyword-rules))
(defn filtered [db] (filter (comp seq second) ((apply juxt rules) db)))
