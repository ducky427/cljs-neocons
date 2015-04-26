(defproject cljs-neocons "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3211" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [cljs-http "0.1.30"]]
  :plugins [[com.cemerick/clojurescript.test "0.3.3"]
            [lein-cljsbuild "1.0.5"]
            [lein-ancient "0.6.7"]]
  :aliases {"test" ["do" "clean," "cljsbuild" "test"]}
  :cljsbuild {:builds [{:compiler {:output-to "target/browser-testable.js"
                                   :optimizations :simple
                                   :pretty-print true}
                        :source-paths ["test"]}
                       {:compiler {:output-to "target/node-testable.js"
                                   :optimizations :simple
                                   :target :nodejs
                                   :hashbang false
                                   :pretty-print true}
                        :source-paths ["test"]}]
              :test-commands {"nodejs" ["node" :node-runner "target/node-testable.js"]
                              "phantom" ["phantomjs" :runner "target/browser-testable.js"]}})
