(ns cljs-neocons.transaction
  (:require (cljs-neocons.rest :as nrest)))


(defn statement
  "Populates a Cypher statement to be sent as part of a transaction."
  ([query]
   {:query query :parameters nil})
  ([query parameters]
   {:query query :parameters parameters})
  ([query parameters result-data-contents]
   {:query query :parameters parameters :result-data-contents result-data-contents}))


(defn tx-statement-from
  [{:keys [query parameters result-data-contents] :as m}]
  (into {:statement query :parameters parameters}
        (when (contains? m :result-data-contents)
          {:resultDataContents result-data-contents})))

(defn tx-payload-from
  [xs]
  {:statements (filter :statement (map tx-statement-from xs))} )


(defn- make-request
  [conn xs uri]
  (nrest/post conn uri (tx-payload-from xs)))


(defn in-transaction
  [connection & coll]
  (let [uri    (str (get-in connection [:body :transaction]) "/commit")]
    (make-request connection coll uri)))
