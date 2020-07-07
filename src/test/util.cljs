(ns util
  (:require
   [cljs.test :refer-macros [is]]
   [cljs.spec.alpha :as s]
   [clojure.test.check.generators]
   [clojure.test.check]
   [clojure.test.check.properties]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [cljs.pprint :as pprint]))

;; Utility functions to intergrate clojure.spec.test/check with clojure.test
(defn summarize-results' [spec-check]
  (map (comp #(pprint/write % :stream nil) stest/abbrev-result) spec-check))

(defn check' [spec-check]
  (is (nil? (-> spec-check first :failure)) (summarize-results' spec-check)))
