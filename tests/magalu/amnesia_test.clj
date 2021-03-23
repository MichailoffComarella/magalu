(ns magalu.amnesia-test
  (:require [clojure.test :refer :all]
            [magalu.amnesia :as amnesia]))

(deftest db-features
  (testing "Posso iniciar um novo db"
    (let [db (amnesia/cria-db)]
      (is (not (nil? db)))
      (is (instance? clojure.lang.Atom db))))

  (testing "Posso criar um novo db com estado inicial"
    (let [db (amnesia/cria-db {:produtos [] :pedidos []})]
      (is (contains? @db :produtos))
      (is (contains? @db :pedidos))))

  (testing "Posso adicionar uma nova coleção"
    (let [db (amnesia/cria-db)]
      (amnesia/adiciona-colecao db "colecao")
      (is (not (empty? @db)))
      (is (contains? @db "colecao"))
      (is (= [] (get @db "colecao")))))

  (testing "Posso remover uma coleção"
    (let [db (amnesia/cria-db {:produtos [] :pedidos []})]
      (amnesia/remove-colecao db :produtos)
      (is (not (contains? @db :produtos)))))

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
