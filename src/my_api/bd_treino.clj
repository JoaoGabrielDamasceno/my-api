(ns my-api.bd-treino
  (:require [datomic.api :as d]
            [clojure.data.json :as json]))

;; Usando a mesma conexão do banco de usuários
(def db-uri "datomic:dev://localhost:4334/meu-banco")
(def conn (d/connect db-uri))

(def treino-schema
  [{:db/ident :treino/id
    :db/valueType :db.type/uuid
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :treino/exercicio
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :treino/data
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :treino/series
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many}
   {:db/ident :serie/serie
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident :serie/repeticoes
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident :serie/peso
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}])

(defn ensure-treino-schema []
  @(d/transact conn treino-schema))

(defn inserir-treino!
  [{:keys [exercicio data series]}]
  (let [treino-id (d/squuid)
        series-data (mapv (fn [{:keys [serie repeticoes peso]}]
                            {:db/id (d/tempid :db.part/user)
                             :serie/serie serie
                             :serie/repeticoes repeticoes
                             :serie/peso peso})
                          series)
        treino-data {:db/id (d/tempid :db.part/user)
                     :treino/id treino-id
                     :treino/exercicio exercicio
                     :treino/data data
                     :treino/series (mapv :db/id series-data)}]
    @(d/transact conn (conj series-data treino-data))))

(defn listar-treinos []
  (let [db (d/db conn)]
    (d/q '[:find ?id ?exercicio ?data ?series
           :where
           [?e :treino/id ?id]
           [?e :treino/exercicio ?exercicio]
           [?e :treino/data ?data]
           [?e :treino/series ?series]]
         db)))

(defn buscar-treino-por-id [treino-id]
  (let [db (d/db conn)]
    (d/q '[:find ?exercicio ?data ?series
           :in $ ?treino-id
           :where
           [?e :treino/id ?treino-id]
           [?e :treino/exercicio ?exercicio]
           [?e :treino/data ?data]
           [?e :treino/series ?series]]
         db treino-id)))

(defn buscar-treinos-por-data [data]
  (let [db (d/db conn)]
    (d/q '[:find ?id ?exercicio ?series
           :in $ ?data
           :where
           [?e :treino/id ?id]
           [?e :treino/exercicio ?exercicio]
           [?e :treino/data ?data]
           [?e :treino/series ?series]]
         db data)))

(defn buscar-serie-por-id [db serie-id]
  (let [ent (d/entity db serie-id)]
    {:serie (:serie/serie ent)
     :repeticoes (:serie/repeticoes ent)
     :peso (:serie/peso ent)}))

(defn get-conn-db []
  (d/db conn)) 