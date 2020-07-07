(ns server.core-test
    (:require
     [pjstadig.humane-test-output]
     [cljs.test :refer-macros [is are deftest testing use-fixtures]]
     [cljs.spec.alpha :as s]
     [clojure.test.check.generators]
     [clojure.test.check]
     [clojure.test.check.properties]
     [clojure.spec.gen.alpha :as gen]
     [clojure.spec.test.alpha :as stest]
     [cljs.pprint :as pprint]
     [server.main]
     [server.radio-sim :as sim]
     [client.presets :as presets]
     [util :refer [check']]))


(deftest test-unknown-command
  (is (= ["unknown"] (sim/command->response "unknown"))))


(defn ranged-rand
  "Returns random int in range start <= rand < end"
  [start end]
  (+ start (long (rand (- end start)))))

(s/fdef ranged-rand
  :args (s/and (s/cat :start int? :end int?)
               #(< (:start %) (:end %)))
  :ret int?
  :fn (s/and #(>= (:ret %) (-> % :args :start))
                          #(< (:ret %) (-> % :args :end))))

(deftest rand-test (check' (stest/check `ranged-rand)))


(deftest preset-regex
  (is (presets/preset? "RT1 SYS_PRESET 5 DESCRIPTION")))

(deftest preset-regex-failes
  (is (not (presets/preset? "STATE SYS_PRESET NAME VMFOIV")))
  (is (not (presets/preset? "STATE SYS_PRESET_NUMBER 1"))))

(deftest parse-preset
  (is (= {:radio "RT1" :number "1"} (presets/parse-preset "RT1 SYS_PRESET 1 S"))))
