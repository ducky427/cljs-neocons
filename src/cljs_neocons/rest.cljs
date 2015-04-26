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


(defn- handle-response
  [buf out response]
  (.on response "data" #(.append buf %))
  (.on response "end" (fn []
                        (put! out {:status (aget response "statusCode")
                                   :success true
                                   :body (js->clj (goog-json/parse (.toString buf))
                                                  :keywordize-keys true)}))))


(defn- make-options
  [uri]
  (let [raw  (.parse url uri)]
    {:hostname (aget raw "hostname")
     :path     (aget raw "path")
     :port     (aget raw "port")
     :protocol (aget raw "protocol")
     :headers  headers}))


(if node?
  (defn- internal-get
    [uri]
    (let [out    (chan)
          opts   (make-options uri)
          buf    (StringBuffer.)]
      (.get http (clj->js opts)
            (fn [res]
              (handle-response buf out res)))
      out))

  (defn- internal-get
    [uri]
    (client/get uri {:with-credentials? false
                     :headers headers})))


(if node?
  (defn- internal-post
    [uri params]
    (let [out    (chan)
          opts   (-> (make-options uri)
                     (assoc-in [:headers "Content-Type"] "application/json")
                     (assoc :method "POST"))
          buf    (StringBuffer.)
          req    (.request http (clj->js opts)
                           (fn [res]
                             (handle-response buf out res)))]
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
