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
          [:link {:href "css/site.css"
                  :rel "stylesheet"
                  :type "text/css"}]
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

(defn edit-title [req res raise]
  (let [{:keys [id title]} (reader/read-string (request/body-string req))]
    (swap! *todos assoc-in [id :title] title)
    (todos req res raise)))

(defn delete-todo [req res raise]
  (let [id (reader/read-string (request/body-string req))]
    (swap! *todos dissoc id)
    (todos req res raise)))

(defn toggle-todo [req res raise]
  (let [id (reader/read-string (get-in req [:params :id]))]
    (swap! *todos update-in [id :done] not)
    (todos req res raise)))

(defn mmap [m f a] (->> m (f a) (into (empty m))))
(defn complete-all [req res raise]
  (let [complete (some? (reader/read-string (request/body-string req)))]
    (swap! *todos mmap map #(assoc-in % [1 :done] complete))
    (todos req res raise)))


(defn clear-done [req res raise]
  (swap! *todos mmap remove #(get-in % [1 :done]))
  (todos req res raise))

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
        "todos" {:get todos}
        "edit" {:put edit-title}
        "add" {:post new-todo}
        "delete" {:put delete-todo}
        "toggle" {:put toggle-todo}
        "complete-all" {:put complete-all}
        "clear-done" {:put clear-done}}])

(defn router [req res raise]
  (if-let [{:keys [handler route-params]} (bidi/match-route* routes (:uri req) req)]
    (handler (assoc req :route-params route-params) res raise)
    (not-found req res raise)))
