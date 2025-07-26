(ns my-api.service
  (:require [io.pedestal.http :as http]
            [my-api.bd-user :as db]
            [my-api.bd-treino :as treino-db]
            [io.pedestal.http.body-params :as body-params]))

(defn user-internal->out [[id _ name age]]
{:id id :name name :age age})

(db/ensure-schema)
(treino-db/ensure-treino-schema)

(def common-interceptors
  [(body-params/body-params)])

(defn home-page [_request]
  {:status 200 :body "Hello, Pedestal!"})

(defn greet-page [_request]
  {:status 200 :body "Hello, Joao!"})

(defn create-user-handler [request]
    (let [user (:edn-params request)]
    (db/inserir-usuario! user)
    {:status 200 :body (str "Usuário cadastrado: " user)}))

(defn all-users-handler [_request]
  (let [users (db/listar-usuarios)
        response (map user-internal->out users)]
  {:status 200 :body response}))

;; Handlers para treinos
(defn create-treino-handler [request]
  (let [treino (:edn-params request)]
    (treino-db/inserir-treino! treino)
    {:status 200 :body (str "Treino cadastrado: " treino)}))

;; Novo handler para exibir o histórico de um exercício
(defn exibir-exercicio-handler [request]
  (let [exercicio (-> request :path-params :exercicio keyword)
        treinos (treino-db/listar-exercicios-por-nome exercicio)]
    {:status 200 :body treinos}))

(defn exibir-treino-handler [request]
  (let [data (-> request :path-params :data)
        treino (treino-db/listar-todos-exercicios-por-data data)]
    {:status 200 :body treino}))

(def routes
  #{["/" 
     :get home-page 
     :route-name :home]
    
    ["/greet" 
     :get greet-page 
     :route-name :greet]
    
    ["/create-user" 
     :post (conj common-interceptors create-user-handler)
     :route-name :create-user]
    
    ["/users"
     :get all-users-handler
     :route-name :users]
    
    ["/create-treino" 
     :post (conj common-interceptors create-treino-handler)
     :route-name :create-treino]
    
    ["/exibir-exercicio/:exercicio"
     :get (conj common-interceptors exibir-exercicio-handler)
     :route-name :exibir-exercicio]
    
    ["/exibir-treino/:data"
     :get (conj common-interceptors exibir-treino-handler)
     :route-name :exibir-treino]})

(def service
  {:env :prod
   ::http/routes routes
   ::http/type :jetty
   ::http/port 8080})
