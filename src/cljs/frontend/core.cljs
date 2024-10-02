(ns frontend.core
  (:require ["@mui/material/AppBar$default" :as AppBar]
            ["@mui/material/Box$default" :as Box]
            ["@mui/material/Button$default" :as Button]
            ["@mui/material/Checkbox$default" :as Checkbox]
            ["@mui/material/Container$default" :as Container]
            ["@mui/material/CssBaseline$default" :as CssBaseline]
            ["@mui/material/FormControl$default" :as FormControl]
            ["@mui/material/Stack$default" :as Stack]
            ["@mui/material/TextField$default" :as TextField]
            ["@mui/material/Toolbar$default" :as Toolbar]
            ["@mui/material/Typography$default" :as Typography]
            ["@mui/material/styles" :refer [createTheme ThemeProvider]]
            [frontend.db]
            [frontend.fx]
            [frontend.handlers]
            [frontend.subs]
            [frontend.uix.hooks :refer [use-subscribe]]
            [re-frame.core :as rf]
            [uix.core :as uix :refer [$ defui]]
            [uix.dom]))

(defui header []
  ($ AppBar
     {:position "static"}
     ($ Toolbar
        ($ Typography
           "Example"))))

(defui text-field [{:keys [on-add-todo]}]
  (let [[value set-value!] (uix/use-state "")]
    ($ TextField
       {:value value
        :placeholder "Add a new todo and hit Enter to save"
        :fullWidth true
        :onChange (fn [^js e]
                    (set-value! (.. e -target -value)))
        :onKeyDown (fn [^js e]
                     (when (= "Enter" (.-key e))
                       (set-value! "")
                       (on-add-todo {:text value :status :unresolved})))})))

(defui editable-text [{:keys [text resolved on-done-editing]}]
  (let [[editing? set-editing!] (uix/use-state false)
        [editing-value set-editing-value!] (uix/use-state nil)]
    ($ TextField
       {:value (or editing-value text)
        :fullWidth true
        :onClick (fn [_]
                   (set-editing! true)
                   (set-editing-value! text))
        :onChange (fn [^js e]
                    (set-editing-value! (.. e -target -value)))
        :onKeyDown (fn [^js e]
                     (when (= "Enter" (.-key e))
                       (set-editing-value! nil)
                       (set-editing! false)
                       (on-done-editing editing-value)))
        :sx #js {".MuiOutlinedInput-notchedOutline" #js {:borderColor (when-not editing? "transparent")}
                 ".MuiInputBase-input" #js {:textDecoration (when resolved "line-through")}}})))

(defui todo-item
  [{:keys [created-at text status on-remove-todo on-set-todo-text]}]
  ($ Stack
     {:direction "row"
      :sx #js {:alignItems "center"}}
     ($ Checkbox
        {:checked (= :resolved status)
         :on-change #(rf/dispatch [:todo/toggle-status created-at])})
     ($ editable-text
        {:text text
         :resolved (= :resolved status)
         :on-done-editing #(on-set-todo-text created-at %)})
     ($ Button
        {:on-click #(on-remove-todo created-at)
         ;;               v- theme color
         :sx #js {:color "error.main"
                  :fontSize "1.5rem"}}
        "×")))

(defui app []
  (let [todos (use-subscribe [:app/todos])]
    ($ Stack
       {:direction "column"}
       ($ header)
       ($ Container
          {:direction "column"
           :sx #js {:p 4}}
          ($ text-field {:on-add-todo #(rf/dispatch [:todo/add %])})
          ($ Stack
             {:direction "column"
              :sx #js {:mt 4
                       :gap 2}}
             (for [[created-at todo] todos]
               ($ todo-item
                  (assoc todo
                         :created-at created-at
                         :key created-at
                         :on-remove-todo #(rf/dispatch [:todo/remove %])
                         :on-set-todo-text #(rf/dispatch [:todo/set-text %1 %2])))))))))

(def theme (createTheme
             #js {:components #js {:Checkbox #js {}}}))

(defui app-wrapper []
  ($ ThemeProvider
     {:theme theme}
     ;; Reset CSS, like body padding etc.
     ($ CssBaseline)
     ($ app)))

(defonce root
  (uix.dom/create-root (js/document.getElementById "app")))

(defn render []
  (rf/dispatch-sync [:app/init-db frontend.db/default-db])
  (uix.dom/render-root ($ app-wrapper) root))

(defn ^:export init []
  (render))
