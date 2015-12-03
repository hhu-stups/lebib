(ns lebib.filters
  (:require [clojure.string :refer [lower-case split]]))

(defn- build-kv-filter [[key value]]
  (fn [db]
    [(str value)
     (filter
       (fn [[_ record]] (= (key record) value))
       db)]))

(defn- build-author-filter [author]
  (fn [db]
    [author
     (filter
       (fn [[_ v]]
         (some
           (fn [s] (.contains (lower-case s) (name author))) (:author v)))
       db)]))

(defn- build-keyword-filter [kw]
  (let [lkw (lower-case (name kw))]
    (println lkw)
    (fn [db]
      [lkw
       (filter
         (fn [[_ v]] (some #{lkw} (split (lower-case (get v :stupskeywords "")) #",")))
         db)])))

(def ^{:private true} authors [:leuschel :bendisposto :schneider])
(def ^{:private true} keywords [:advance :prob :pyb])
(def ^{:private true} kv-pairs (mapv (fn [x] [:year x]) (range 1998 2016)))

(def ^{:private true} author-rules (map build-author-filter authors))
(def ^{:private true} kv-rules (map build-kv-filter kv-pairs))
(def ^{:private true} keyword-rules (map build-keyword-filter keywords))

(def rules (concat author-rules kv-rules keyword-rules))
