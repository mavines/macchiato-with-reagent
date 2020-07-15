(ns client.main
  (:require [cljsjs.react]
            [cljsjs.react.dom]
            [cljs.reader :as reader]
            [clojure.string :as string]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [reagent.core :as r :refer [atom]]
            [reagent.dom :as rdom])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce *todos (r/atom (sorted-map)))

(defn add-af-token [data]
  (assoc-in data [:headers "X-CSRF-token"] js/antiForgeryToken))

(defn af-post [url data]
  (http/post url (add-af-token data)))

(defn af-put [url data]
  (http/put url (add-af-token data)))

(defn af-delete [url data]
  (http/delete url (add-af-token data)))

(defn parse-todos [response]
  (:body response))

(defn add-todo [text]
  (go (let [response (<! (af-post "/add" {:body {:title text}}))
            updated-todos (parse-todos response)]
        (reset! *todos updated-todos))))

(defn toggle [id]
  (go (let [response (<! (af-put (str "/toggle?id=" id) {}))
            updated-todos (parse-todos response)]
        (reset! *todos updated-todos))))


(defn save [id title]
  (go (let [response (<! (af-put "/edit" {:body {:id id :title title}}))
            updated-todos (parse-todos response)]
        (reset! *todos updated-todos))))

(defn delete [id]
  (go (let [response (<! (af-put "/delete" {:body id}))
            updated-todos (parse-todos response)]
        (reset! *todos updated-todos))))

(defn mmap [m f a] (->> m (f a) (into (empty m))))
(defn complete-all [v]
  (go (let [response (<! (af-put "/complete-all" {:body v}))
            updated-todos (parse-todos response)]
        (reset! *todos updated-todos))))

(defn clear-done []
  (go (let [response (<! (af-put "/clear-done" {}))
            updated-todos (parse-todos response)]
        (reset! *todos updated-todos))))

(defonce init (do
                (add-todo "Rename Cloact to Reagent")
                (add-todo "Add undo demo")
                (add-todo "Make all rendering async")
                (add-todo "Allow any arguments to component functions")
                (complete-all true)))

(defn todo-input [{:keys [title on-save on-stop]}]
  (let [val (r/atom title)
        stop #(do (reset! val "")
                  (if on-stop (on-stop)))
        save #(let [v (-> @val str string/trim)]
                (if-not (empty? v) (on-save v))
                (stop))]
    (fn [{:keys [id class placeholder]}]
      [:input {:type "text" :value @val
               :id id :class class :placeholder placeholder
               :on-blur save
               :on-change #(reset! val (-> % .-target .-value))
               :on-key-down #(case (.-which %)
                               13 (save)
                               27 (stop)
                               nil)}])))

(def todo-edit (with-meta todo-input
                 {:component-did-mount #(.focus (rdom/dom-node %))}))

(defn todo-stats [{:keys [filt active done]}]
  (let [props-for (fn [name]
                    {:class (if (= name @filt) "selected")
                     :on-click #(reset! filt name)})]
    [:div
     [:span#todo-count
      [:strong active] " " (case active 1 "item" "items") " left"]
     [:ul#filters
      [:li [:a (props-for :all) "All"]]
      [:li [:a (props-for :active) "Active"]]
      [:li [:a (props-for :done) "Completed"]]]
     (when (pos? done)
       [:button#clear-completed {:on-click clear-done}
        "Clear completed " done])]))

(defn todo-item []
  (let [editing (r/atom false)]
    (fn [{:keys [id done title]}]
      [:li {:class (str (if done "completed ")
                        (if @editing "editing"))}
       [:div.view
        [:input.toggle {:type "checkbox"
                        :checked done
                        :value done
                        :on-change #(toggle id)}]
        [:label {:on-double-click #(reset! editing true)} title]
        [:button.destroy {:on-click #(delete id)} ""]]
       (when @editing
         [todo-edit {:class "edit" :title title
                     :on-save #(save id %)
                     :on-stop #(reset! editing false)}])])))

(defn todo-app [props]
  (let [filt (r/atom :all)]
    (fn []
      (let [items (vals @*todos)
            done (->> items (filter :done) count)
            active (- (count items) done)]
        [:div
         [:section#todoapp
          [:header#header
           [:h1 "todos"]
           [todo-input {:id "new-todo"
                        :placeholder "What needs to be done?"
                        :on-save add-todo}]]
          (when (-> items count pos?)
            [:div
             [:section#main
              [:input#toggle-all {:type "checkbox"
                                  :checked (zero? active)
                                  :value (zero? active)
                                  :on-change #(complete-all (pos? active))}]
              [:label {:for "toggle-all"} "Mark all as complete"]
              [:ul#todo-list
               (for [todo (filter (case @filt
                                    :active (complement :done)
                                    :done :done
                                    :all identity) items)]
                 ^{:key (:id todo)} [todo-item todo])]]
             [:footer#footer
              [todo-stats {:active active :done done :filt filt}]]])]
         [:footer#info
                    [:p "Double-click to edit a todo"]]]))))

(defn ^:dev/after-load start
  []
  (rdom/render [todo-app]
               (.getElementById js/document "app")))

(defn ^:export main []
  (start))
