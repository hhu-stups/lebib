(ns lebib.core
  (:gen-class)
  (:require
            [clojure.string :as string]
            [clojure.tools.cli :refer  [parse-opts]]
            [hiccup.core :as h :refer [html]]
            [hiccup.page :refer [include-css]]
            [lebib.filters :refer [rules]]
            [lebib.maps :refer [authors]]
            [lebib.www3-hhu.pdf :refer [has-pdf? get-url]])
  (:import [org.jbibtex BibTeXParser
                        LaTeXParser
                        LaTeXPrinter]))


(def parser (proxy [BibTeXParser][]
              (checkStringResolution [k t] (println :unresolved-string k t))
              (checkCrossReferenceResolution [k t] (println :unresolved-reference k t))))

(def latex-parser (LaTeXParser.))

(def latex-printer (LaTeXPrinter.))

(defn de-latex [str]
  (if-not (nil? (some #{\{} str))
    (.print latex-printer (.parse latex-parser str))
    str))


(def order {:inproceedings [:booktitle :editor :series :volume :number :publisher :page :pages]
            :proceedings [:editor :series :volume :number :publisher]
            :incollection [:booktitle :editor :series :volume :number :publisher :page :pages]
            :article [:journal :volume :number :publisher :page :pages]
            :mastersthesis [:school]
            :phdthesis [:school]
            :techreport [:institution :number]})

(defn publication [{:keys [type] :as entry}]
  (let [extract-fn (apply juxt (conj (get order type) :year))
        fields (remove nil? (extract-fn entry))]
    (string/join ", " fields)))

(defn render-author [name]
  (if-let [url (get authors name)]
    (html [:a {:href url} name])
    name))

(defn render-entry [[k {:keys [title author year] :as e}]]
  (html
    [:li
   [:div.pub_entry
    (when (seq? author)
      [:div.pub_author (string/join ", " (map render-author author))])
    [:b [:div.pub_title (str title ".")]]
    [:div (str " In " (publication e) ".")]
    (when (has-pdf? (str k ".pdf"))
      [:a {:href (get-url (str k ".pdf")) :title title}
       "PDF"])]]))

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
(defmethod translate :author [[k v]] [k (map #(-> % de-latex .trim) (string/split v #"and"))])
(defmethod translate :default [[k v]] [k (de-latex v)])


(defn entry->clj [entry]
  (into {:type (keyword (.. entry getType getValue))}
        (map (comp translate as-clj) (.getFields entry))))

(defn bib->clj [db]
  (->> db
       (.getEntries)
       (map (fn [[key entry]]
              [(.getValue key) (entry->clj entry)]))
       (into {})))

(defn render-div [entries]
  (html [:div.content
         [:ul (map render-entry entries)]]))

(defn render-page [entries]
  (html
   [:html
    [:head
     (include-css "http://stups.hhu.de/mediawiki/skins/stups/publications.css?270")
     ]
    [:body (render-div entries)]]))

(defn- to-filename [key] (string/replace (-> key name string/lower-case) #"[^a-z0-9]" "_"))

(defn- save [output-format dir [key db]]
  (let [f (output-format {:full render-page :snippet render-div})]
    (spit (str dir (to-filename key) ".html") (f db))))

(defn- sort-by-year [db]
  (into
    (sorted-map-by (fn [k1 k2] (let [y1 (:year (db k1))
                                     y2 (:year (db k2))]
                                 (* -1 (compare [y1 k1] [y2 k2])))))
        db))

(def cli-options
  ;; An option with a required argument
  [["-m" "--mode MODE" "Output mode: 'full' for HTML Document or 'snippet' for a fragment."
    :default :snippet
    :parse-fn keyword
    :validate [#(some #{%} [:full :snippet]) "Must be either 'full' or 'snippet'"]]
   ["-h" "--help"]])


(defn print-usage
  ([summary] (print-usage summary nil))
  ([summary error]
   (when-not (nil? error)
     (println error \newline \newline))
   (println (string/join
     \newline
     ["LeBib bibtex to html transformer."
      ""
      "Usage: lebib [options] bib-file output-dir"
      ""
      "Options:"
      summary]))))

(defn render [bibfile mode output-dir]
  (let [db (sort-by-year (bib->clj (parse bibfile)))]
    ; write the full pub list to dir
    (save mode output-dir [:all db])
    ; write all filtered lists to output dir
    (mapv (partial save mode output-dir)
          ((apply juxt rules) db))))

(defn -main [& args]
  ; XXX validate cli input
  (let [opts (parse-opts args cli-options)
        {:keys [options arguments summary]} opts
        mode  (:mode options)
        help  (:help options)
        bibfile (first arguments)
        output-dir (last arguments)]
    (cond
      (true? help) (print-usage summary)
      (nil? bibfile) (print-usage summary ".bib file is required.")
      (nil? output-dir) (print-usage summary "Output directory for generate bib file is required.")
      :otherwise (render bibfile mode output-dir))))
