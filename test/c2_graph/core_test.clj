(ns c2-graph.core-test
  (:require [c2-graph.core :refer :all]
            [clojure.java.io :as io]
            [clojure.test :refer :all]))

(deftest test-extract-url

  (is (= "fullSearch"
         (extract-url {:tag :a, :attrs {:href "fullSearch",
                                        :rel "nofollow"},
                       :content '("Aalbert Torsius")})))
  (is (= nil
         (extract-url {}))))

(deftest test-wiki-url?
  (is (= true
         (wiki-url? "wiki?url")))
  (is (= false
         (wiki-url? "not-a-wiki"))))

(deftest test-extract-links
  (is (= '() (extract-links {})))
  (is (= [{:tag :a, :attrs {:href "fullSearch"}}]
         (extract-links {:tag :span, :content
                         '({:tag :a, :attrs {:href "fullSearch"}})}))))

(deftest test-get-all-link-names
  (is (= []
         (get-all-link-names
          {:tag :span, :content '()})))
  (is (= ["TestName"]
         (get-all-link-names
          {:tag :span, :content
           '({:tag :a, :attrs {:href "wiki?TestName"}})})))
  (is (= ["TestName"]
         (get-all-link-names
          {:tag :span, :content
           '({:tag :a, :attrs {:href "wiki?TestName"}}
             {:tag :a, :attrs {:href "not-a-wiki-link"}})}))))

(deftest test-get-link-name
  (is (= nil
         (get-link-name "not-a-wiki-link")))

  (is (= "TestName"
         (get-link-name "wiki?TestName"))))

(deftest test-relative-to
  (is (= (as-path "pages/TestPath")
         ((relative-to (absolute-resources-dir))
          (as-absolute-path "resources/pages/TestPath")))))

(deftest test-combine-page-maps
  (is (= {"TestPage" ["LinkOne" "LinkTwo"]}
         (combine-page-maps {}
                            {:page-name "TestPage"
                             :links ["LinkOne" "LinkTwo"]})))
  (is (= {"TestOne" [1 2]
          "TestTwo" [3 4]}
         (combine-page-maps {"TestOne" [1 2]}
                            {:page-name "TestTwo"
                             :links [3 4]}))))
