(ns server.main
  (:require
   [server.middleware :refer [wrap-defaults]]
   [server.routes :refer [router]]
   [macchiato.server :as http]
   [macchiato.env :as config]))

(defn reload! []
  (print "Code Reloaded"))

(defn main! []
  (let [{:keys [host port]} (config/env)]
    (-> (http/start
         {:handler (wrap-defaults router)
          :host host
          :port port
          :websockets? false
          :on-success #(print "Started server")}))))
