(ns my-api.service
  (:require [io.pedestal.http :as http]
            [my-api.bd-user :as db]
            [my-api.bd-treino :as treino-db]
            [my-api.bd-exercicio :as exercicio-db]
            [my-api.migrations :as migrations]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.cors :as cors]
            [io.pedestal.http.route :as route]
            [clojure.data.json :as json]))

(defn user-internal->out [[id _ name age]]
{:id id :name name :age age})

;; Garantir schemas e popular dados iniciais
(db/ensure-schema)
(treino-db/ensure-treino-schema)
(exercicio-db/ensure-exercicio-schema)

;; Popular exercícios padrão e criar admin automaticamente na inicialização
(println "\n=== Inicializando banco de dados ===")
(migrations/popular-exercicios!)
(db/criar-admin-padrao!)
(println "=== Inicialização concluída ===\n")

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
  (let [user (or (:edn-params request) (:json-params request))]
    (try
      (db/inserir-usuario! user)
      {:status 200 
       :headers {"Content-Type" "application/json"}
       :body (json/write-str {:success true 
                              :message "Usuário cadastrado com sucesso"
                              :user {:id (:id user) 
                                     :name (:name user) 
                                     :age (:age user)}})}
      (catch Exception e
        {:status 400
         :headers {"Content-Type" "application/json"}
         :body (json/write-str {:success false 
                                :message (str "Erro ao cadastrar usuário: " (.getMessage e))})}))))

(defn login-handler [request]
  (let [credentials (or (:edn-params request) (:json-params request))
        user-id (:id credentials)
        password (:password credentials)
        user (db/validar-login user-id password)]
    (if user
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/write-str {:success true
                              :user user})}
      {:status 401
       :headers {"Content-Type" "application/json"}
       :body (json/write-str {:success false
                              :message "Credenciais inválidas"})})))

(defn all-users-handler [_request]
  (let [users (db/listar-usuarios)
        response (map user-internal->out users)]
  {:status 200 :body response}))

;; Handlers para treinos
(defn create-treino-handler [request]
  (let [treino-raw (or (:edn-params request) (:json-params request))
        ;; Converter o exercicio de string para keyword se necessário
        exercicio-kw (if (string? (:exercicio treino-raw))
                       (keyword (:exercicio treino-raw))
                       (:exercicio treino-raw))
        treino (assoc treino-raw :exercicio exercicio-kw)]
    (treino-db/inserir-treino! treino)
    {:status 200 :body (str "Treino cadastrado: " treino)}))

;; Handler para cadastro de exercício (apenas admin)
(defn create-exercicio-handler [request]
  (let [exercicio (or (:edn-params request) (:json-params request))
        user-id (get exercicio :userId)]
    (if (and user-id (db/usuario-eh-admin? user-id))
      (do
        (exercicio-db/inserir-exercicio! (dissoc exercicio :userId))
        {:status 200 
         :headers {"Content-Type" "application/json"}
         :body (json/write-str {:success true 
                                :message "Exercício cadastrado com sucesso"})})
      {:status 403
       :headers {"Content-Type" "application/json"}
       :body (json/write-str {:success false 
                              :message "Apenas administradores podem cadastrar exercícios"})})))

;; Handler para listar exercícios
(defn listar-exercicios-handler [_request]
  (let [items (exercicio-db/listar-exercicios)]
    {:status 200 
     :headers {"Content-Type" "application/json"}
     :body (json/write-str items)}))

;; Handler para listar categorias
(defn listar-categorias-handler [_request]
  (let [categorias (exercicio-db/listar-categorias)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str categorias)}))

;; Novo handler para exibir o histórico de um exercício
(defn exibir-exercicio-handler [request]
  (let [exercicio (-> request :path-params :exercicio keyword)
        treinos (treino-db/listar-exercicios-por-nome exercicio)]
    {:status 200 :body treinos}))

(defn exibir-treino-handler [request]
  (let [data (-> request :path-params :data)
        treino (treino-db/listar-todos-exercicios-por-data data)]
    {:status 200 :body treino}))

;; Handler para listar todos os treinos (com filtro opcional por data e userId)
(defn listar-treinos-handler [request]
  (let [query-params (:query-params request)
        params (:params request)
        data-filtro (or (get query-params "data")
                        (get query-params :data)
                        (get params "data")
                        (get params :data))
        user-id (or (get query-params "userId")
                    (get query-params :userId)
                    (get params "userId")
                    (get params :userId))
        treinos (cond
                  (and data-filtro (not (empty? data-filtro)) 
                       user-id (not (empty? user-id)))
                  (treino-db/listar-todos-treinos data-filtro user-id)
                  
                  (and user-id (not (empty? user-id)))
                  (treino-db/listar-todos-treinos nil user-id)
                  
                  (and data-filtro (not (empty? data-filtro)))
                  (treino-db/listar-todos-treinos data-filtro)
                  
                  :else
                  (treino-db/listar-todos-treinos))]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str treinos)}))

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
    
    ["/login"
     :post (conj common-interceptors login-handler)
     :route-name :login]
    
    ["/check-admin/:userId"
     :get (fn [request]
            (let [user-id (-> request :path-params :userId)
                  is-admin (db/usuario-eh-admin? user-id)]
              {:status 200
               :headers {"Content-Type" "application/json"}
               :body (json/write-str {:isAdmin is-admin})}))
     :route-name :check-admin]
    
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
    
    ["/exercicios/categorias"
     :get (conj common-interceptors listar-categorias-handler)
     :route-name :listar-categorias]
    
    ["/treinos"
     :get (conj common-interceptors listar-treinos-handler)
     :route-name :listar-treinos]
    
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
