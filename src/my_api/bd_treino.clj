(ns my-api.bd-treino
  (:require [datomic.api :as d]))

(def treino-schema
  [;; Atributo para nome do exercício
   {:db/ident       :treino/exercicio
    :db/valueType   :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Chave com nome do exercício"}

   ;; Atributo para a data do treino
   {:db/ident       :treino/data
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Data do treino"}

   ;; Atributo que representa as séries: um conjunto de entidades relacionadas
   ;; Usamos cardinalidade many para representar várias séries para um treino
   {:db/ident       :treino/series
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc         "Referências para entidades das séries algoz relacionadas ao treino"}

   ;; Schema para cada série do treino (entidade própria):
   ;; número da série
   {:db/ident       :serie/numero
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc         "Número da série"}

   ;; quantidade de repetições
   {:db/ident       :serie/repeticoes
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc         "Quantidade de repetições da série"}

   ;; peso utilizado
   {:db/ident       :serie/peso
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc         "Peso utilizado na série"}])

;; Usando a mesma conexão do banco de usuários
(def db-uri "datomic:dev://localhost:4334/meu-banco")
(def conn (d/connect db-uri))
(defn ensure-treino-schema [] @(d/transact conn treino-schema))
(defn db-hist [] (d/history (d/db conn)))


(defn inserir-treino!
  "Insere um treino com múltiplas séries.
  Exemplo de uso:
  (inserir-treino! \"Supino\" #inst \"2024-06-01\" [{:numero 1 :repeticoes 10 :peso 60.0}
                                                   {:numero 2 :repeticoes 8 :peso 65.0}])"
  [{:keys [exercicio data series]}]
  (let [series-tx (mapv (fn [{:keys [numero repeticoes peso]}]
                          {:db/id (d/tempid :db.part/user)
                           :serie/numero numero
                           :serie/repeticoes repeticoes
                           :serie/peso peso}) series)
        treino-tx {:treino/exercicio exercicio
                   :treino/data data
                   :treino/series (mapv #(select-keys % [:db/id]) series-tx)}]
    @(d/transact conn (concat series-tx [treino-tx]))))

(defn listar-treinos-por-exercicio
  "Retorna o histórico de todas as tuplas de treinos de um exercício específico.
  Exemplo de uso: (listar-treinos-por-exercicio :supino-reto)"
  [exercicio]
  (vec (d/q '[:find ?data ?serie-num ?serie-rep ?serie-peso ?tx ?op
              :in $ ?exercicio
              :where
              [?t :treino/exercicio ?exercicio ?tx ?op]
              [?t :treino/data ?data ?tx ?op]
              [?t :treino/series ?serie ?tx ?op]
              [?serie :serie/numero ?serie-num ?tx ?op]
              [?serie :serie/repeticoes ?serie-rep ?tx ?op]
              [?serie :serie/peso ?serie-peso ?tx ?op]]
            (db-hist) exercicio)))
