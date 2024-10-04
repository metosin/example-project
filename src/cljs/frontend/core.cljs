(ns frontend.core
  (:require ["/icons/Cross$default" :as Cross]
            ["@mui/icons-material/Home$default" :as Home]
            ["@mui/material/AppBar$default" :as AppBar]
            ["@mui/material/Button$default" :as Button]
            ["@mui/material/Checkbox$default" :as Checkbox]
            ["@mui/material/Container$default" :as Container]
            ["@mui/material/CssBaseline$default" :as CssBaseline]
            ["@mui/material/Stack$default" :as Stack]
            ["@mui/material/SvgIcon$default" :as SvgIcon]
            ["@mui/material/TextField$default" :as TextField]
            ["@mui/material/Toolbar$default" :as Toolbar]
            ["@mui/material/Typography$default" :as Typography]
            ["@mui/material/styles" :refer [createTheme ThemeProvider]]
            [frontend.handlers :as h]
            [frontend.subs :as s]
            [frontend.uix.hooks :refer [use-subscribe]]
            [re-frame.core :as rf]
            [uix.core :as uix :refer [$ defui]]
            [uix.dom]))

(defui header []
  ($ AppBar
     {:position "static"}
     ($ Toolbar
        ($ Home
           {:sx #js {:mr 2}})
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
  [{:keys [todo]}]
  (let [{:keys [id text status]} todo]
     ($ Stack
        {:direction "row"
         :sx #js {:alignItems "center"}}
        ($ Checkbox
           {:checked (= :resolved status)
            :on-change (fn [_e]
                          (rf/dispatch [::h/save-changes id {:status (if (= :resolved status)
                                                                        :unresolved
                                                                        :resolved)}]))})
        ($ editable-text
           {:text text
            :resolved (= :resolved status)
            :on-done-editing (fn [text]
                                (rf/dispatch [::h/save-changes id {:text text}]))})
        ($ Button
           {:on-click (fn [_e]
                         (rf/dispatch [::h/remove id]))
            :sx #js {:color "error.main"
                     :fontSize "1.5rem"}}
           ($ SvgIcon
              {:component Cross})))))

(defui app []
  (let [todos (use-subscribe [::s/todos])]
    (uix/use-effect (fn []
                       (rf/dispatch [::h/get-todos]))
                    [])
    ($ Stack
       {:direction "column"}
       ($ header)
       ($ Container
          {:direction "column"
           :sx #js {:p 4}}
          ($ text-field {:on-add-todo #(rf/dispatch [::h/add %])})
          ($ Stack
             {:direction "column"
              :sx #js {:mt 4
                       :gap 2}}
             (for [todo todos]
               ($ todo-item
                  {:key (:id todo)
                   :todo todo})))))))

(def theme (createTheme
             #js {:components #js {:Checkbox #js {}}}))

(defui app-wrapper []
  ($ ThemeProvider
     {:theme theme}
     ;; Reset CSS, like body padding etc.
     ($ CssBaseline)
     ($ app)))

(defonce root
  ;; This def is also running in Karma tests, where the element isn't available.
  ;; Avoid errors, we won't need the react root there?
  (when-let [el (js/document.getElementById "app")]
     (uix.dom/create-root el)))

(defn render []
  (uix.dom/render-root ($ app-wrapper) root))

(defn ^:export init []
  (render))
