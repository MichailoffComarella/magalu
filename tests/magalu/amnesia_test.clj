(ns magalu.amnesia-test
  (:require [clojure.test :refer :all]
            [magalu.amnesia :as amnesia]))

(deftest db-features
  (testing "Posso iniciar um novo db"
    (let [db (amnesia/cria-db)]
      (is (not (nil? db)))
      (is (instance? clojure.lang.Atom db))))

  (testing "Posso adicionar uma nova coleção"
    (let [db (amnesia/cria-db)
          colecao [1 2 3 4]]
      (swap! db conj colecao)
      (is false)))

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
