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

(def schema
  [{:db/ident :group/number
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Group number"}
   {:db/ident :group/total
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Number of animals in the group"}
   {:db/ident :group/type
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Type of cow in the group"}
   {:db/ident :group/dim
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Days in milk production for the cow group"}
   {:db/ident :group/kg-fed
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Amount of kg of food fed to the cow group"}
   {:db/ident :group/kg-left
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Amount of kg food left over for the cow group"}
   {:db/ident :group/dm-intake
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Dry materials intake for the cow group"}
   {:db/ident :group/milk
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Reference to milk entity"}
   {:db/ident :milk/price
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Price of milk"}
   {:db/ident :group/feed
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Reference to feed entity"}
   {:db/ident :feed/in-mixer
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Amount of feed in the mixer (kg)"}
   {:db/ident :feed/num-mixes
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Number of mixes made per day"}
   {:db/ident :feed/mixture-cost
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Feed mixture cost (R/kg dry materials)"}
   {:db/ident :feed/tgr
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Dry matter percentage formulated TGR"}
   {:db/ident :milk/butterfat-percent
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Butterfat percentage in milk"}
   {:db/ident :milk/milk-protein-percent
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Milk protein percentage in milk"}
   {:db/ident :milk/tank-milk
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "Tank milk (L)"}
   {:db/ident :milk/other-milk
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

(defn create-data [num]
  (->> (for [n (range num)]
         (let [feed (rand 1000)]
           [{:db/id                     (str "feed-entity-id-" n)
             :feed/in-mixer             (+ 100 feed)
             :feed/num-mixes            (rand-int 5)
             :feed/tgr                  (rand 100)}
            {:db/id                     (str "milk-entity-id-" n)
             :milk/price                (rand 20)
             :milk/butterfat-percent    (rand 100)
             :milk/milk-protein-percent (rand 100)
             :milk/tank-milk            (rand 1000)
             :milk/other-milk           (rand 100)}
            {:db/id                     (str "group-entity-id-" n)
             :group/milk (str "milk-entity-id-" n)
             :group/feed (str "feed-entity-id-" n)
             :group/number              n
             :group/total               (rand-int 100)
             :group/type                (str "type" n)
             :group/dim                 (rand-int 50)
             :group/kg-fed              (double (+ 200 feed))
             :group/kg-left             (double (+ 50 feed))
             :group/dm-intake           (rand 20)}]))
       (apply concat)
       vec))

(create-data 2)

(comment

  (d/transact (conn) (create-data 10))
  (d/q '[:find ?feed-in-mixer
         :in $ ?group-number
         :where
         [?group-entity :group/number ?group-number]
         [?group-entity :group/feed ?feed-id]
         [?feed-id :feed/in-mixer ?feed-in-mixer]]
       (db) 0)
  (d/q '[:find ?group-number
         :where
         [?entity :group/number ?group-number]]
       (db))
  (d/q '[:find ?feed-in-mixer
         :in $ ?number
         :where
         [?entity :group/number ?number]
         [?entity :group/feed ?id]
         [?id :feed/in-mixer ?feed-in-mixer]]
       (db) 0)

;;this works :)
  (defn create-schema []
    (d/transact (conn)
                [{:db/id (d/tempid :db.part/db)
                  :db/ident :person/name
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one}]))

  (d/transact (conn) [{:person/name "John Doe"}]))
