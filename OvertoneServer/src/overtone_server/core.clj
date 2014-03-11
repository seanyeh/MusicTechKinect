(ns overtone-server.core
  (:require [overtone.live :refer :all]
            ))



;; (def note-offsets [0 2 3 7 8])
;;
;; (defn change-freq [freq]
;;   (ctl id :freq freq)
;;   )


;; (defn midi-to-freq [midi]
;;   (* 440 (Math/pow 2 (/ (- midi 69) 12)))
;;   )
;;
;; (defn to-midi [pos]
;;   ; for now, 100 = octave
;;   (let [pix-per-oct   100
;;         pix-per-note  (/ pix-per-oct (count note-offsets))
;;         octaves       (quot pos pix-per-oct)
;;         offset        (mod (quot pos pix-per-note) (count note-offsets))
;;         start-note    62]
;;     (+ start-note (* 12 octaves) (nth note-offsets offset))
;;     )
;;   )
;;
;; (defn on-head-move [msg]
;;   (let [pos   (first (:args msg))
;;         midi  (to-midi pos)
;;         freq  (midi-to-freq midi)
;;         ]
;;     (println "midi:" midi)
;;     (change-freq freq)
;;     )
;;   )



(def RACH (load-sample "rach5.wav"))



(definst buf-inst [buf        RACH
                   bpf-freq   100
                   reverb-mix 0
                   ]
  ;; (* 2 (bpf (play-buf 2 buf) bpf-freq))
  (* 5 (free-verb (bpf (play-buf 2 buf) bpf-freq 0.5) reverb-mix 1 0.2))
  )



;; Fix sometime. this function is hacky
(defn scale-exp [x in-max out-max]
  ; use 10^x
  (let [
        pow       10
        in-ratio  (float (/ x in-max))
        out-ratio (float (/ (Math/pow pow in-ratio) pow))
        ]
    (* out-ratio out-max)
    )
  )

(defn on-lh-move [inst-id msg]
  (let [pos (first (:args msg))
        ]
    (if (and (>= pos 0 ) (<= pos 1600))
      (do 
        ;; (println (scale-exp pos 1600 1))
        (ctl inst-id :reverb-mix (scale-exp pos 1600 1))
        )
      )
    )
  )

(defn on-rh-move [inst-id msg]
  (let [pos (first (:args msg))
        ]
    (if (and (>= pos 0 ) (<= pos 1600))
      (ctl inst-id :bpf-freq (scale-exp pos 1600 2000))
      ;; (println (scale-exp pos 1600 5000))
      )
    )
  )


(defn start-server [port inst-id]
  (let [server (osc-server port "osc-clj")]
    (osc-listen server (fn [msg] ()) :debug)
    ;; (osc-handle server "/head" on-head-move)
    (osc-handle server "/RIGHT_HAND" (partial on-rh-move inst-id))
    ;; (osc-handle server "/LEFT_HAND" (partial on-lh-move inst-id))
    ;; (osc-handle server "/head" on-rh-move)
    ;; (osc-handle server "/r_elbow"

    )
  )

(defn -main [& args]

  (println "Hello, World!")

  (let [inst-id (buf-inst)]
    (start-server 32000 inst-id)
    )

  )

;; testing
;; (demo 14 (comb-c (mda-piano 440) 0.5 0.2 5)) ; Comb delay line, cubic
;; (demo 10 (free-verb (mda-piano 440) 0.7 1 0.2)) ; Reverb
;; (demo 5 (pluck fasdf 2 0.2 0.2 5)) ; pluck. similar to comb delay??
;;
