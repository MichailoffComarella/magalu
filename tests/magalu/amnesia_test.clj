(ns magalu.amnesia-test
  (:require [clojure.test :refer :all]
            [magalu.amnesia :as amnesia]))

(deftest db-features
  (testing "Posso iniciar um novo db"
      (is (not (nil? (amnesia/cria-db))))
      (is (instance? clojure.lang.Atom (amnesia/cria-db))))

  (testing "Posso adicionar uma nova coleção"
    (let [db (amnesia/cria-db)
          colecao {:nome "Max" :idade 33}]
      (amnesia/adiciona-colecao db colecao)
      (is (not (empty? @db)))
      (is (= @db [colecao]))))

  (testing "Posso criar uma coleção"
    (let [db (amnesia/cria-db)
          colecao (amnesia/cria-colecao db "nome")]
      (is (not (empty? @db)))))

  (testing "Posso remover uma coleção"
    (let [db (amnesia/cria-db)
          colecao {:nome "Max" :idade 33}]
      (swap! db conj colecao)
      (reset! db [])
      (is (empty? @db))))

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
