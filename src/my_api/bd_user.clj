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
    :db/cardinality :db.cardinality/one}
   {:db/ident :user/admin
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc "Indica se o usuário é administrador"}])

(defn ensure-schema []
  @(d/transact conn user-schema))

(defn inserir-usuario! 
  [{:keys [id password name age admin]}]
  (let [user-data (cond-> {:user/id id 
                            :user/password password 
                            :user/name name 
                            :user/age age}
                    (some? admin) (assoc :user/admin admin))]
    @(d/transact conn [user-data])))

(defn listar-usuarios []
  (let [db (d/db conn)]
    (set (d/q '[:find ?id ?password ?name ?age
           :where
           [?e :user/id ?id]
           [?e :user/password ?password]
           [?e :user/name ?name]
           [?e :user/age ?age]]
         db))))

(defn buscar-usuario-por-id [user-id]
  "Busca um usuário pelo ID"
  (let [db (d/db conn)
        result (first (d/q '[:find ?id ?name ?age ?admin
                             :in $ ?user-id
                             :where
                             [?e :user/id ?user-id]
                             [?e :user/id ?id]
                             [?e :user/name ?name]
                             [?e :user/age ?age]
                             [(get-else $ ?e :user/admin false) ?admin]]
                           db user-id))]
    (when result
      (let [[id name age admin] result]
        {:id id :name name :age age :admin admin}))))

(defn validar-login [user-id password]
  "Valida as credenciais do usuário. Retorna o usuário se válido, nil caso contrário"
  (let [db (d/db conn)
        result (first (d/q '[:find ?id ?password ?name ?age ?admin
                             :in $ ?user-id
                             :where
                             [?e :user/id ?user-id]
                             [?e :user/id ?id]
                             [?e :user/password ?password]
                             [?e :user/name ?name]
                             [?e :user/age ?age]
                             [(get-else $ ?e :user/admin false) ?admin]]
                           db user-id))]
    (when result
      (let [[id stored-password name age admin] result]
        (when (= password stored-password)
          {:id id :name name :age age :admin admin})))))

(defn usuario-eh-admin? [user-id]
  "Verifica se um usuário é administrador"
  (let [db (d/db conn)
        result (first (d/q '[:find ?admin
                             :in $ ?user-id
                             :where
                             [?e :user/id ?user-id]
                             [(get-else $ ?e :user/admin false) ?admin]]
                           db user-id))]
    (if result
      (first result)
      false)))

(defn criar-admin-padrao! []
  "Cria um usuário admin padrão se não existir"
  (let [admin-id "admin"]
    (when-not (buscar-usuario-por-id admin-id)
      (inserir-usuario! {:id admin-id
                         :password "admin123"
                         :name "Administrador"
                         :age 30
                         :admin true})
      (println "✅ Usuário admin criado: admin / admin123"))))