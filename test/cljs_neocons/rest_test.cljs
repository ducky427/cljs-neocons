(ns cljs-neocons.rest-test
  (:require-macros [cemerick.cljs.test :refer (is deftest done testing)]
                   [cljs.core.async.macros :refer [go]])
  (:require [cemerick.cljs.test :as t]
            [cljs.core.async :as async :refer [<! chan close! put!]]
            [cljs-neocons.rest :as hr]))


(deftest test-simple
  (= 2 (+ 1 1)))


(deftest ^:async test-connect
  (go
    (let [response (<! (hr/connect "http://localhost:7474/db/data/"))]
      (is (= 200 (:status response)))
      (is (= true (:success response)))
      (is (some? (get-in response [:body :neo4j_version])))
      (done))))
