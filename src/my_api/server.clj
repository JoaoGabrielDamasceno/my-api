(ns my-api.server
  (:require [io.pedestal.http :as http]
            [my-api.service :as service]))

(defonce runnable-service (http/create-server service/service))

(defn -main [& _args]
  (http/start runnable-service))
