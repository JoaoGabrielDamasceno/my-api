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

(defn ensure-vector [x]
  (if (sequential? x) x [x]))

(defn all-treinos-handler [_request]
  (let [db (treino-db/get-conn-db)
        treinos (treino-db/listar-treinos)
        response (map (fn [[id exercicio data series-ids]]
                       {:id id
                        :exercicio exercicio
                        :data data
                        :series (mapv #(treino-db/buscar-serie-por-id db %) (ensure-vector series-ids))})
                     treinos)]
    {:status 200 :body response}))

(defn treino-por-id-handler [request]
  (let [db (treino-db/get-conn-db)
        treino-id (java.util.UUID/fromString (get-in request [:path-params :id]))
        treinos (treino-db/buscar-treino-por-id treino-id)
        response (map (fn [[exercicio data series-ids]]
                       {:exercicio exercicio
                        :data data
                        :series (mapv #(treino-db/buscar-serie-por-id db %) (ensure-vector series-ids))})
                     treinos)]
    {:status 200 :body response}))

(defn treinos-por-data-handler [request]
  (let [db (treino-db/get-conn-db)
        data-str (get-in request [:path-params :data])
        treinos (treino-db/buscar-treinos-por-data data-str)
        response (map (fn [[id exercicio series-ids]]
                       {:id id
                        :exercicio exercicio
                        :series (mapv #(treino-db/buscar-serie-por-id db %) (ensure-vector series-ids))})
                     treinos)]
    {:status 200 :body response}))


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
    
    ["/treinos"
     :get all-treinos-handler
     :route-name :treinos]
    
    ["/treino/:id"
     :get treino-por-id-handler
     :route-name :treino-por-id]
    
    ["/treinos/data/:data"
     :get treinos-por-data-handler
     :route-name :treinos-por-data]})

(def service
  {:env :prod
   ::http/routes routes
   ::http/type :jetty
   ::http/port 8080})
