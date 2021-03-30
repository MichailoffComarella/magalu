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
    (let [db (amnesia/cria-db {:pedidos [] :produtos []})]
      (amnesia/adiciona-item db :pedidos "qualquer coisa")
      (is (some #(= % "qualquer coisa") (:pedidos @db)))))

  (testing "Posso remover um item de uma coleção"
    (let [db (amnesia/cria-db {:pedidos ["alguma coisa"] :produtos ["qualquer coisa"]})]
      (amnesia/remove-item db :pedidos "alguma coisa")
      (is (not (some #(= % ["alguma coisa"]) (:pedidos @db))))))

  (testing "Posso remover vários itens de uma coleção"
    (let [db (amnesia/cria-db {:pedidos ["alguma coisa" "mais alguma coisa" "ultima coisa"]
                               :produtos ["qualquer coisa"]})]
      (amnesia/remove-itens db :pedidos ["mais alguma coisa" "ultima coisa"])
      (is (not (some #(and (= % "mais alguma coisa")
                           (= % "ultima coisa"))
                     (:pedidos db))))))

  (testing "Posso remover todos os itens de uma coleção"
    (let [db (amnesia/cria-db {:pedidos ["alguma coisa" "mais alguma coisa" "ultima coisa"]
                               :produtos ["qualquer coisa"]})]
      (amnesia/remove-todos-itens db :pedidos)
      (is (empty? (:pedidos db)))))

  (testing "Posso pesquisar itens de uma coleção por nome"
    (let [db (amnesia/cria-db {:pedidos ["alguma coisa" "mais alguma coisa" "ultima coisa"]
                               :produtos ["qualquer coisa"]})
          resultado (first (amnesia/pesquisar-nome db :pedidos "alguma coisa"))]
      (is (= "alguma coisa" resultado))))

  (testing "Posso pesquisar itens de uma coleção por posição"
    (let [db (amnesia/cria-db {:pedidos ["alguma coisa" "mais alguma coisa" "ultima coisa"]
                               :produtos ["qualquer coisa"]})
          resultado (amnesia/pesquisar-posicao db :pedidos 0)]
      (is (= "alguma coisa" resultado))))

  (testing "Posso atualizar um item uma coleção por nome"
    (let [db (amnesia/cria-db {:pedidos ["alguma coisa" "mais alguma coisa" "ultima coisa"]
                               :produtos ["qualquer coisa"]})]
      (amnesia/atualiza-item-nome db :pedidos "alguma coisa" "nova coisa")
     (is (and (some #(= % "nova coisa") (:pedidos @db))
              (not (some #(= % "alguma coisa") (:pedidos @db))))))))