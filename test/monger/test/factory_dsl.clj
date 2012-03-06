(ns monger.test.factory-dsl
  (:use     [clojure.test]
            [monger testing joda-time]
            [monger.test.fixtures]
            [clj-time.core :only [days ago weeks now]])
  (:require [monger.collection  :as mc]
            [monger.test.helper :as helper])
  (:import [org.bson.types ObjectId]
           [org.joda.time DateTime]))


(helper/connect!)

(use-fixtures :each purge-domains purge-pages)



(defaults-for "domains"
  :ipv6-enabled false)

(factory "domains" "clojure"
         :name       "clojure.org"
         :created-at (-> 2 days ago))

(factory "domains" "elixir"
         :name     "elixir-lang.org"
         :created-at (fn [] (now))
         :topics     (fn [] ["programming" "erlang" "beam" "ruby"])
         :related    {
                      :terms (fn [] ["erlang" "python" "ruby"])
                      })

(factory "pages" "http://clojure.org/rationale"
         :name "/rationale"
         :created-at (-> 2 days ago)
         ;; :domain-id  (parent-id "domains" "clojure")
         )

(deftest test-building-documents-from-a-factory-case-1
  (let [t   (-> 2 weeks ago)
        doc (build "domains" "clojure" :created-at t)]
    (is (:_id doc))
    (is (= t (:created-at doc)))
    (is (= "clojure.org" (:name doc)))
    (is (false? (:ipv6-enabled doc)))))

(deftest test-building-documents-from-a-factory-case-2
  (let [oid (ObjectId.)
        doc (build "domains" "clojure" :_id oid)]
    (is (= oid (:_id doc)))
    (is (= "clojure.org" (:name doc)))
    (is (false? (:ipv6-enabled doc)))))

(deftest test-building-documents-from-a-factory-case-3
  (let [oid (ObjectId.)
        t   (-> 3 weeks ago)
        doc (build "domains" "clojure" :_id oid :created-at t :name "clojurewerkz.org" :ipv6-enabled true)]
    (is (= oid (:_id doc)))
    (is (= t (:created-at doc)))
    (is (= "clojurewerkz.org" (:name doc)))
    (is (:ipv6-enabled doc))))


(deftest test-building-documents-from-a-factory-case-4
  (let [doc (build "domains" "elixir")]
    (is (:_id doc))
    (is (instance? DateTime (:created-at doc)))
    (is (= ["erlang" "python" "ruby"] (get-in doc [:related :terms])))
    (is (= "elixir-lang.org" (:name doc)))
    (is (not (:ipv6-enabled doc)))))




(deftest test-seeding-documents-using-a-factory-case1
  (is (mc/empty? "domains"))
  (let [t   (-> 2 weeks ago)
        doc (seed "domains" "clojure" :created-at t)]
    (is (= 1 (mc/count "domains")))
    (is (:_id doc))
    (is (= t (:created-at doc)))
    (is (= "clojure.org" (:name doc)))
    (is (false? (:ipv6-enabled doc)))))
