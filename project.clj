(defproject lebib "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories  [["jitpack" "https://jitpack.io"]]
  :dependencies [[clj-tagsoup "0.3.0"  :exclusions  [org.clojure/clojure]]
                 [com.github.jbibtex/jbibtex "-SNAPSHOT"]
                 [hiccup "1.0.5"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]]
  :main ^:skip-aot lebib.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
