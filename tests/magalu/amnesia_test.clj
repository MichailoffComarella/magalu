(ns magalu.amnesia-test
  (:require [clojure.test :refer :all]
            [magalu.amnesia :as amnesia]
            [schema.core :as s
             :include-macros true]))

(deftest quando-adiciona-uma-colecao
  (let [db (amnesia/cria-db)]
    (amnesia/adiciona-colecao :produtos amnesia/Produto db)

    (testing "Uma coleção é adicionada ao db"
      (is (contains? @db :produtos)))

    (testing "A coleção adicionada possui um indice"
      (is (contains? (:produtos @db) :indice)))

    (testing "A coleção adicionada possui um vetor de itens"
      (is (contains? (:produtos @db) :itens)))

    (testing "O indice é um atom"
      (is (instance? clojure.lang.Atom (get-in @db [:produtos :indice]))))

    (testing "O indice é iniciado com o valor 0"
      (is (= @(get-in @db [:produtos :indice]) 0)))

    (testing "A coleção adicionada possui um modelo"
      (is (contains? (:produtos @db) :modelo)))))

(deftest quando-remove-uma-colecao
  (let [db (amnesia/cria-db {:produtos {:indice (atom 0)
                                        :itens  []}})]
    (amnesia/remove-colecao :produtos db)

    (testing "A coleção não está mais no banco de dados"
      (is (not (contains? @db :produtos))))))

(deftest quando-adiciona-um-item-valido
  (let [db (amnesia/cria-db {:produtos {:indice (atom 0)
                                        :modelo amnesia/Produto
                                        :itens  []}})
        resultado (amnesia/adiciona-item :produtos {:nome "nome" :valor 123 :quantidade 321} db)]

    (testing "Retorna informações sobre a operação"
      (is (= resultado
             (amnesia/->Resultado 1 [{:id 1 :nome "nome" :valor 123 :quantidade 321}] {}))))

    (testing "O item é adicionado a coleção"
      (is (some #(= {:id 1 :nome "nome" :valor 123 :quantidade 321} %) (get-in @db [:produtos :itens]))))

    (testing "O indice é incrementado"
      (is (= @(get-in @db [:produtos :indice]) 1)))

    (testing "O item recebe um id"
      (is (some #(= (:id %) 1) (get-in @db [:produtos :itens]))))

    (testing "O id é o indice atual"
      (is (= (some :id (get-in @db [:produtos :itens]))
             @(get-in @db [:produtos :indice]))))))

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
      (amnesia/adiciona-colecao "colecao" amnesia/Produto db)

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
    (let [db (amnesia/cria-db {:produtos {:indice (atom 0)
                                          :modelo amnesia/Produto
                                          :itens  []}})
          resultado (amnesia/adiciona-item :produtos {:nome "nome" :valor 123 :quantidade 321} db)]

      (is (some #(= % {:id 1 :nome "nome" :valor 123 :quantidade 321}) (get-in @db [:produtos :itens])))
      (is (= @(get-in @db [:produtos :indice]) 1))
      (is (some #(= (:id %) 1) (get-in @db [:produtos :itens])))
      (is (= (some :id (get-in @db [:produtos :itens]))
             @(get-in @db [:produtos :indice])))
      (is (= resultado
             (amnesia/->Resultado 1 [{:id 1 :nome "nome" :valor 123 :quantidade 321}] {})))))

  (testing "Posso remover um item de uma coleção"
    (let [db (amnesia/cria-db {:produtos {:indice (atom 1)
                                          :itens  [{:id 1 :nome "alguma coisa"}]}})
          resultado (amnesia/remove-item :produtos #(= (:id %) 1) db)]

      (is (empty? (get-in @db [:pedidos :itens])))
      (is (= resultado
             (amnesia/->Resultado 1 [{:id 1 :nome "alguma coisa"}] {})))))

  (testing "Posso remover itens de uma coleção"
    (let [db (amnesia/cria-db {:pedidos {:indice (atom 5)
                                         :modelo amnesia/Pedido
                                         :itens  [{:id 1 :nome "nome"}
                                                  {:id 3 :nome "nome"}
                                                  {:id 5 :nome "nome"}]}})
          resultado (amnesia/remove-item :pedidos #(odd? (:id %)) db)]

      (is (empty? (get-in @db [:pedidos :itens])))
      (is (= resultado
             (amnesia/->Resultado 3 [{:id 1 :nome "nome"}, {:id 3 :nome "nome"}, {:id 5 :nome "nome"}] {})))))

  (testing "Posso remover todos os itens de uma coleção"
    (let [db (amnesia/cria-db {:pedidos {:indice (atom 5)
                                         :modelo amnesia/Pedido
                                         :itens  [{:id 1 :nome "nome"}
                                                  {:id 3 :nome "nome"}
                                                  {:id 5 :nome "nome"}]}})
          resultado (amnesia/remove-todos-itens :pedidos db)]

      (is (empty? (get-in @db [:pedidos :itens])))
      (is (= resultado
             (amnesia/->Resultado 3 [{:id 1 :nome "nome"}, {:id 3 :nome "nome"}, {:id 5 :nome "nome"}] {})))))

  (testing "Posso pesquisar itens de uma coleção"
    (let [db (amnesia/cria-db {:produtos {:indice (atom 1)
                                          :modelo amnesia/Produto
                                          :itens  [{:id 1 :nome "nome" :valor 123 :quantidade 321}]}})]

      (is (= (amnesia/->Resultado 1 [{:id 1 :nome "nome" :valor 123 :quantidade 321}] {})
             (amnesia/pesquisa-item :produtos #(= (:id %) 1) db)))))

  (testing "Posso atualizar um item uma coleção"
    (let [db (amnesia/cria-db {:produtos {:indice (atom 1)
                                          :modelo amnesia/Produto
                                          :itens  [{:id 1 :nome "alguma coisa"}]}})
          resultado (amnesia/atualiza-item :produtos #(= (:id %) 1) {:nome "novo nome" :alterou? "sim!"} db)]

      (is (some #(= {:id 1 :nome "novo nome" :alterou? "sim!"} %) (get-in @db [:produtos :itens])))
      (is (not (some #(= {:id 1 :nome "alguma coisa"} %) (get-in @db [:produtos :itens]))))
      (is (= resultado
             (amnesia/->Resultado 1 [{:id 1 :nome "novo nome" :alterou? "sim!"}] {})))))

  (testing "Atualizar um item inexistente não modifica a coleção"
    (let [db (amnesia/cria-db {:pedidos {:indice (atom 1)
                                         :itens  []}})
          resultado  (amnesia/atualiza-item :pedidos #(= 1 (:id %)) {:id 1 :nome "novo item"} db)]

      (is (empty? (get-in @db [:pedidos :itens])))
      (is (= resultado
             (amnesia/->Resultado 0 [] {}))))))

