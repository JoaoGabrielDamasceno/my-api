(ns my-api.service
  (:require [io.pedestal.http :as http]
            [my-api.bd-user :as db]
            [io.pedestal.http.body-params :as body-params]))

(defn user-internal->out [[id _ name age]]
{:id id :name name :age age})

(db/ensure-schema)

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
     :route-name :users]})

(def service
  {:env :prod
   ::http/routes routes
   ::http/type :jetty
   ::http/port 8080})
