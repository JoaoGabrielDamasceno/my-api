(ns my-api.bd-exercicio
  (:require [clojure.string :as str]
            [datomic.api :as d]))

(def exercicio-schema
  [;; Identificador único do exercício
   {:db/ident       :exercicio/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "ID único do exercício"}

   ;; Nome de exibição do exercício
   {:db/ident       :exercicio/nome
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Nome de exibição do exercício"}

   ;; Nome interno único (slug/keyword)
   {:db/ident       :exercicio/nome-interno
    :db/valueType   :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Nome interno único do exercício"}

   ;; Categoria (ex.: perna, peito, costas)
   {:db/ident       :exercicio/categoria
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Categoria do exercício"}])

(def db-uri "datomic:dev://localhost:4334/meu-banco")
(def conn (d/connect db-uri))

(defn ensure-exercicio-schema []
  @(d/transact conn exercicio-schema))

(defn- gerar-id-exercicio []
  (java.util.UUID/randomUUID))

(defn- coerce-nome-interno [value]
  (if (string? value)
    (-> value str/trim str/lower-case (str/replace #"\s+" "-") keyword)
    (throw (ex-info "nome-interno inválido (esperado string)" {:valor value}))))

(defn inserir-exercicio!
  "Insere um exercício. Aceita :nome (string) e :categoria (string). Retorna dados essenciais cadastrados."
  [{:keys [nome categoria]}]
  (let [nome-interno-kw (coerce-nome-interno nome)
        id (gerar-id-exercicio)
        tx {:exercicio/id id
            :exercicio/nome nome
            :exercicio/nome-interno nome-interno-kw
            :exercicio/categoria categoria}]
    @(d/transact conn [tx])
    {:id id :nome nome :nome-interno nome-interno-kw :categoria categoria}))

(defn listar-exercicios
  "Retorna vetor de mapas contendo :nome, :nome-interno e :categoria para todos os exercícios."
  []
  (->> (d/q '[:find ?nome ?nome-interno ?categoria
              :where
              [?e :exercicio/nome ?nome]
              [?e :exercicio/nome-interno ?nome-interno]
              [?e :exercicio/categoria ?categoria]]
            (d/db conn))
       (map (fn [[nome nome-interno categoria]] 
              {:nome (str/capitalize nome)
               :nome-interno (name nome-interno)
               :categoria categoria}))
       (sort-by :nome)
       (vec)))


(defn listar-categorias
  "Retorna lista de todas as categorias distintas."
  []
  (->> (d/q '[:find ?categoria
              :where
              [?e :exercicio/categoria ?categoria]]
            (d/db conn))
       (map first)
       (distinct)
       (sort)
       (vec)))

