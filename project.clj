(defproject intuitionistic "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clojure-csv "2.0.1"]
                 [com.github.yannrichet/JMathPlot "1.0.1"]
                 [incanter "1.9.0"]
                 [org.apfloat/apfloat "1.6.3"]]
  :main ^:skip-aot intuitionistic.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :java-source-paths ["src/java"])
