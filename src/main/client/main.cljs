(ns client.main
  (:require [cljsjs.react]
            [cljsjs.react.dom]
            [reagent.core :as reagent :refer [atom]]
            [reagent.dom :as rdom]))

(defn home-page []
  [:div
   [:h1 "Hello World"]])

(defn ^:dev/after-load start
  []
  (rdom/render [home-page]
               (.getElementById js/document "app")))

(defn ^:export main []
  (start))
