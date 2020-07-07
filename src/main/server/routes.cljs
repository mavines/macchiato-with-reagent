(ns server.routes
  (:require [bidi.bidi :as bidi]
            [hiccups.runtime]
            [macchiato.middleware.anti-forgery :as af]
            [macchiato.util.response :as r])
  (:require-macros
   [hiccups.core :refer [html]]))


(defn home [req res raise]
  (-> [:html
       [:head
        [:meta {:name "viewport"
                :content "minimum-scale=1, initial-scale=1, width=device-width"}]]
       [:body
        [:div#app
         [:h2 "Loading Radio Controller..."]
         [:script {:src "/js/client.js"}]
         [:script "client.main.main()"]]]]
      (html)
      (r/ok)
      (r/content-type "text/html")
      (res)))

(defn not-found [req res raise]
  (-> (html
       [:html
        [:body
         [:h2 (:uri req) " was not found"]]])
      (r/not-found)
      (r/content-type "text/html")
      (res)))

(def routes
  ["/" {"" {:get home}}])

(defn router [req res raise]
  (if-let [{:keys [handler route-params]} (bidi/match-route* routes (:uri req) req)]
    (handler (assoc req :route-params route-params) res raise)
    (not-found req res raise)))
