(ns cljs-neocons.rest
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<! chan close! put! >!]]))


(def headers {"content-type" "application/json; charset=UTF-8"})


(defn post
  [conn uri params]
  (http/post uri {:with-credentials? false
                  :header headers
                  :json-params params}))


(defn connect
  [uri]
  (let [out (chan)]
    (go (let [response   (<! (http/get uri {:with-credentials? false
                                            :headers headers}))]
          (>! out response)))
    out))
