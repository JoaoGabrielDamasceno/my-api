(ns my-api.bd-user
  (:require [datomic.api :as d]))

;; Usando banco em memória para desenvolvimento
(def db-uri "datomic:dev://localhost:4334/meu-banco")

(d/create-database db-uri)

(def conn (d/connect db-uri))

(def user-schema
  [{:db/ident :user/id
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :user/password
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :user/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :user/age
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}])

(defn ensure-schema []
  @(d/transact conn user-schema))

(defn inserir-usuario! 
  [{:keys [id password name age]}]
  @(d/transact conn [{:user/id id :user/password password :user/name name :user/age age}]))

(defn listar-usuarios []
  (let [db (d/db conn)]
    (set (d/q '[:find ?id ?password ?name ?age
           :where
           [?e :user/id ?id]
           [?e :user/password ?password]
           [?e :user/name ?name]
           [?e :user/age ?age]]
         db))))