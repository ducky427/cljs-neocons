(ns cljs-neocons.rest
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [<! chan close! put! >!]]
            [cljs-http.client :as client]
            [goog.string :as gstring]
            [goog.json :as goog-json])
  (:import [goog.string StringBuffer]))


(def node? (= cljs.core/*target* "nodejs"))


(when node?
  (def http (js/require "http"))
  (def url (js/require "url")))


(def headers {"content-type" "application/json; charset=UTF-8"
              "X-Stream" true})

(defn- check-json
  [headers data]
  (if (gstring/caseInsensitiveContains (aget headers "content-type") "application/json")
    (js->clj (goog-json/parse data) :keywordize-keys true)
    data))


(if node?
  (defn- internal-get
    [uri]
    (let [out    (chan)
          raw    (.parse url uri)
          r      (StringBuffer.)]
      (.get http (clj->js {:hostname (aget raw "hostname")
                           :path (aget raw "path")
                           :port (aget raw "port")
                           :protocol (aget raw "protocol")
                           :headers headers})
            (fn [res]
              (.on res "data" #(.append r %))
              (.on res "end" (fn [] (put! out {:status (aget res "statusCode")
                                               :success true
                                               :body (check-json (.-headers res)
                                                                 (.toString r))})))))
      out))

  (defn- internal-get
    [uri]
    (client/get uri {:with-credentials? false
                     :headers headers})))


(if node?
  (defn- internal-post
    [uri params]
    (let [out    (chan)
          raw    (.parse url uri)
          r      (StringBuffer.)
          req    (.request http (clj->js {:hostname (aget raw "hostname")
                                          :path (aget raw "path")
                                          :port (aget raw "port")
                                          :protocol (aget raw "protocol")
                                          :headers (assoc headers "Content-Type" "application/json")
                                          :method "POST"})
                           (fn [res]
                             (.on res "data" #(.append r %))
                             (.on res "end" (fn []
                                              (put! out {:status (aget res "statusCode")
                                                         :success true
                                                         :body (check-json (.-headers res)
                                                                           (.toString r))})))))]
      (.write req (goog-json/serialize (clj->js params)))
      (.end req)
      out))


  (defn- internal-post
    [uri params]
    (client/post uri {:with-credentials? false
                      :header headers
                      :json-params params})))


(defn post
  [conn uri params]
  (internal-post uri params))



#_(defn connect
  [uri]
  (let [out (chan)]
    (go (let [response   (<! (internal-get uri {:with-credentials? false
                                                :headers headers}))]
          (>! out response)))
    out))


(defn connect
  [uri]
  (internal-get uri))
