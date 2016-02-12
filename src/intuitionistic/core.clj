(ns intuitionistic.core
  (:gen-class)
  ;;(:import )
  ;;(:require [clojure-csv.core :as csv])
  (:use [incanter core stats charts pdf])
  (:import generic.Input
           generic.Output
           generic.Tuple
           tools.JMathPlotter
           java.util.TreeMap
           
           type1.sets.T1MF_Interface
           type1.sets.T1MF_Triangular
           type1.sets.T1MF_Gaussian
           type1.system.T1_Antecedent
           type1.system.T1_Consequent
           type1.system.T1_Rule
           type1.system.T1_Rulebase
           
           intervalType2.sets.IntervalT2MF_Interface
           intervalType2.sets.IntervalT2MF_Triangular
           intervalType2.sets.IntervalT2MF_Gaussian
           intervalType2.system.IT2_Antecedent
           intervalType2.system.IT2_Consequent
           intervalType2.system.IT2_Rule
           intervalType2.system.IT2_Rulebase
           
           generalType2zSlices.sets.GenT2zMF_Interface
           generalType2zSlices.sets.GenT2zMF_Triangular
           generalType2zSlices.system.GenT2z_Antecedent
           generalType2zSlices.system.GenT2z_Consequent
           generalType2zSlices.system.GenT2z_Rule
           generalType2zSlices.system.GenT2z_Rulebase
           generalType2zSlices.system.GenT2zEngine_Defuzzification
           generalType2zSlices.system.GenT2zEngine_Intersection
           generalType2zSlices.system.GenT2zEngine_Union)
  )

(load "mgts")
(load "sunspot")

(defn % [num den]
  (if (zero? den)
    num
    (/ num den)))

(defn scale [ts from to]
  (let [max (apply max ts)
        min (apply min ts)]
    (map (fn [x]
           (+ (* from (- 1 (/ (- x min) (- max min))))
              (* to (/ (- x min) (- max min))))) ts)))

(defn scale [ts from to]
  (let [max (apply max ts)
        min (apply min ts)]
    (map (fn [x]
           (+ (* from (- 1 (% (- x min) (- max min))))
              (* to (% (- x min) (- max min))))) ts)))

;; Mackey-Glass preprocessing

(defn mg-group [lst indexes]
  (loop [result []
         remaining lst]
    (if (> (count remaining) (apply max indexes))
      (recur (conj result (map #(nth remaining %) indexes))
             (rest remaining))
      (into [] result))))

;;(count *sunspot-train)
;;(count *sunspot-test)

(def *mg-train (subvec (mg-group (scale (subvec (into [] *mg) 0 1019)
                                        0 100) [0 6 12 18 19])
                       0 500))
(def *mg-test (subvec (mg-group (scale (subvec (into [] *mg) 0 1019)
                                       0 100) [0 6 12 18 19])
                      500 1000))

(def *sunspot-train (subvec (mg-group (scale (subvec (into [] *sunspot) 0 304)
                                             0 100) [0 1 2 3 4])
                            0 150))
(def *sunspot-test (subvec (mg-group (scale (subvec (into [] *sunspot) 0 304)
                                            0 100) [0 1 2 3 4])
                           150 300))

;;(mg-group (into [] (take 100 *mg)) [0 6 12 18 19])

;; end Mackey-Glass preprocessing

;; Gajure

(defn roulette-select
  "Select num individuals from pop, with an individual's fitness porportional to selection likelihood."
  [pop fit-fn num]
  (let [pop-fits (map fit-fn pop)
        inc-fits (iterate (fn [[pfit idx]]
                            [(+ (nth pop-fits (inc idx)) pfit) (inc idx)])
                          [(first pop-fits) 0])
        max-fitness (apply + pop-fits)
        pick-one (fn [num] (second (first (drop-while #(< (first %) num) inc-fits))))]
    (map (fn [x] (nth pop (pick-one (rand-int max-fitness)))) (range num))))

(defn do-crossover
  "Apply cross-fn to plist, partitioned into groups of num-parents. For instance,
traditional 2-parent crossover requires that num-parents equal 2."
  [p-list cross-fn num-parents]
  (map cross-fn (partition num-parents p-list)))

(defn keys-not-nil [lst hash]
  "For each key in a list of keys, make sure that is is not nil in the hash"
  (reduce #(and %1 %2) (map #(not (nil? (hash %))) lst)))

(defn run-ga
  "Pass two maps, one for functions, the other for settings.
  For funcmap -- init-fn: takes one argument (a number) and initializes population.
  fit-fn: takes a population member and outputs fitness
  mut-fn: takes a population, and returns a mutated population.
  sel-fn: takes a population, a fitness function, and a number to select. Returns selected members.
  cross-fn: takes a list of vectors, each vector containing the parent to cross.
  For setting-map -- pop-sz is size of population; gen is number of generations to run;
  children is the number of children to create each generation; mut-r is the rate of mutation (0-100)"
  [func-map setting-map]
  {:pre [(and (keys-not-nil (list :init-fn :fit-fn :mut-fn :sel-fn :cross-fn) func-map)
              (keys-not-nil (list :pop-sz :gen :children :mut-r) setting-map))]}
  (let [ipop ((:init-fn func-map) (:pop-sz setting-map))]
    (loop [pop ipop
           num (:gen setting-map)]
      (if (zero? num)
        (do ;; insert information you would like printed here
          (let [evaluated-pop (pmap #(vector ((:fit-fn func-map) %) %) pop)]
            (let [pre-fittest (first (sort-by first < evaluated-pop))
                  fittest (second pre-fittest)]
              ;;(println "")
              (println (str (first pre-fittest) ", "
                            (if (= (count (first fittest)) 2)
                         (ifis-fitness *mg-test fittest)
                         (if (= (count (first fittest)) 3)
                           (t1-fitness *mg-test fittest)
                           (it2-fitness *mg-test fittest)))))
              ;;fittest
              )))
        (let [total-left (- (:pop-sz setting-map) (:children setting-map))]
          (do ;; and here
            (let [evaluated-pop (pmap #(vector ((:fit-fn func-map) %) %) pop)]
              (let [pre-fittest (first (sort-by first < evaluated-pop))
                    fittest (second pre-fittest)]
                ;;(println (first pre-fittest))
                (recur
                 (concat
                  [fittest]
                  ((:mut-fn func-map)
                   (do-crossover
                    ((:sel-fn func-map)
                     pop
                     (:fit-fn func-map)
                     (* (:children setting-map) 2))
                    (:cross-fn func-map)
                    2)
                   (:mut-r setting-map))
                  ((:init-fn func-map) total-left))
                 (dec num))))))))))

;; Some helpers for using the framework

(defn list-crossover
  "A generic crossover function for simple lists. You may need to write your own."
  [[s1 s2]]
  (let [point (rand-int
               (min (count s1)
                    (count s2)))]
    (concat (take point (shuffle s1))
            (drop point (shuffle s2)))))

;;(list-crossover ['(1 2 3 4) '(11 12 13 14)])

(defn generic-mutation
  "Randomly mutates lists with elements from other lists in the population."
  [list prob]
  {:pre [(and (>= prob 1) (<= prob 100))]}
  (map
   (fn [s-list]
     (map
      (fn [test]
        (if (> prob (rand-int 100))
          (let [r-s (rand-int (count list))
                r-t (rand-int (count (nth list r-s)))]
            (nth (nth list r-s)
                 r-t))
          test))
      s-list))
   list))

(defn rand-from-list
  "Given a list of elements, construct a new list using the elements in that set.
Ex: (rand-from-list '(1 2 3 4 5) 2) => (3 5)"
  [lst num]
  (let [total-el (count lst)]
    (map (fn [x] (nth lst (rand-int total-el))) (range 0 num))))

(defn rand-pop
  "Creates a population using rand-from list. Helpful for creating init-fn."
  [lst num num-pop]
  (map (fn [x] (rand-from-list lst num)) (range 0 num-pop)))

;;end Gajure

(defn ifs [mf nmf]
  (map #(vector (first %1) (second %1) (second %2)) mf nmf))

(defn gaussian-mf [mean spread from to]
  (map #(vector (double %1) %2)
       (range 0 101)
       (scale (map #(Math/exp (* -0.5 (Math/pow (/ (- % mean) spread) 2)))
                   (range 0 101)) from to)))
(defn gaussian-nmf [mean spread from to]
  (map #(vector (double %1) %2)
       (range 0 101)
       (scale (map #(- 1 %)
                   (map #(Math/exp (* -0.5 (Math/pow (/ (- % mean) spread) 2)))
                        (range 0 101))) from to)))

(defn gaussian [mean spread from to]
  (ifs (gaussian-mf mean spread from to)
       (gaussian-nmf mean spread from to)))

(defn triangular-mf [a b c from to]
  (map #(vector (double %1) %2)
       (range 0 101)
       (scale (map (fn [x]
                     (double (if (<= x a)
                               0
                               (if (and (<= a x) (<= x b))
                                 (/ (- x a)
                                    (- b a))
                                 (if (and (<= b x) (<= x c))
                                   (/ (- c x)
                                      (- c b))
                                   0)))))
                   (range 0 101)) from to)))

(defn triangular-nmf [a b c from to]
  (map #(vector (double %1) %2)
       (range 0 101)
       (scale
        (map #(- 1 %)
             (map (fn [x]
                    (double (if (<= x a)
                              0
                              (if (and (<= a x) (<= x b))
                                (/ (- x a)
                                   (- b a))
                                (if (and (<= b x) (<= x c))
                                  (/ (- c x)
                                     (- c b))
                                  0)))))
                  (range 0 101))) from to)))

(defn triangular [a b c from to]
  (ifs (triangular-mf a b c from to)
       (triangular-nmf a b c from to)))

(defn trapezoidal-mf [a b c d from to]
  (map #(vector (double %1) (double %2))
       (range 0 101)
       (scale
        (map (fn [x]
               (max (min (/ (- x a) (- b a)) 1 (/ (- d x) (- d c))) 0))
             (range 0 101))
        from to)))

(defn trapezoidal-nmf [a b c d from to]
  (map #(vector (double %1) (double %2))
       (range 0 101)
       (scale
        (map #(- 1 %)
             (map (fn [x]
                    (max (min (/ (- x a) (- b a)) 1 (/ (- d x) (- d c))) 0))
                  (range 0 101)))
        from to)))

(defn trapezoidal [a b c d from to]
  (ifs (trapezoidal-mf a b c d from to)
       (trapezoidal-nmf a b c d from to)))

(defn gbell-mf [a b c from to]
  (map #(vector (double %1) (double %2))
       (range 0 101)
       (scale (map (fn [x]
                     (/ 1 (+ 1 (Math/pow (abs (/ (- x c) a)) (* 2 b)))))
                   (range 0 101))
              from to)))

(defn gbell-nmf [a b c from to]
  (map #(vector (double %1) (double %2))
       (range 0 101)
       (scale (map #(- 1 %)
                   (map (fn [x]
                          (/ 1 (+ 1 (Math/pow (abs (/ (- x c) a)) (* 2 b)))))
                        (range 0 101)))
              from to)))

(defn gbell [a b c from to]
  (ifs (gbell-mf a b c from to)
       (gbell-nmf a b c from to)))

;;(iplot (gbell 20 1 50 0 1))

(defn sigmoidal-mf [a b from to]
  (map #(vector (double %1) (double %2))
       (range 0 101)
       (scale (map (fn [x]
                     (/ 1 (+ 1 (exp (* (- a) (- x b)))))
                     )
                   (range 0 101))
              from to)))

(defn sigmoidal-nmf [a b from to]
  (map #(vector (double %1) (double %2))
       (range 0 101)
       (scale (map #(- 1 %)
                   (map (fn [x]
                          (/ 1 (+ 1 (exp (* (- a) (- x b)))))
                          )
                        (range 0 101)))
              from to)))

(defn sigmoidal [a b from to]
  (ifs (sigmoidal-mf a b from to)
       (sigmoidal-nmf a b from to)))

(defn left-right-mf [a b c from to]
  (map #(vector (double %1) (double %2))
       (range 0 101)
       (scale (map (fn [x]
                     (if (<= x c)
                       (let [v (sqrt (- 1 (Math/pow (/ (- c x) a) 2)))]
                              (if (Double/isNaN v)
                                0
                                v))
                       (exp (- (Math/pow (abs (/ (- x c) b)) 3)))
                       ))
                   (range 0 101))
              from to)))

(defn left-right-nmf [a b c from to]
  (map #(vector (double %1) (double %2))
       (range 0 101)
       (scale (map #(- 1 %)
                   (map (fn [x]
                          (if (<= x c)
                            (let [v (sqrt (- 1 (Math/pow (/ (- c x) a) 2)))]
                              (if (Double/isNaN v)
                                0
                                v))
                            (exp (- (Math/pow (abs (/ (- x c) b)) 3)))
                            ))
                        (range 0 101)))
              from to)))

(defn left-right [a b c from to]
  (ifs (left-right-mf a b c from to)
       (left-right-nmf a b c from to)))


;; deprecated
(defn down-mf
  "TODO: Correct discretization-level in every mf"
  [lastone firstzero domain-size discretization-level]
  (let [ones (map (fn [x] 1) (range lastone))
        ramp (map (fn [x] x) (range 0 1 (/ 1 (- firstzero lastone))))
        zeros (map (fn [x] 0) (range firstzero domain-size))]
    (map #(vector (double %1) (double %2))
         (range 0 domain-size (/ domain-size discretization-level))
         (concat ones (reverse ramp) zeros))))

;; deprecated
(defn down-nmf [lastzero firstone domain-size nm-from nm-to discretization-level]
  (let [ones (map (fn [x] 1) (range lastzero))
        ramp (map (fn [x] x) (range 0 1 (/ 1 (- firstone lastzero))))
        zeros (map (fn [x] 0) (range firstone discretization-level))]
    (map #(vector (double %1) (double %2))
         (range 0 domain-size (/ domain-size discretization-level))
         (scale (map #(- 1 %) (concat ones (reverse ramp) zeros)) nm-from nm-to))
    ))

;; deprecated by (trapezoidal)
(defn down [lastone firstzero domain-size nm-from nm-to discretization-level]
  (ifs (down-mf lastone firstzero domain-size discretization-level)
       (down-nmf lastone firstzero domain-size nm-from nm-to discretization-level)))

(defn up-mf [lastzero firstone domain-size discretization-level]
  (let [ones (map (fn [x] 1) (range lastzero))
        ramp (map (fn [x] x) (range 0 1 (/ 1 (- firstone lastzero))))
        zeros (map (fn [x] 0) (range firstone discretization-level))]
    (map #(vector (double %1) (double (- 1 %2)))
         (range 0 domain-size (/ domain-size discretization-level))
         (concat ones (reverse ramp) zeros))
    ))

(defn up-nmf
  [lastone firstzero domain-size nm-from nm-to discretization-level]
  (let [ones (map (fn [x] 1) (range lastone))
        ramp (map (fn [x] x) (range 0 1 (/ 1 (- firstzero lastone))))
        zeros (map (fn [x] 0) (range firstzero domain-size))]
    (map #(vector (double %1) (double %2))
         (range 0 domain-size (/ domain-size discretization-level))
         (scale (concat ones (reverse ramp) zeros) nm-from nm-to))))

(defn up [lastone firstzero domain-size nm-from nm-to discretization-level]
  (ifs (up-mf lastone firstzero domain-size discretization-level)
       (up-nmf lastone firstzero domain-size nm-from nm-to discretization-level)))

(defn ifs-csv [file ifs]
  (spit file (apply str (map #(str (nth % 0) ", " (nth % 1) ", " (nth % 2) "\n") ifs))))

(defn union [ifs1 ifs2]
  (map #(vector (nth %1 0)
                (max (nth %1 1) (nth %2 1))
                (min (nth %1 2) (nth %2 2))) ifs1 ifs2))

(defn intersection [ifs1 ifs2]
  (map #(vector (nth %1 0)
                (min (nth %1 1) (nth %2 1))
                (max (nth %1 2) (nth %2 2))) ifs1 ifs2))

(defn implication [ifs1 ifs2]
  (map #(vector (nth %1 0)
                (max (nth %1 2) (nth %2 1))
                (min (nth %1 1) (nth %2 2))) ifs1 ifs2))

(defn membership [x ifs]
  (if (>= x (- (count ifs) 1))
    (last ifs)
    (if (neg? x)
      (first ifs)
      (let [index (int x)
            fraction (- x (int x))]
        (map + (nth ifs index)
             (map #(* fraction %)
                  (map -
                       (nth ifs (inc index))
                       (nth ifs index))))
        ))))

(defn for-mf-what-nmf [mf ifs]
  (if (>= mf 1)
    (nth (first (sort-by second > ifs)) 2)
    (if (<= mf 0)
      (nth (first (sort-by second < ifs)) 2)
      (let [fst (last (take-while #(< (nth % 1) mf) ifs))
            lst (first (drop-while #(< (nth % 1) mf) ifs))
            subst (map #(Math/abs %) (map - lst fst))
            mf-fraction (/ (- mf (nth fst 1)) (- (nth lst 1) (nth fst 1)))
            nmf-fraction (/ (- mf (nth lst 1)) (- (nth fst 1) (nth lst 1)))
            ;;mult-frac (map #(* fraction %) subst)
            mult-frac (vector (* (nth subst 0) mf-fraction)
                              (* (nth subst 1) mf-fraction)
                              (* (nth subst 2) nmf-fraction))]
        (vector (+ (nth fst 0) (nth mult-frac 0))
                (+ (nth fst 1) (nth mult-frac 1))
                (+ (nth lst 2) (nth mult-frac 2)))
        )
      )))

(defn for-mf-what-nmf [mf ifs]
  (if (>= mf (second (first (sort-by second > ifs))))
    (nth (first (sort-by second > ifs)) 2)
    (if (<= mf 0)
      (nth (first (sort-by second < ifs)) 2)
      (let [;;fst (last (take-while #(< (nth % 1) mf) ifs))
            index (let [try1 (filter #(> (nth % 1) mf) ifs)
                        try2 (filter #(< (nth % 1) mf) ifs)]
                    (if (= (int (first (first try1))) 0)
                      (int (first (first try2)))
                      (int (first (first try1))))
                    )
            fst (nth ifs (dec (int index)))
            lst (nth ifs (int index))
            ;;lst (first (drop-while #(< (nth % 1) mf) ifs))
            ]
        (+ (nth lst 2) (* (Math/abs (- (nth lst 2) (nth fst 2)))
                          (/ (- mf (nth lst 1)) (- (nth fst 1) (nth lst 1)))))
        ))))

;; (for-mf-what-nmf 0.4999 (ifs (gaussian-mf 33 10 0 0.5)
;;                           (gaussian-nmf 33 10 0 0.3)))

;; (iplot (ifs (gaussian-mf 33 10 0 0.5)
;;             (gaussian-nmf 33 10 0 0.3)))

;;(for-mf-what-nmf 0.7 (gaussian 33 30 0 1))
;;(for-mf-what-nmf 0.4 (gaussian 33 30 0 1))
;;(iplot (gaussian 33 30 0 1))

(defn if-membership [x ifs]
  ;; i\mu_{A}(x) = (\nu_{A}(x) + \mu_{A}(x))\mu_{A}(x)
  (let [m (membership x ifs)]
    (* (+ (nth m 2) (nth m 1)) (nth m 1))
    ;;(nth m 1)
    ))


;; aqui
(comment
  ;; fs as ifs
  (incanter.pdf/save-pdf (iplot (gaussian 50 15 0 1))
                         "fs-as-ifs.pdf")

  ;; ifs example
  (incanter.pdf/save-pdf (iplot (ifs (gaussian-mf 50 15 0 1.0)
                                     (gaussian-nmf 50 15 0 0.3)))
                         "ifs.pdf")
  
  ;; ifs with different means and sds
  (incanter.pdf/save-pdf (iplot (ifs (gaussian-mf 50 15 0 0.7)
                                     (gaussian-nmf 30 20 0 0.3)))
                         "ifs-diff-mu-sd.pdf")

  ;; alpha-cut simple
  (incanter.pdf/save-pdf (iplot (clip 0.6 (ifs (gaussian-mf 50 15 0 0.7)
                                               (gaussian-nmf 50 15 0 0.3))))
                         "alpha-cut-simple.pdf")
  
  ;; alpha-cut complex
  (incanter.pdf/save-pdf (iplot (clip 0.6 (ifs (gaussian-mf 50 15 0 0.7)
                                               (gaussian-nmf 30 15 0 0.3))))
                         "alpha-cut.pdf")

  ;; union of different alpha-cuts
  (incanter.pdf/save-pdf (iplot (reduce union
                                        [(clip 0.6 (ifs (gaussian-mf 50 15 0 1)
                                                        (gaussian-nmf 50 15 0 1)))
                                         (clip 0.6 (ifs (gaussian-mf 60 15 0 1)
                                                        (gaussian-nmf 60 15 0 1)))
                                         (clip 0.6 (ifs (gaussian-mf 90 15 0 1)
                                                        (gaussian-nmf 90 15 0 1)))]))
                         "ifs-union.pdf")

  ;; intersection ifs
  (incanter.pdf/save-pdf (iplot (reduce intersection
                                        [(clip 0.6 (ifs (gaussian-mf 50 15 0 1)
                                                        (gaussian-nmf 50 15 0 1)))
                                         (clip 0.6 (ifs (gaussian-mf 60 15 0 1)
                                                        (gaussian-nmf 60 15 0 1)))]))
                         "ifs-intersection.pdf")
  
  ;; implication gaussian ifs
  (incanter.pdf/save-pdf (iplot (reduce implication
                                        [(clip 0.6 (ifs (gaussian-mf 50 15 0 1)
                                                        (gaussian-nmf 50 15 0 1)))
                                         (clip 0.6 (ifs (gaussian-mf 60 15 0 1)
                                                        (gaussian-nmf 60 15 0 1)))]))
                         "ifs-implication.pdf")

  ;; implication triangular ifs
  (incanter.pdf/save-pdf (iplot (reduce implication
                                        [(clip 0.6 (ifs (triangular-mf 20 40 60 0 1)
                                                        (triangular-nmf 20 40 60 0 1)))
                                         (clip 0.6 (ifs (triangular-mf 40 70 90 0 1)
                                                        (triangular-nmf 40 70 90 0 1)))]))
                         "ifs-implication-triangular.pdf")
  
  ;; if-membership plot
  (incanter.pdf/save-pdf (iplot (map #(vector (nth %1 0) (nth %1 1) %2)
                                     (gaussian-mf 50 15 0 1)
                                     (map (fn [x]
                                            (if-membership x (ifs (gaussian-mf 50 15 0 1)
                                                                  (gaussian-nmf 50 15 0 0.5))))
                                          (range 101))))
                         "if-membership.pdf")

  ;; a more drastic example
  (incanter.pdf/save-pdf (iplot (map #(vector (nth %1 0) (nth %1 1) %2)
                                     (gaussian-mf 50 15 0 0.8)
                                     (map (fn [x]
                                            (if-membership x (ifs (gaussian-mf 50 15 0 0.8)
                                                                  (gaussian-nmf 30 20 0 0.2))))
                                          (range 101))))
                         "if-membership-drastic.pdf")

  ;;comparison between coa and if-coa
  (incanter.pdf/save-pdf (coa-iplot (map
                                     (fn [x]
                                       (vector
                                        x
                                        (coa (ifs (gaussian-mf x 15 0 0.8)
                                                  (gaussian-nmf (- x 20) 20 0 0.2)))
                                        (if-coa (ifs (gaussian-mf x 15 0 0.8)
                                                     (gaussian-nmf (- x 20) 20 0 0.2)))))
                                     (range 101)))
                         "if-coa-vs-coa.pdf")


  ;; down mf
  (incanter.pdf/save-pdf (iplot (ifs (down-mf 20 40 101 101)
                                     (down-nmf 20 40 101 0 1 101)))
                         "down-mf.pdf")

  ;; up mf
  (incanter.pdf/save-pdf (iplot (ifs (up-mf 20 40 101 101)
                                     (up-nmf 20 40 101 0 1 101)))
                         "up-mf.pdf")

  ;; trapezoidal
  (incanter.pdf/save-pdf (iplot (ifs (trapezoidal-mf 20 40 60 80 0 0.6)
                                     (trapezoidal-nmf 40 60 80 100 0 0.4)))
                         "trapezoidal-mf.pdf")

  ;; trianglular mf
  (incanter.pdf/save-pdf (iplot (ifs
                                 (triangular-mf 30 50 80 0 0.8)
                                 (triangular-nmf 40 60 80 0 0.2)
                                 ))
                         "triangular.pdf")

  ;; generalized bell mf
  (incanter.pdf/save-pdf (iplot (ifs (gbell-mf 20 3 50 0 0.7)
                                     (gbell-nmf 20 3 30 0 0.3)))
                         "gbell.pdf")
  
  ;; sigmoidal
  (incanter.pdf/save-pdf (iplot (ifs (sigmoidal-mf 0.3 50 0 1)
                                     (sigmoidal-nmf 0.3 50 0 1)))
                         "sigmoidal.pdf")

  ;; left-right mf left
  (incanter.pdf/save-pdf (iplot (left-right 60 10 65 0 1))
                         "left-right-l.pdf")

  ;; left-right mf right
  (incanter.pdf/save-pdf (iplot (left-right 10 40 25 0 1))
                         "left-right-r.pdf")
  )

(defn clip [x ifs]
  (let [;;nm-max (nth (first (filter #(> (nth % 1) x) ifs)) 2)
        nm-max (for-mf-what-nmf x ifs)
        ]
    (if nm-max
      (map vector
           (map first ifs)
           (map #(min x (nth % 1)) ifs)
           ;;(map #(if (= (min x (nth % 1)) x)) ifs)
           (map #(max nm-max (nth % 2)) ifs)
           )
      ifs)
    ))

(comment
  (map (fn [tuple]
         (+ (nth tuple 1) (nth tuple 2))
         )
       (let [mf (gaussian (rand 100) (rand 100) 0 1)]
         ;;(iplot mf)
         (iplot (clip 0.7 mf))
         ;;(clip (rand 1) mf)
         )
       ))

;;(iplot (clip 0.3 (gaussian 33 10 0 1)))

(defn rule [x predicate consequent]
  (clip (if-membership x predicate) consequent))

(defn coa [ifs]
  (% (reduce + (map #(* (nth % 1) (nth % 0)) ifs))
     (reduce + (map #(nth % 1) ifs))))

(defn if-coa [ifs]
  ;; A_{CoA} = \dfrac{\sum_{i=1}^{N} (\mu(x_{i}) + \nu(x_{i})) \mu(x_{i}) x_{i}}{\sum_{i=1}^{N} (\mu(x_{i}) + \nu(x_{i})) \mu(x_{i})}
  ;; = \dfrac{\sum_{i=1}^{N} i\mu_{A}(x) x_{i}}{\sum_{i=1}^{N} i\mu_{A}(x)}
  (% (reduce + (map #(* (* (+ (nth % 2) (nth % 1))
                           (nth % 1)) (nth % 0)) ifs))
     (reduce + (map #(* (+ (nth % 2) (nth % 1))
                        (nth % 1)) ifs))))

(defn fis-happiness [joy]
  (let [joy (* joy 100)
        low-joy (ifs (gaussian-mf 33 10 0 1)
                     (gaussian-nmf 33 10 0 1))
        high-joy (ifs (gaussian-mf 66 10 0 1)
                      (gaussian-nmf 66 10 0 1))
        low-happiness (ifs (gaussian-mf 33 10 0 1)
                           (gaussian-nmf 33 10 0 1))
        high-happiness (ifs (gaussian-mf 66 10 0 1)
                            (gaussian-nmf 66 10 0 1))

        rule1 (rule joy low-joy low-happiness)
        rule2 (rule joy high-joy high-happiness)
        ]
    (/ (if-coa (union rule1 rule2)) 100)))

(comment
  (let [results (remove #(Double/isNaN %)
                        (map (fn [_]
                               (let [params [(rand 100) (rand 30) (rand 100) (rand 30)]
                                     ;;params [33 30 33 30]
                                     input (rand 100)
                                     ;;input 30
                                     ]
                                 ;;(println params)
                                 ;;(println "input: " input)
                                 (- (fis-happiness input params)
                                    (it2-happiness input params))
                                 ))
                             (range 200)))]
    (/ (apply + results) (count results)))
  )

(defn fis-happiness [joy params]
  (let [low-joy (ifs (gaussian-mf (nth params 0) (nth params 1) 0 1)
                     (gaussian-nmf (nth params 0) (nth params 1) 0 1))
        high-joy (ifs (gaussian-mf (nth params 2) (nth params 3) 0 1)
                      (gaussian-nmf (nth params 2) (nth params 3) 0 1))
        low-happiness (ifs (gaussian-mf (nth params 0) (nth params 1) 0 1)
                           (gaussian-nmf (nth params 0) (nth params 1) 0 1))
        high-happiness (ifs (gaussian-mf (nth params 2) (nth params 3) 0 1)
                            (gaussian-nmf (nth params 2) (nth params 3) 0 1))

        rule1 (rule joy low-joy low-happiness)
        rule2 (rule joy high-joy high-happiness)
        ]
    (if-coa (union rule1 rule2))))


(defn ifis-happiness [joy]
  (let [low-joy (ifs (gaussian-mf 33 40 0 1)
                     (gaussian-nmf 33 40 0 1))
        high-joy (ifs (gaussian-mf 66 40 0 1)
                      (gaussian-nmf 66 40 0 1))
        low-happiness (ifs (gaussian-mf 33 40 0 1)
                           (gaussian-nmf 33 40 0 1))
        high-happiness (ifs (gaussian-mf 66 40 0 1)
                            (gaussian-nmf 66 40 0 1))

        rule1 (rule joy low-joy low-happiness)
        rule2 (rule joy high-joy high-happiness)
        ]
    (if-coa (union rule1 rule2))))


(comment
  (view (function-plot fis-happiness 0 1))
  (view (function-plot ifis-happiness 0 1))
  (view (function-plot it2-happiness 0 1))

  (iplot (gaussian 33 10 0 1))
  (iplot (gaussian 66 10 0 1)))

(defn t1-happiness [input]
  (let [joy (new Input "Joy" (new Tuple 0 100))
        happiness (new Output "Happiness" (new Tuple 0 100))
        rulebase (new T1_Rulebase 2)
        
        lowjoy-mf (new T1MF_Gaussian "Low Joy MF" 33 40)
        highjoy-mf (new T1MF_Gaussian "High Joy MF" 66 40)

        lowhappy-mf (new T1MF_Gaussian "Low Happy MF" 33 40)
        highhappy-mf (new T1MF_Gaussian "High Happy MF" 66 40)


        lowjoy (new T1_Antecedent "Low Joy" lowjoy-mf joy)
        highjoy (new T1_Antecedent "High Joy" highjoy-mf joy)

        lowhappy (new T1_Consequent "Low Happy" lowhappy-mf happiness)
        highhappy (new T1_Consequent "High Happy" highhappy-mf happiness)

        rule1 (new T1_Rule (into-array [lowjoy]) lowhappy)
        rule2 (new T1_Rule (into-array [highjoy]) highhappy)
        ]
    (.addRule rulebase rule1)
    (.addRule rulebase rule2)
    
    (.setInput joy input)
    (.get (.evaluate rulebase 1) happiness)
    ))

(defn it2-happiness [input]
  (let [joy (new Input "Joy" (new Tuple 0 1))
        happiness (new Output "Happiness" (new Tuple 0 1))
        rulebase (new IT2_Rulebase 2)
        
        lowjoy-umf (new T1MF_Gaussian "U Low Joy MF" 0.38 0.10)
        lowjoy-lmf (new T1MF_Gaussian "L Low Joy MF" 0.28 0.10)
        lowjoy-mf (new IntervalT2MF_Gaussian "Low Joy IT2MF" lowjoy-umf lowjoy-lmf)

        highjoy-umf (new T1MF_Gaussian "U High Joy MF" 0.71 0.10)
        highjoy-lmf (new T1MF_Gaussian "L High Joy MF" 0.61 0.10)
        highjoy-mf (new IntervalT2MF_Gaussian "High Joy IT2MF" highjoy-umf highjoy-lmf)

        lowhappy-umf (new T1MF_Gaussian "U Low Happy MF" 0.38 0.10)
        lowhappy-lmf (new T1MF_Gaussian "L Low Happy MF" 0.28 0.10)
        lowhappy-mf (new IntervalT2MF_Gaussian "Low Happy IT2MF" lowhappy-umf lowhappy-lmf)

        highhappy-umf (new T1MF_Gaussian "U High Happy MF" 0.71 0.10)
        highhappy-lmf (new T1MF_Gaussian "L High Happy MF" 0.61 0.10)
        highhappy-mf (new IntervalT2MF_Gaussian "High Happy IT2MF" highhappy-umf highhappy-lmf)

        lowjoy (new IT2_Antecedent "Low Joy" lowjoy-mf joy)
        highjoy (new IT2_Antecedent "High Joy" highjoy-mf joy)

        lowhappy (new IT2_Consequent "Low Happy" lowhappy-mf happiness)
        highhappy (new IT2_Consequent "High Happy" highhappy-mf happiness)

        rule1 (new IT2_Rule (into-array [lowjoy]) lowhappy)
        rule2 (new IT2_Rule (into-array [highjoy]) highhappy)
        ]
    (.addRule rulebase rule1)
    (.addRule rulebase rule2)
    
    (.setInput joy input)
    (.get (.evaluate rulebase 1) happiness)
    ))

(defn it2-happiness [input params]
  (let [joy (new Input "Joy" (new Tuple 0 100))
        happiness (new Output "Happiness" (new Tuple 0 100))
        rulebase (new IT2_Rulebase 2)
        
        lowjoy-umf (new T1MF_Gaussian "U Low Joy MF" (nth params 0) (nth params 1))
        lowjoy-lmf (new T1MF_Gaussian "L Low Joy MF" (nth params 0) (nth params 1))
        lowjoy-mf (new IntervalT2MF_Gaussian "Low Joy IT2MF" lowjoy-umf lowjoy-lmf)

        highjoy-umf (new T1MF_Gaussian "U High Joy MF" (nth params 2) (nth params 3))
        highjoy-lmf (new T1MF_Gaussian "L High Joy MF" (nth params 2) (nth params 3))
        highjoy-mf (new IntervalT2MF_Gaussian "High Joy IT2MF" highjoy-umf highjoy-lmf)

        lowhappy-umf (new T1MF_Gaussian "U Low Happy MF" (nth params 0) (nth params 1))
        lowhappy-lmf (new T1MF_Gaussian "L Low Happy MF" (nth params 0) (nth params 1))
        lowhappy-mf (new IntervalT2MF_Gaussian "Low Happy IT2MF" lowhappy-umf lowhappy-lmf)

        highhappy-umf (new T1MF_Gaussian "U High Happy MF" (nth params 2) (nth params 3))
        highhappy-lmf (new T1MF_Gaussian "L High Happy MF" (nth params 2) (nth params 3))
        highhappy-mf (new IntervalT2MF_Gaussian "High Happy IT2MF" highhappy-umf highhappy-lmf)

        lowjoy (new IT2_Antecedent "Low Joy" lowjoy-mf joy)
        highjoy (new IT2_Antecedent "High Joy" highjoy-mf joy)

        lowhappy (new IT2_Consequent "Low Happy" lowhappy-mf happiness)
        highhappy (new IT2_Consequent "High Happy" highhappy-mf happiness)

        rule1 (new IT2_Rule (into-array [lowjoy]) lowhappy)
        rule2 (new IT2_Rule (into-array [highjoy]) highhappy)
        ]
    (.addRule rulebase rule1)
    (.addRule rulebase rule2)
    
    (.setInput joy input)
    (.get (.evaluate rulebase 1) happiness)
    ))

(comment
  (defn ifis-mg [inputs]
    (let [at (ifs (gaussian-mf 10 5 0 1)
                  (gaussian-nmf 10 5 0 1))
          at-6 (ifs (gaussian-mf 10 5 0 1)
                    (gaussian-nmf 10 5 0 1))
          at-12 (ifs (gaussian-mf 10 5 0 1)
                     (gaussian-nmf 10 5 0 1))
          at-18 (ifs (gaussian-mf 10 5 0 1)
                     (gaussian-nmf 10 5 0 1))
          ct (ifs (gaussian-mf 10 5 0 1)
                  (gaussian-nmf 10 5 0 1))
          ct-6 (ifs (gaussian-mf 10 5 0 1)
                    (gaussian-nmf 10 5 0 1))
          ct-12 (ifs (gaussian-mf 10 5 0 1)
                     (gaussian-nmf 10 5 0 1))
          ct-18 (ifs (gaussian-mf 10 5 0 1)
                     (gaussian-nmf 10 5 0 1))

          rule1 (rule (nth inputs 0) at ct)
          rule2 (rule (nth inputs 1) at-6 ct-6)
          rule3 (rule (nth inputs 2) at-12 ct-12)
          rule4 (rule (nth inputs 3) at-18 ct-18)]
      (if-coa (reduce union [rule1 rule2 rule3 rule4]))
      )))

(defn ifis-mg [parameters inputs]
  (let [at-mf (nth (nth parameters 0) 0) at-nmf (nth (nth parameters 0) 1)
        at6-mf (nth (nth parameters 1) 0) at6-nmf (nth (nth parameters 1) 1)
        at12-mf (nth (nth parameters 2) 0) at12-nmf (nth (nth parameters 2) 1)
        at18-mf (nth (nth parameters 3) 0) at18-nmf (nth (nth parameters 3) 1)
        ct-mf (nth (nth parameters 4) 0) ct-nmf (nth (nth parameters 4) 1)
        ct6-mf (nth (nth parameters 5) 0) ct6-nmf (nth (nth parameters 5) 1)
        ct12-mf (nth (nth parameters 6) 0) ct12-nmf (nth (nth parameters 6) 1)
        ct18-mf (nth (nth parameters 7) 0) ct18-nmf (nth (nth parameters 7) 1)
        ]
    (let [at (ifs (apply gaussian-mf at-mf)
                  (apply gaussian-nmf at-nmf))
          at6 (ifs (apply gaussian-mf at6-mf)
                   (apply gaussian-nmf at6-nmf))
          at12 (ifs (apply gaussian-mf at12-mf)
                    (apply gaussian-nmf at12-nmf))
          at18 (ifs (apply gaussian-mf at18-mf)
                    (apply gaussian-nmf at18-nmf))
          ct (ifs (apply gaussian-mf ct-mf)
                  (apply gaussian-nmf ct-nmf))
          ct6 (ifs (apply gaussian-mf ct6-mf)
                   (apply gaussian-nmf ct6-nmf))
          ct12 (ifs (apply gaussian-mf ct12-mf)
                    (apply gaussian-nmf ct12-nmf))
          ct18 (ifs (apply gaussian-mf ct18-mf)
                    (apply gaussian-nmf ct18-nmf))

          rule1 (rule (nth inputs 0) at ct)
          rule2 (rule (nth inputs 1) at6 ct6)
          rule3 (rule (nth inputs 2) at12 ct12)
          rule4 (rule (nth inputs 3) at18 ct18)]
      (if-coa (reduce union [rule1 rule2 rule3 rule4]))
      )))

(comment
  (let [input [(rand 100) (rand 100) (rand 100) (rand 100)]
        at-mean (rand 100) at-sd (rand 100)
        at6-mean (rand 100) at6-sd (rand 100)
        at12-mean (rand 100) at12-sd (rand 100)
        at18-mean (rand 100) at18-sd (rand 100)
        
        ct-mean (rand 100) ct-sd (rand 100)
        ct6-mean (rand 100) ct6-sd (rand 100)
        ct12-mean (rand 100) ct12-sd (rand 100)
        ct18-mean (rand 100) ct18-sd (rand 100)
        
        ifis-params [[[at-mean at-sd 0 1] [at-mean at-sd 0 1]]
                     [[at6-mean at6-sd 0 1] [at6-mean at6-sd 0 1]]
                     [[at12-mean at12-sd 0 1] [at12-mean at12-sd 0 1]]
                     [[at18-mean at18-sd 0 1] [at18-mean at18-sd 0 1]]
                     
                     [[ct-mean ct-sd 0 1] [ct-mean ct-sd 0 1]]
                     [[ct6-mean ct6-sd 0 1] [ct6-mean ct6-sd 0 1]]
                     [[ct12-mean ct12-sd 0 1] [ct12-mean ct12-sd 0 1]]
                     [[ct18-mean ct18-sd 0 1] [ct18-mean ct18-sd 0 1]]
                     ]
        t1-params [[at-mean at-sd at-mean at-sd]
                   [at6-mean at6-sd at6-mean at6-sd]
                   [at12-mean at12-sd at12-mean at12-sd]
                   [at18-mean at18-sd at18-mean at18-sd]

                   [ct-mean ct-sd ct-mean ct-sd]
                   [ct6-mean ct6-sd ct6-mean ct6-sd]
                   [ct12-mean ct12-sd ct12-mean ct12-sd]
                   [ct18-mean ct18-sd ct18-mean ct18-sd]
                   ]]
    ;;(println input)
    (time (println (ifis-mg ifis-params
                            input)))
    (time (println (t1-mg t1-params
                          input)))
    (time (println (it2-mg t1-params
                           input)))))

(defn t1-mg [parameters input]
  (let [d 100
        at (new Input "AT" (new Tuple 0 d)) at6 (new Input "AT6" (new Tuple 0 d))
        at12 (new Input "AT12" (new Tuple 0 d)) at18 (new Input "AT18" (new Tuple 0 d))
        ct (new Output "CT" (new Tuple 0 d))
        rulebase (new T1_Rulebase 4)
        
        at-mf (new T1MF_Gaussian "AT MF" (nth (nth parameters 0) 0) (nth (nth parameters 0) 1))
        at6-mf (new T1MF_Gaussian "AT6 MF" (nth (nth parameters 1) 0) (nth (nth parameters 1) 1))
        at12-mf (new T1MF_Gaussian "AT12 MF" (nth (nth parameters 2) 0) (nth (nth parameters 2) 1))
        at18-mf (new T1MF_Gaussian "AT18 MF" (nth (nth parameters 3) 0) (nth (nth parameters 3) 1))

        ct-mf (new T1MF_Gaussian "CT MF" (nth (nth parameters 4) 0) (nth (nth parameters 4) 1))
        ct6-mf (new T1MF_Gaussian "CT6 MF" (nth (nth parameters 5) 0) (nth (nth parameters 5) 1))
        ct12-mf (new T1MF_Gaussian "CT12 MF" (nth (nth parameters 6) 0) (nth (nth parameters 6) 1))
        ct18-mf (new T1MF_Gaussian "CT18 MF" (nth (nth parameters 7) 0) (nth (nth parameters 7) 1))
        
        var-at (new T1_Antecedent "AT" at-mf at)
        var-at6 (new T1_Antecedent "AT6" at6-mf at6)
        var-at12 (new T1_Antecedent "AT12" at12-mf at12)
        var-at18 (new T1_Antecedent "AT18" at18-mf at18)

        var-ct (new T1_Consequent "CT" ct-mf ct)
        var-ct6 (new T1_Consequent "CT6" ct6-mf ct)
        var-ct12 (new T1_Consequent "CT12" ct12-mf ct)
        var-ct18 (new T1_Consequent "CT18" ct18-mf ct)

        rule1 (new T1_Rule (into-array [var-at]) var-ct)
        rule2 (new T1_Rule (into-array [var-at6]) var-ct6)
        rule3 (new T1_Rule (into-array [var-at12]) var-ct12)
        rule4 (new T1_Rule (into-array [var-at18]) var-ct18)
        ]
    (.addRule rulebase rule1)
    (.addRule rulebase rule2)
    (.addRule rulebase rule3)
    (.addRule rulebase rule4)
    
    (.setInput at (nth input 0))
    (.setInput at6 (nth input 1))
    (.setInput at12 (nth input 2))
    (.setInput at18 (nth input 3))
    
    (let [res (.get (.evaluate rulebase 1) ct)]
      (if (Double/isNaN res)
        (rand)
        res))
    ))

(defn it2-mg [parameters input]
  (let [d 100
        at (new Input "AT" (new Tuple 0 d)) at6 (new Input "AT6" (new Tuple 0 d))
        at12 (new Input "AT12" (new Tuple 0 d)) at18 (new Input "AT18" (new Tuple 0 d))
        ct (new Output "CT" (new Tuple 0 d))
        rulebase (new IT2_Rulebase 4)
        
        at-umf (new T1MF_Gaussian "U AT MF" (nth (nth parameters 0) 0) (nth (nth parameters 0) 1))
        at-lmf (new T1MF_Gaussian "L AT MF" (nth (nth parameters 0) 2) (nth (nth parameters 0) 3))
        at-mf (new IntervalT2MF_Gaussian "AT IT2MF" at-umf at-lmf)

        at6-umf (new T1MF_Gaussian "U AT6 MF" (nth (nth parameters 1) 0) (nth (nth parameters 1) 1))
        at6-lmf (new T1MF_Gaussian "L AT6 MF" (nth (nth parameters 1) 2) (nth (nth parameters 1) 3))
        at6-mf (new IntervalT2MF_Gaussian "AT6 IT2MF" at6-umf at6-lmf)
      
        at12-umf (new T1MF_Gaussian "U AT12 MF" (nth (nth parameters 2) 0) (nth (nth parameters 2) 1))
        at12-lmf (new T1MF_Gaussian "L AT12 MF" (nth (nth parameters 2) 2) (nth (nth parameters 2) 3))
        at12-mf (new IntervalT2MF_Gaussian "AT12 IT2MF" at12-umf at12-lmf)

        at18-umf (new T1MF_Gaussian "U AT18 MF" (nth (nth parameters 3) 0) (nth (nth parameters 3) 1))
        at18-lmf (new T1MF_Gaussian "L AT18 MF" (nth (nth parameters 3) 2) (nth (nth parameters 3) 3))
        at18-mf (new IntervalT2MF_Gaussian "AT18 IT2MF" at18-umf at18-lmf)

        ct-umf (new T1MF_Gaussian "U CT MF" (nth (nth parameters 4) 0) (nth (nth parameters 4) 1))
        ct-lmf (new T1MF_Gaussian "L CT MF" (nth (nth parameters 4) 2) (nth (nth parameters 4) 3))
        ct-mf (new IntervalT2MF_Gaussian "CT IT2MF" ct-umf ct-lmf)

        ct6-umf (new T1MF_Gaussian "U CT6 MF" (nth (nth parameters 5) 0) (nth (nth parameters 5) 1))
        ct6-lmf (new T1MF_Gaussian "L CT6 MF" (nth (nth parameters 5) 2) (nth (nth parameters 5) 3))
        ct6-mf (new IntervalT2MF_Gaussian "CT6 IT2MF" ct6-umf ct6-lmf)
      
        ct12-umf (new T1MF_Gaussian "U CT12 MF" (nth (nth parameters 6) 0) (nth (nth parameters 6) 1))
        ct12-lmf (new T1MF_Gaussian "L CT12 MF" (nth (nth parameters 6) 2) (nth (nth parameters 6) 3))
        ct12-mf (new IntervalT2MF_Gaussian "CT12 IT2MF" ct12-umf ct12-lmf)

        ct18-umf (new T1MF_Gaussian "U CT18 MF" (nth (nth parameters 7) 0) (nth (nth parameters 7) 1))
        ct18-lmf (new T1MF_Gaussian "L CT18 MF" (nth (nth parameters 7) 2) (nth (nth parameters 7) 3))
        ct18-mf (new IntervalT2MF_Gaussian "CT18 IT2MF" ct18-umf ct18-lmf)
        
        var-at (new IT2_Antecedent "AT" at-mf at)
        var-at6 (new IT2_Antecedent "AT6" at6-mf at6)
        var-at12 (new IT2_Antecedent "AT12" at12-mf at12)
        var-at18 (new IT2_Antecedent "AT18" at18-mf at18)

        var-ct (new IT2_Consequent "CT" ct-mf ct)
        var-ct6 (new IT2_Consequent "CT6" ct6-mf ct)
        var-ct12 (new IT2_Consequent "CT12" ct12-mf ct)
        var-ct18 (new IT2_Consequent "CT18" ct18-mf ct)

        rule1 (new IT2_Rule (into-array [var-at]) var-ct)
        rule2 (new IT2_Rule (into-array [var-at6]) var-ct6)
        rule3 (new IT2_Rule (into-array [var-at12]) var-ct12)
        rule4 (new IT2_Rule (into-array [var-at18]) var-ct18)
        ]
    (.addRule rulebase rule1)
    (.addRule rulebase rule2)
    (.addRule rulebase rule3)
    (.addRule rulebase rule4)
    
    (.setInput at (nth input 0))
    (.setInput at6 (nth input 1))
    (.setInput at12 (nth input 2))
    (.setInput at18 (nth input 3))
    
    (let [res (.get (.evaluate rulebase 1) ct)]
      (if (Double/isNaN res)
        (rand)
        res))
    ))

(comment
  (view (function-plot fis-happiness 0 1))
  (view (function-plot ifis-happiness 0 1))
  (view (function-plot it2-happiness 0 1))

  (iplot (gaussian 33 10 0 1))
  (iplot (gaussian 66 10 0 1)))

(defn fwrap [points]
  #(nth points %))

(defn mf [imf-points]
  (fwrap (map #(nth % 1) imf-points)))

(defn nmf [imf-points]
  (fwrap (map #(nth % 2) imf-points)))

;;(view (function-plot (fwrap *mg) 0 1000 :step-size 1))

(defn iplot
  ([imf]
   (doto
       (function-plot (mf imf) 0 100 :step-size 1 :y-label "Membership & Non-Membership" :x-label "x")
     (add-function (nmf imf) 0 100 :step-size 1)
     view))
  ([mf nmf]
   (doto
       (function-plot (mf mf) 0 100 :step-size 1 :y-label "Membership & Non-Membership" :x-label "x")
     (add-function (mf nmf) 0 100 :step-size 1))))

(defn coa-iplot
  ([imf]
   (doto
       (function-plot (mf imf) 0 100 :step-size 1 :y-label "Output" :x-label "Mean")
     (add-function (nmf imf) 0 100 :step-size 1)
     view))
  ([mf nmf]
   (doto
       (function-plot (mf mf) 0 100 :step-size 1 :y-label "Output" :x-label "Mean")
     (add-function (mf nmf) 0 100 :step-size 1))))

;; Gajure

(comment
  (ifis-mg [[10 5 0 1] [10 5 0 1]
            [10 5 0 1] [10 5 0 1]
            [10 5 0 1] [10 5 0 1]
            [10 5 0 1] [10 5 0 1]
            [10 5 0 1] [10 5 0 1]
            [10 5 0 1] [10 5 0 1]
            [10 5 0 1] [10 5 0 1]
            [10 5 0 1] [10 5 0 1]]
           [0 0 0 0])
  )

(defn ifis-pop-gen [num-pop]
  (map (fn [_]
         (apply concat (map (fn [_]
                              (let [mf-max (+ (rand (- 1 0.95)) 0.95)
                                    nmf-max (- 1 mf-max)

                                    mf-max (+ (rand (- 0.95 0)) 0)
                                    nmf-max (+ (rand (- 1 0.6)) 0.6)
                                    
                                    mean1 (+ (rand (- 100 0)) 0)
                                    mean2 (+ (rand (- (+ mean1 5) (- mean1 5))) (- mean1 5))
                                    sd1 (+ (rand (- 100 0)) 0)
                                    sd2 (+ (rand (- (+ sd1 5) (- sd1 5))) (- sd1 5))
                                    ]
                                [[(list mean1 sd1 0 1)
                                  (list mean1 sd1 0 nmf-max)]]))
                            (range 8))))
       (range num-pop)))

(defn t1-pop-gen [num-pop]
  (map (fn [_]
         (apply concat (map (fn [_]
                              (let [mean1 (+ (rand (- 100 0)) 0)
                                    sd1 (+ (rand (- 100 0)) 0)
                                    ]
                                [[mean1 sd1 :im-here-to-diff]]))
                            (range 8))))
       (range num-pop)))

(defn it2-pop-gen [num-pop]
  (map (fn [_]
         (apply concat (map (fn [_]
                              (let [mean1 (+ (rand (- 100 25)) 25)
                                    mean2 (+ (rand (- mean1 (- mean1 20))) (- mean1 20))
                                    sd1 (+ (rand (- 100 25)) 25)
                                    sd2 (+ (rand (- sd1 (- sd1 20))) (- sd1 20))
                                    ]
                                [[mean1 sd1 mean1 sd2]]))
                            (range 8))))
       (range num-pop)))
;; ya le ganamos con 1Mean

(defn ifis-fitness [dataset lst]
  (/ (apply + (map (fn [p r]
                     (Math/pow (- p (nth r 4)) 2)
                     )
                   (scale (map (fn [tuple]
                                 (ifis-mg lst [(nth tuple 0) (nth tuple 1)
                                               (nth tuple 2) (nth tuple 3)]))
                               dataset) 0 100)
                   dataset))
     (count dataset)))

(defn t1-fitness [dataset lst]
  (/ (apply + (map (fn [p r]
                     (Math/pow (- p (nth r 4)) 2)
                     )
                   (scale (map (fn [tuple]
                                 (t1-mg lst [(nth tuple 0) (nth tuple 1)
                                              (nth tuple 2) (nth tuple 3)]))
                               dataset) 0 100)
                   dataset))
     (count dataset)))

(defn it2-fitness [dataset lst]
  (/ (apply + (map (fn [p r]
                     (Math/pow (- p (nth r 4)) 2)
                     )
                   (scale (map (fn [tuple]
                                 (it2-mg lst [(nth tuple 0) (nth tuple 1)
                                              (nth tuple 2) (nth tuple 3)]))
                               dataset) 0 100)
                   dataset))
     (count dataset)))

(comment
  (iplot (ifs (apply gaussian-mf '(96.69305100875314 19.984387365214655 0 0.6278421445797916))
              (apply gaussian-nmf '(85.7257938487251 18.146139247268575 0 0.3721578554202084))))

  (def coco
    (iplot (ifs (apply gaussian-mf '(96.69305100875314 19.984387365214655 0 0.6278421445797916))
                (apply gaussian-nmf '(85.7257938487251 18.146139247268575 0 0.3721578554202084))))))

(defn imf? [imf]
  (empty? (filter #(> % 1)
                  (map #(+ (nth % 1) (nth % 2)) imf))))

;; ver la gráfica de it2 vs int

(comment
  (let [mf-max (+ (rand (- 1 0.9)) 0.9)
        nmf-max (- 1 mf-max)
        mf-max 1

        nmf-min (+ (rand (- 0.3 0.0)) 0.0)
        nmf-max (+ (rand (- 0.2 0.0)) 0.0)
        
        mean1 (rand 100)
        mean2 (rand mean1)
        sd1 (rand 100)
        sd2 (+ (rand (- 1 sd1)) sd1)
        ]
    (map #(+ (nth % 1) (nth % 2))
         (ifs (gaussian-mf mean1 sd1 0 1)
              (gaussian-nmf mean1 sd1 0 nmf-max))))
  )

(comment
  (iplot (gaussian 50 15 0 1))
  (iplot (ifs (gaussian-mf 50 15 0 1)
              (gaussian-nmf 50 15 0 0.2))))

(comment
  (do
    ;; (println "IFIS")
    ;; (println "1Mean(0-100)")
    ;; (println "SD1(30-70), SD2(SD1+-10)")
    ;; (println "Height(MF-MAX)")
    ;; (println "")

    ;; (println "T1")
    ;; (println "1Mean(0-100)")
    ;; (println "SD1(30-70)")
    ;; (println "")
    
    ;; (println "IT2")
    ;; ;;(println "Mean1(0-100), Mean2(Mean1-10 - Mean1)")
    ;; (println "1Mean(0-100)")
    ;; (println "SD1(30-70), SD2(SD1-10 - SD1)")
    ;; (println "")

    (map (fn [i]
           ;;(println "IFIS #" (inc i))
           
           ;; (run-ga {:fit-fn (memoize (partial ifis-fitness *mg-train)) :mut-fn generic-mutation
           ;;          :sel-fn roulette-select :init-fn ifis-pop-gen
           ;;          :cross-fn list-crossover}
           ;;         {:pop-sz 100 :children 5 :mut-r 1 :gen 20})
           
           ;;(println "")

           ;; (println "T1 #" (inc i))
           
           ;; (run-ga {:fit-fn (memoize (partial t1-fitness *mg-train)) :mut-fn generic-mutation
           ;;          :sel-fn roulette-select :init-fn t1-pop-gen
           ;;          :cross-fn list-crossover}
           ;;         {:pop-sz 100 :children 5 :mut-r 1 :gen 20})
           
           ;; (println "")
           
           ;; (println "IT2 #" (inc i))
           
           (run-ga {:fit-fn (memoize (partial it2-fitness *mg-train )) :mut-fn generic-mutation
                    :sel-fn roulette-select :init-fn it2-pop-gen
                    :cross-fn list-crossover}
                   {:pop-sz 100 :children 5 :mut-r 1 :gen 20})
           
           ;; (println "")
           
           ;;(println "")
           )
         (range 400)))
  )
