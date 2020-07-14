(ns server.routes
  (:require [bidi.bidi :as bidi]
            [cljs.reader :as reader]
            [hiccups.runtime]
            [macchiato.middleware.anti-forgery :as af]
            [macchiato.util.response :as r]
            [macchiato.util.request :as request])
  (:require-macros
   [hiccups.core :refer [html]]))

(defonce *todos (atom {}))
(defonce *id (atom 0))

(defn home [req res raise]
  (let [af-token af/*anti-forgery-token*]
    (-> [:html
         [:head
          [:meta {:name "viewport"
                  :content "minimum-scale=1, initial-scale=1, width=device-width"}]]
         [:body
          [:div#app
           [:h2 "Loading Radio Controller..."]
           [:script "var antiForgeryToken = '" af-token "'"]
           [:script {:src "/js/client.js"}]
           [:script "client.main.main()"]]]]
        (html)
        (r/ok)
        (r/content-type "text/html")
        (res))))

(defn todos [req res raise]
  (-> @*todos
      (r/ok)
      (r/transit)
      (res)))

(defn new-todo [req res raise]
  (let [text (:title (reader/read-string (request/body-string req)))]
    (swap! *id inc)
    (swap! *todos assoc @*id {:id @*id :title text :done false})
    (todos req res raise)))

(defn not-found [req res raise]
  (-> (html
       [:html
        [:body
         [:h2 (:uri req) " was not found"]]])
      (r/not-found)
      (r/content-type "text/html")
      (res)))

(def routes
  ["/" {"" {:get home}
        "todos" {:get todos
                 :post new-todo}}])

(defn router [req res raise]
  (if-let [{:keys [handler route-params]} (bidi/match-route* routes (:uri req) req)]
    (handler (assoc req :route-params route-params) res raise)
    (not-found req res raise)))
