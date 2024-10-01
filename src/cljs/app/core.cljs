(ns app.core
  (:require ["@mui/material/Button$default" :as Button]
            ["@mui/material/Checkbox$default" :as Checkbox]
            ["@mui/material/Stack$default" :as Stack]
            ["@mui/material/Typography$default" :as Typography]
            ["@mui/material/styles" :refer [ThemeProvider createTheme]]
            [uix.core :as uix :refer [defui $]]
            [uix.dom]
            [app.hooks :as hooks]
            [app.subs]
            [app.handlers]
            [app.fx]
            [app.db]
            [re-frame.core :as rf]))

(defui header []
  ($ :header.app-header
     ($ :img {:src "https://raw.githubusercontent.com/pitch-io/uix/master/logo.png"
              :width 32})))

(defui footer []
  ($ :footer.app-footer
     ($ :small "made with "
        ($ :a {:href "https://github.com/pitch-io/uix"}
           "UIx"))))

(defui text-field [{:keys [on-add-todo]}]
  (let [[value set-value!] (uix/use-state {:foo [{:bar ""}]})]
    ($ :input.text-input
       {:value value
        :placeholder "Add a new todo and hit Enter to save"
        :style {:color "green"}
        :on-change (fn [^js e]
                     (set-value! (.. e -target -value)))
        :on-key-down (fn [^js e]
                       (when (= "Enter" (.-key e))
                         (set-value! "")
                         (on-add-todo {:text value :status :unresolved})))})))

(defui editable-text [{:keys [text text-style on-done-editing]}]
  (let [[editing? set-editing!] (uix/use-state false)
        [editing-value set-editing-value!] (uix/use-state "")]
    (if editing?
      ($ :input.todo-item-text-field
         {:value editing-value
          :auto-focus true
          :on-change (fn [^js e]
                       (set-editing-value! (.. e -target -value)))
          :on-key-down (fn [^js e]
                         (when (= "Enter" (.-key e))
                           (set-editing-value! "")
                           (set-editing! false)
                           (on-done-editing editing-value)))})
      ($ :span.todo-item-text
         {:style text-style
          :on-click (fn [_]
                      (set-editing! true)
                      (set-editing-value! text))}
         text))))

(defui todo-item
  [{:keys [created-at text status on-remove-todo on-set-todo-text]}]
  ($ Stack
     {:direction "row"
      :sx #js {:my 1
               :mx 4
               :backgroundColor "primary.main"
               "&:hover" #js {}
               ".Mui-Checkbox" #js {}}}

     ($ Checkbox
        {:checked (= status :resolved)
         :on-change #(rf/dispatch [:todo/toggle-status created-at])})
     ($ editable-text
        {:text text
         :text-style {:text-decoration (when (= :resolved status) :line-through)}
         :on-done-editing #(on-set-todo-text created-at %)})
     ($ Button
        {:on-click #(on-remove-todo created-at)
         ;;               v- theme color
         :sx #js {:color "error.main"}}
        "×")))

(defui app []
  (let [todos (hooks/use-subscribe [:app/todos])]
    ($ Stack
       {}
       ($ header)
       ($ text-field {:on-add-todo #(rf/dispatch [:todo/add %])})
       (for [[created-at todo] todos]
         ($ todo-item
            (assoc todo
                   :created-at created-at
                   :key created-at
                   :on-remove-todo #(rf/dispatch [:todo/remove %])
                   :on-set-todo-text #(rf/dispatch [:todo/set-text %1 %2]))))
       ($ footer))))

(def theme (createTheme
             #js {:components #js {:Checkbox #js {}}}))

(defui app-wrapper []
  ($ ThemeProvider
     {:theme theme}
     ($ app)))

(defonce root
  (uix.dom/create-root (js/document.getElementById "root")))

(defn render []
  (rf/dispatch-sync [:app/init-db app.db/default-db])
  (uix.dom/render-root ($ app-wrapper) root))

(defn ^:export init []
  (render))
