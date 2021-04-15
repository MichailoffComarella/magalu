(ns magalu.amnesia-test
  (:require [clojure.test :refer :all]
            [magalu.amnesia :as amnesia]))

(deftest quando-adiciona-uma-colecao
  (let [db (amnesia/cria-db)]
    (amnesia/adiciona-colecao :colecao db)

    (testing "Uma coleção é adicionada ao db"
      (is (contains? @db :colecao)))

    (testing "A coleção adicionada possui um indice"
      (is (contains? (:colecao @db) :indice)))

    (testing "A coleção adicionada possui um vetor de itens"
      (is (contains? (:colecao @db) :itens)))

    (testing "O indice é um atom"
      (is (instance? clojure.lang.Atom (get-in @db [:colecao :indice]))))

    (testing "O indice é iniciado com o valor 0"
      (is (= @(get-in @db [:colecao :indice]) 0)))))

(deftest quando-remove-uma-colecao
  (let [db (amnesia/cria-db {:colecao {:indice (atom 0)
                                       :itens  []}})]
    (amnesia/remove-colecao :colecao db)

    (testing "A coleção não está mais no banco de dados"
      (is (not (contains? @db :colecao))))))

(deftest quando-adiciona-um-item
  (let [db (amnesia/cria-db {:colecao {:indice (atom 0)
                                       :itens  []}})]
    (amnesia/adiciona-item :colecao {:nome "nome"} db)

    (testing "O item é adicionado a coleção"
      (is (some #(= {:id 1 :nome "nome"} %) (get-in @db [:colecao :itens]))))

    (testing "O indice é incrementado"
      (is (= @(get-in @db [:colecao :indice]) 1)))

    (testing "O item recebe um id"
      (is (some #(= (:id %) 1) (get-in @db [:colecao :itens]))))

    (testing "O id é o indice atual"
      (is (= (some :id (get-in @db [:colecao :itens])))
          @(get-in @db [:colecao :indice])))))

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
      (amnesia/adiciona-colecao "colecao" db)

      (is (not (empty? @db)))
      (is (contains? @db "colecao"))
      (is (contains? (@db "colecao") :indice))
      (is (contains? (@db "colecao") :itens))
      (is (instance? clojure.lang.Atom (get-in @db ["colecao" :indice])))
      (is (= @(get-in @db ["colecao" :indice])))))

  (testing "Posso remover uma coleção"
    (let [db (amnesia/cria-db {:produtos [] :pedidos []})]
      (amnesia/remove-colecao :produtos db)

      (is (not (contains? @db :produtos)))))

  (testing "Posso adicionar um novo item a uma coleção"
    (let [db (amnesia/cria-db {:pedidos {:indice (atom 0)
                                         :itens  []}})]
      (amnesia/adiciona-item :pedidos {:nome "qualquer coisa"} db)

      (is (some #(= % {:id 1 :nome "qualquer coisa"}) (get-in @db [:pedidos :itens])))
      (is (= @(get-in @db [:pedidos :indice]) 1))
      (is (some #(= (:id %) 1) (get-in @db [:pedidos :itens])))
      (is (= (some :id (get-in @db [:pedidos :itens]))
             @(get-in @db [:pedidos :indice])))))

  (testing "Posso remover um item de uma coleção"
    (let [db (amnesia/cria-db {:pedidos {:indice (atom 1)
                                         :itens  [{:id 1 :nome "alguma coisa"}]}})]
      (amnesia/remove-item :pedidos #(= (:id %) 1) db)

      (is (empty? (get-in @db [:pedidos :itens])))))

  (testing "Posso remover itens de uma coleção"
    (let [db (amnesia/cria-db {:pedidos {:indice (atom 5)
                                         :itens  [{:id 1 :nome "nome"}
                                                  {:id 3 :nome "nome"}
                                                  {:id 5 :nome "nome"}]}})]
      (amnesia/remove-item :pedidos #(odd? (:id %)) db)

      (is (empty? (get-in @db [:pedidos :itens])))))

  (testing "Posso remover todos os itens de uma coleção"
    (let [db (amnesia/cria-db {:pedidos {:indice (atom 5)
                                         :itens  [{:id 1 :nome "nome"}
                                                  {:id 3 :nome "nome"}
                                                  {:id 5 :nome "nome"}]}})]
      (amnesia/remove-todos-itens :pedidos db)

      (is (empty? (get-in @db [:pedidos :itens])))
      (is (= @(get-in @db [:pedidos :indice]) 0))))

  (testing "Posso pesquisar itens de uma coleção"
    (let [db (amnesia/cria-db {:pedidos {:indice (atom 1)
                                         :itens  [{:id 1 :nome "nome"}]}})]

      (is (= [{:id 1 :nome "nome"}]
            (amnesia/pesquisa-item :pedidos #(= (:id %) 1) db)))))

  (testing "Posso atualizar um item uma coleção"
    (let [db (amnesia/cria-db {:pedidos {:indice (atom 1)
                                         :itens  [{:id 1 :nome "alguma coisa"}]}})]
      (amnesia/atualiza-item :pedidos #(= (:id %) 1) {:nome "novo nome" :alterou? "sim!"} db)

      (is (some #(= {:id 1 :nome "novo nome" :alterou? "sim!"} %) (get-in @db [:pedidos :itens])))
      (is (not (some #(= {:id 1 :nome "alguma coisa"} %) (get-in @db [:pedidos :itens]))))))

  (testing "Atualizar um item inexistente não modifica a coleção"
    (let [db (amnesia/cria-db {:pedidos {:indice (atom 1)
                                         :itens  []}})]

      (amnesia/atualiza-item :pedidos #(= 1 (:id %)) {:id 1 :nome "novo item"} db)
      (is (empty? (get-in @db [:pedidos :itens]))))))