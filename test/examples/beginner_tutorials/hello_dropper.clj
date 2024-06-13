;; Please start your REPL with `+test` profile
(ns examples.beginner-tutorials.hello-dropper
  "Adapted from Ertugrul Cetin's Clojure version of https://wiki.jmonkeyengine.org/docs/3.3/tutorials/beginner/hello_physics.html"
  (:require [java-time.api :refer [after? instant millis plus]]
            [jme-clj.core :refer [add-control add-to-root attach attach-child
                                  bitmap-text box bullet-app-state call* cam
                                  defsimpleapp detach-all-child geo get*
                                  get-state gui-node letj load-font load-texture look-at
                                  material rigid-body-control set* set-state
                                  setc sphere vec3]])
  (:import (com.jme3.math ColorRGBA Vector3f)
           (com.jme3.texture Texture$WrapMode)))

(def brick-length 0.5)
(def brick-width 0.5)
(def brick-height 0.5)

(def interval
  "Drop interval in milliseconds"
  100)

(defn init-materials []
  {:wall-mat (set* (material "Common/MatDefs/Misc/Unshaded.j3md") :color "Color" ColorRGBA/Red)
   :stone-mat (set* (material "Common/MatDefs/Misc/Unshaded.j3md") :color "Color" ColorRGBA/Gray)
   :floor-mat (set* (material "Common/MatDefs/Misc/Unshaded.j3md") :color "Color" ColorRGBA/Blue)})

;; (defn- init-materials []
;;   (letj [wall-mat (material "Common/MatDefs/Misc/Unshaded.j3md")
;;          stone-mat (material "Common/MatDefs/Misc/Unshaded.j3md")
;;          floor-mat (material "Common/MatDefs/Misc/Unshaded.j3md")]
;;         (set* wall-mat :texture "ColorMap" (load-texture "Textures/Terrain/BrickWall/BrickWall.jpg"))
;;         (set* floor-mat :texture "ColorMap" (-> (load-texture "Textures/Terrain/Pond/Pond.jpg")
;;                                                 (set* :wrap Texture$WrapMode/Repeat)))))

(defn- make-brick [loc box* wall-mat bullet-as]
  (let [brick-geo (-> (geo "brick" box*)
                      (setc :material wall-mat
                            :local-translation loc)
                      (add-to-root))
        brick-phy (rigid-body-control 2.0)]
    (add-control brick-geo brick-phy)
    (-> bullet-as
        (get* :physics-space)
        (call* :add brick-phy))))

(defn- init-floor [bullet-as floor floor-mat]
  (let [floor-geo (-> (geo "Floor" floor)
                      (set* :material floor-mat)
                      (set* :local-translation 0 -0.1 0)
                      (add-to-root))
        floor-phy (rigid-body-control 0.0)]
    (add-control floor-geo floor-phy)
    (-> bullet-as
        (get* :physics-space)
        (call* :add floor-phy))))

(defn drop-brick [bullet-as box* wall-mat] 
    (make-brick (vec3 0 (* 20 brick-height) 0) box* wall-mat bullet-as))

(defn simple-update [_tpf]
  (let [{:keys [box boxes boxes-text bullet-as last-drop wall-mat]} (get-state)
        now (instant)]
    ;; (println (format "Last drop `%s`; now `%s`." last-drop now))
  (cond (nil? last-drop) (set-state :app :last-drop now)
        (after? now (plus last-drop  
                      (millis interval))) (do
                       (set* boxes-text :text (format "%d boxes" boxes))
                       (drop-brick bullet-as box wall-mat) 
                       (set-state :app :last-drop now)
                       (set-state :app :boxes (inc boxes))))))

(defn init []
  (let [bullet-as (bullet-app-state)
        bullet-as     (set* bullet-as :debug-enabled true)
        box*      (box brick-length brick-height brick-width)
        floor     (box 10 0.1 5)
        gui-font    (load-font "Interface/Fonts/Default.fnt")
        size        (-> gui-font (get* :char-set) (get* :rendered-size))
        boxes-text  (bitmap-text gui-font false)
        boxes-text'  (-> boxes-text
                        (set* :size size)
                        (set* :text "Starting...")
                        (set* :local-translation 300 (get* boxes-text :line-height) 0))]
    (attach-child (detach-all-child (gui-node)) boxes-text')
    (attach bullet-as)
    (setc (cam) :location (vec3 0 16 24))
    (look-at (vec3 2 2 0) Vector3f/UNIT_Y)
    ;; (set-up-keys)
    (let [{:keys [wall-mat stone-mat floor-mat]} (init-materials)] 
      (init-floor bullet-as floor floor-mat)
      ;; (init-cross-hairs)
      (drop-brick bullet-as box* wall-mat) 
      {:box box* 
       :boxes 1
       :boxes-text boxes-text'
       :stone-mat stone-mat 
       :wall-mat wall-mat
       :bullet-as bullet-as})))

(defsimpleapp app :init init :update simple-update)
