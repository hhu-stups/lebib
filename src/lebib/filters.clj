(ns lebib.filters
  (:require [clojure.string :refer [lower-case split]]))

; Filters are functions that filter the content of the publication database by
; different criteria, e.g. Author, year, keyword.
;
; Each filter takes a database (list of publications) as input and returns a
; map entries from the filter-key to the map of entries that match the criteria
; {1998 {cite-key entry}}

(defn- build-kv-filter [[key value]]
  (fn [db]
    {(str value)
     (select-keys db (for  [[k record] db :when (= (key record) value)] k))}))

(defn- build-author-filter [author]
  (let [pred (fn [s] (.contains (lower-case s) (name author)))]
    (fn [db]
      {author
       (select-keys db (for [[k v] db :when (some pred (:author v))] k))})))

(defn- build-keyword-filter [kw]
  (let [lkw (lower-case (name kw))
        keywords (fn [v] (split (lower-case (get v :stupskeywords "")) #","))]
    (fn [db]
        {lkw
         (select-keys db (for [[k v] db :when (some #{lkw} (keywords v))] k))})))

(def ^{:private true} authors [:leuschel :bendisposto :schneider :dobrikov
                               :hansen :krings :ladenberger :witulski :clark
                               :höfges :körner :witt :bolz :borgemans :büngener
                               :craig :cuni :elbeshausen :fontaine :fritz
                               :hager :hallerstede :hudson :jastram :luo
                               :plagge :rigo :samia :spermann :weigelt
                               :wiegard])
(def ^{:private true} keywords [:advance :prob :pyb])
(def ^{:private true} kv-pairs (mapv (fn [x] [:year x]) (range 1998
                                                               (.getValue (java.time.Year/now)))))

(def ^{:private true} author-rules (map build-author-filter authors))
(def ^{:private true} kv-rules (map build-kv-filter kv-pairs))
(def ^{:private true} keyword-rules (map build-keyword-filter keywords))
(def ^{:private true} all-rule [(fn [db] {:all db})])

(def rules (concat all-rule author-rules kv-rules keyword-rules))
