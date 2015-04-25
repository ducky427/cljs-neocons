(ns cljs-neocons.transaction-test
  (:require-macros [cemerick.cljs.test :refer (is deftest done testing)]
                   [cljs.core.async.macros :refer [go]])
  (:require [cemerick.cljs.test :as t]
            [cljs.core.async :as async :refer [<! chan close! put!]]
            [cljs-neocons.rest :as nr]
            [cljs-neocons.transaction :as tx]))


(deftest ^:async test-connect
  (go
    (let [conn    (<! (nr/connect "http://localhost:7474/db/data/"))
          resp    (<! (tx/in-transaction
                       conn
                       (tx/statement "CREATE (n {props}) RETURN n" {:props {:name "My Node"}})
                       (tx/statement "CREATE (n {props}) RETURN n" {:props {:name "My Another Node"}})))
          result (get-in resp [:body :results])]
      (is (= (count result) 2))
      (is (= (:data (first result)) [{:row [{:name "My Node"}]}]))
      (is (= (:columns (first result)) ["n"]))
      (is (= (:data (second result)) [{:row [{:name "My Another Node"}]}]))
      (is (= (:columns (second result)) ["n"]))
      (done))))
