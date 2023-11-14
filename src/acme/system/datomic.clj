(ns acme.system.datomic
  (:require [datomic.api :as d]
            [integrant.core :as ig]))

(defonce *conn
  (atom nil))

(defn conn []
  @*conn)

(defn db []
  (d/db (conn)))

(defn set-connection! [uri]
  (reset! *conn (d/connect uri)))

(def schema-test
  [{:db/id (d/tempid :db.part/db)
    :db/ident :group/number
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Group number"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :group/total
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Number of animals in the group"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :group/type
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Type of cow in the group"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :group/dim
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Days in milk production for the cow group"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :group/kg-fed
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Amount of kg of food fed to the cow group"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :group/kg-left
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Amount of kg food left over for the cow group"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :group/dm-intake
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Dry materials intake for the cow group"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :milk/price
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Price of milk"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :feed/in-mixer
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Amount of feed in the mixer (kg)"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :feed/num-mixes
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Number of mixes made per day"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :feed/mixture-cost
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Feed mixture cost (R/kg dry materials)"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :feed/tgr
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Dry matter percentage formulated TGR"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :milk/butterfat-percent
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Butterfat percentage in milk"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :milk/milk-protein-percent
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Milk protein percentage in milk"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :milk/tank-milk
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Tank milk (L)"}
   {:db/id (d/tempid :db.part/db)
    :db/ident :milk/other-milk
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Other milk used for feeding calfs or house use (L)"}])

(defn create-schema []
  (d/transact (conn) schema))

(defmethod ig/init-key :acme.system/datomic [_ {:keys [uri]}]
  (d/create-database uri)
  (let [conn (set-connection! uri)]
    (create-schema)
    {:connection conn
     :uri        uri}))

(d/transact (conn) [{:group/number 110}
                    {:milk/other-milk 1.2}])

(d/q '[:find ?group-number
       :where
       [?entity :group/number ?group-number]]
     (db))
(d/q '[:find ?other-milk
       :where
       [?entity :milk/other-milk ?other-milk]]
     (db))
(comment
;;this works :)
  (defn create-schema []
    (d/transact (conn)
                [{:db/id (d/tempid :db.part/db)
                  :db/ident :person/name
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one}]))
  (d/transact (conn) [{:person/name "John Doe"}]))
