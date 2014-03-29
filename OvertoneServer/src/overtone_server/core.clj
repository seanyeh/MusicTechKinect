(ns overtone-server.core
  (:require [overtone.live :refer :all]
            [overtone.inst.drum :refer :all]
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



(definst buf-inst [buf          RACH
                   bpf-freq     100
                   reverb-mix   0
                   ringz-freq   440
                   ]
  (let [out   (* 10 (play-buf 2 buf))

        out   (bpf out bpf-freq 0.5)

        out-ringz   (* 0.2 (ringz (* 0.02 out) ringz-freq 0.5))
        out   (mix [out out-ringz])

        out   (free-verb out reverb-mix 1 0.2)
        ]

    (* 1 out)
    ))



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

(defn scale-lin [x in-min in-max out-max]
  (let [in-diff   (- in-max in-min)

        in-ratio  (/ (- x in-min) in-diff)
        in-ratio  (float in-ratio)
        ]
    (cond (> in-ratio 1)  out-max
          (< in-ratio 0)  0
          :else           (* in-ratio out-max)
          )
    )
  )

(defn ctl-reverb [inst-id msg]
  (let [pos (first (:args msg))
        x   (scale-lin (Math/abs pos) 100 1200 0.8)]
    (do
      ;; (println "reverb:" x)
      (ctl inst-id :reverb-mix x)
      )))

(defn ctl-ringz [inst-id msg]
  (let [pos (first (:args msg))
        x   (scale-exp pos 1600 2000)]
    (ctl inst-id :ringz-freq x)
    ))

(defn on-rh-move [inst-id msg]
  (let [args  (:args msg)
        pos   (first args)]
    (if (and (>= pos 0 ) (<= pos 1600))
      (ctl inst-id :bpf-freq (scale-exp pos 1600 2000))
      ;; (println (scale-exp pos 1600 5000))
      )
    )
  )


(defn start-server [port inst-id]
  (let [server (osc-server port "osc-clj")]
    (osc-listen server (fn [msg] (println msg)) :debug)
    (osc-handle server "/HAND_SPAN"   (partial ctl-reverb inst-id))
    ;; (osc-handle server "/TORSO"       (partial ctl-ringz  inst-id))

    (osc-handle server "/RIGHT_HAND"  (partial on-rh-move inst-id))

    (osc-handle server "/RH_GESTURE" (fn [msg] (haziti-clap)))

    ;; (osc-handle server "/LEFT_HAND"   (partial ctl-bpf inst-id))
    ;; (osc-handle server "/head" on-rh-move)
    ;; (osc-handle server "/r_elbow"

    )
  )

(defn -main [& args]

  (println "Hello, World!")

  (let [inst-id (buf-inst)
        c (fx-limiter 0)
        c (fx-limiter 1)
        ;; c (fx-compressor 0)
        ;; c (fx-compressor 1)
        ]
    (start-server 32000 inst-id)
    )

  )

;; testing
;; (demo 14 (comb-c (mda-piano 440) 0.5 0.2 5)) ; Comb delay line, cubic
;; (demo 10 (free-verb (mda-piano 440) 0.7 1 0.2)) ; Reverb
;; (demo 5 (pluck fasdf 2 0.2 0.2 5)) ; pluck. similar to comb delay??
;;
