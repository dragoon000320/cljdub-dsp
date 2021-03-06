(ns cljdub-dsp.util-test
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer :all]
            [clojure.test :refer :all]
            [cljdub-dsp.util :refer :all]
            [libpython-clj.python :refer [py..]]
            [libpython-clj.require :refer [require-python]]))

(require-python '[numpy :as np]
                '[builtins :as bins])

(def uint8-gen
  (gen/choose 0 255))

(defspec numpy->bytes-test
  100
  (prop/for-all [bs (gen/bind (gen/choose 2 300)
                              #(gen/vector uint8-gen %))]
                (let [numpy-arr (np/array bs :dtype np/uint8)
                      byte-string (bins/bytes bs)]
                  (= (numpy->bytes numpy-arr)
                     byte-string))))

(defspec bytes->numpy-test
  100
  (prop/for-all [bs (gen/bind (gen/choose 2 300)
                              #(gen/vector uint8-gen %))]
                (let [numpy-arr (py.. (np/array bs :dtype np/uint8)
                                      (astype np/int8))
                      byte-string (bins/bytes bs)]
                  (= (vec (bytes->numpy byte-string))
                     (vec numpy-arr)))))

(defspec empty-byte-numpy-test
  100
  (prop/for-all [size (gen/choose 2 300)]
                (let [numpy-arr (empty-byte-numpy size)]
                  (and (= np/int8 (py.. numpy-arr -dtype))
                       (= (bins/len numpy-arr) size)))))

(deftest wrap-vec-test
  (let [vec [1 2 3 4 5 6 7 8]
        coll '(1 2 3 4 5 6 7 8)
        elem 1
        vec-nested [1 2 3 4 5 [6 7 8 9]]
        coll-nested '(1 2 [1 2 3] [5 6 7 8] 9 19)]
    (is (= vec (apply wrap-vec vec)))
    (is (= vec (wrap-vec vec)))
    (is (= [elem] (wrap-vec elem)))
    (is (= (flatten vec-nested) (apply wrap-vec vec-nested)))
    (is (= (flatten coll-nested) (apply wrap-vec coll-nested)))))

(deftest doto-for-test
  (is (= (doto-for (new java.util.ArrayList)
                   .add
                   [1 2 3 4 5])
         [1 2 3 4 5]))
  (is (= (doto-for (new java.util.ArrayList)
                   .add
                   1 2 3 4 5)
         [1 2 3 4 5]))
  (is (= (.toString (doto-for (new java.lang.StringBuilder)
                              .append
                              "t" "e" "s" "t"))
         "test")))
