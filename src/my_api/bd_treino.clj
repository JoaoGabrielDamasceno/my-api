(ns my-api.bd-treino
  (:require [datomic.api :as d]))

(def treino-schema
  [;; ID único do treino
   {:db/ident       :treino/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "ID único do treino"}

   ;; Atributo para nome do exercício
   {:db/ident       :treino/exercicio
    :db/valueType   :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/doc         "Nome do exercício"}

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

(defn gerar-id-treino
  "Gera um novo UUID para usar como ID do treino"
  []
  (java.util.UUID/randomUUID))

(defn buscar-treino-por-id
  "Busca um treino específico pelo seu ID"
  [id]
  (d/q '[:find ?exercicio ?data ?series
         :in $ ?id
         :where
         [?t :treino/id ?id]
         [?t :treino/exercicio ?exercicio]
         [?t :treino/data ?data]
         [?t :treino/series ?series]]
       (d/db conn) id))

(defn inserir-treino!
  "Insere um treino com múltiplas séries.
  Exemplo de uso:
  (inserir-treino! {:exercicio :supino-reto
                    :data \"2024-06-01\"
                    :series [{:numero 1 :repeticoes 10 :peso 60}
                             {:numero 2 :repeticoes 8 :peso 65}]})"
  [{:keys [exercicio data series]}]
  (let [id (gerar-id-treino)
        series-tx (mapv (fn [{:keys [numero repeticoes peso]}]
                          {:db/id (d/tempid :db.part/user)
                           :serie/numero numero
                           :serie/repeticoes repeticoes
                           :serie/peso peso}) series)
        treino-tx {:treino/id id
                   :treino/exercicio exercicio
                   :treino/data data
                   :treino/series (mapv #(select-keys % [:db/id]) series-tx)}]
    @(d/transact conn (concat series-tx [treino-tx]))))

(defn processar-treinos-por-exercicio
  [exercicios]
  (->> exercicios
       (group-by first)
       (map (fn [[data treinos-data]]
              {:data data
               :series (mapv (fn [[_ numero repeticoes peso]]
                               {:numero numero
                                :repeticoes repeticoes
                                :peso peso})
                             treinos-data)}))
       (sort-by :data)))

(defn listar-treinos-por-exercicio
  "Retorna todos os treinos atuais de um exercício específico.
  Exemplo de uso: (listar-treinos-por-exercicio :supino-reto)"
  [ex]
  (-> (d/q '[:find ?data ?serie-num ?serie-rep ?serie-peso
              :in $ ?exercicio
              :where
              [?t :treino/id ?id]
              [?t :treino/exercicio ?exercicio]
              [?t :treino/data ?data]
              [?t :treino/series ?serie]
              [?serie :serie/numero ?serie-num]
              [?serie :serie/repeticoes ?serie-rep]
              [?serie :serie/peso ?serie-peso]]
            (d/db conn) ex)
      (vec)
      (processar-treinos-por-exercicio)))
