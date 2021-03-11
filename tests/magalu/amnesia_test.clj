(ns magalu.amnesia-test
  (:require [clojure.test :refer :all]
            [magalu.amnesia :as amnesia]))

(deftest db-features
  (testing "Posso iniciar um novo db"
    (is (not (nil? (amnesia/cria-db))))
    (is (= )))

  (testing "Posso adicionar uma nova coleção"
    (is false))

  (testing "Posso remover um coleção"
    (is false))

  (testing "Posso adicionar um novo item a uma coleção"
    (is false))

  (testing "Posso remover um item de uma coleção"
    (is false))

  (testing "Posso remover vários itens de uma coleção"
    (is false))

  (testing "Posso remover todos os itens de uma coleção"
    (is false))

  (testing "Posso pesquisar itens de uma coleção"
    (is false))

  (testing "Posso atualizar um item uma coleção"
    (is false)))
