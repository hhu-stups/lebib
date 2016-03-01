(ns lebib.www3-hhu.pdf
  (:require
    [pl.danieljanus.tagsoup :refer [parse]]
    [clojure.string :refer [ends-with?]]))

(def listing-url "https://www3.hhu.de/stups/downloads/pdf")

(def documents
  (filter
    (fn [x] (ends-with? x ".pdf"))
    (filter
      string?
      (map :href (flatten (parse listing-url))))))

(defn has-pdf? [doc-name]
  (boolean
    (some #{doc-name} documents)))

(defn get-url [doc-name] (str listing-url "/" doc-name))
