(ns my-api.service
  (:require [io.pedestal.http :as http]
            [my-api.bd-user :as db]
            [my-api.bd-treino :as treino-db]
            [my-api.bd-exercicio :as exercicio-db]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.cors :as cors]
            [clojure.data.json :as json]))

(defn user-internal->out [[id _ name age]]
{:id id :name name :age age})

(db/ensure-schema)
(treino-db/ensure-treino-schema)
(exercicio-db/ensure-exercicio-schema)

(def cors-interceptor
  (cors/allow-origin {:allowed-origins (constantly true)
                      :creds true
                      :methods #{:get :put :post :delete :options}}))

(def common-interceptors
  [cors-interceptor (body-params/body-params)])

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
  (let [treino (or (:edn-params request) (:json-params request))]
    (treino-db/inserir-treino! treino)
    {:status 200 :body (str "Treino cadastrado: " treino)}))

;; Handler para cadastro de exercício
(defn create-exercicio-handler [request]
  (let [exercicio (or (:edn-params request) (:json-params request))]
    (print "exercicio: " request)
    (exercicio-db/inserir-exercicio! exercicio)
    {:status 200 :body (str "Exercício cadastrado: " exercicio)}))

;; Handler para listar exercícios
(defn listar-exercicios-handler [_request]
  (let [items (exercicio-db/listar-exercicios)]
    {:status 200 
     :headers {"Content-Type" "application/json"}
     :body (json/write-str items)}))

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
    
    ["/create-exercicio"
     :post (conj common-interceptors create-exercicio-handler)
     :route-name :create-exercicio]

    ["/exercicios"
     :get (conj common-interceptors listar-exercicios-handler)
     :route-name :listar-exercicios]
    
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
   ::http/port 8080
   ::http/allowed-origins {:creds true :allowed-origins (constantly true)}})
