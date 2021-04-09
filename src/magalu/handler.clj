(ns magalu.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [magalu.amnesia :as amnesia]))

(s/defschema Produto
  {(s/optional-key :id) s/Num
   :nome                s/Str
   :valor               s/Num
   :quantidade          s/Num})

(s/defschema Pedido
  {(s/optional-key :id) s/Num
   :itens               [Produto]})

(def db (amnesia/cria-db {:pedidos  []
                          :produtos []}))

(def index (atom 1))

(def index-produto (atom 1))

(defn salvar-pedido [pedido]
  (let [pedido-indexado (assoc pedido :id @index)]
    (amnesia/adiciona-item db :pedidos pedido-indexado)
    (swap! index inc)
    pedido-indexado))

(defn salvar-produto [produto]
  (let [produto-indexado (assoc produto :id @index-produto)]
    (amnesia/adiciona-item db :produtos produto-indexado)
    (swap! index-produto inc)
    produto-indexado))

(def app
  (api
    {:swagger
     {:ui   "/"
      :spec "/swagger.json"
      :data {:info {:title       "Magalu"
                    :description "API teste magazine luiza"}
             :tags [{:name        "api"
                     :description "some apis"}]}}}

    (context "/api" []
      :tags ["api"]

      (context "/pedidos" []
        :tags ["pedidos"]

        (GET "/" []
          :return [Pedido]
          :summary "Listar todos os pedidos"
          (ok (:pedidos @db)))

        (GET "/:id" [id]
          :return Pedido
          :summary "Retornar o pedido por id."
          (if-let [pedido (first (amnesia/pesquisa-item db :pedidos #(= (Integer/parseInt id) (:id %))))]
            (ok pedido)
            (not-found)))

        (POST "/" []
          :return Pedido
          :body [pedido Pedido]
          :summary "Fazer um pedido"
          (created "" (salvar-pedido pedido)))

        (DELETE "/:id" [id]
          :summary "Excluir um pedido"
          (amnesia/remove-item db :pedidos #(= (Integer/parseInt id) (:id %)))
          (ok))

        (PUT "/:id" [id]
          :return [Pedido]
          :body [pedido Pedido]
          :summary "Corrigir um pedido"
          (let [novo-pedido (assoc pedido :id (Integer/parseInt id))]
            (amnesia/atualiza-item db :pedidos #(= (Integer/parseInt id) (:id %)) novo-pedido)
            (ok (:pedidos @db)))))

      (context "/produtos" []
        :tags ["produtos"]

        (GET "/" []
          :return [Produto]
          :summary "Listar todos os produtos"
          (ok (:produtos @db)))

        (GET "/:id" [id]
          :return Produto
          :summary "Retornar um produto"
          (if-let [produto (first(amnesia/pesquisa-item db :produtos #(= (Integer/parseInt id) (:id %))))]
            (ok produto)
            (not-found)))

        (POST "/" []
          :return Produto
          :body [produto Produto]
          :summary "Criar um produto"
          (created "" (salvar-produto produto)))

        (DELETE "/:id" [id]
          :summary "Deletar produto"
          (amnesia/remove-item db :produtos #(= (Integer/parseInt id) (:id %)))
          (ok))

        (PUT "/:id" [id]
          :return [Produto]
          :body [produto Produto]
          :summary "Corrigir um produto"
          (let [novo-produto (assoc produto :id (Integer/parseInt id))]
            (amnesia/atualiza-item db :produtos #(= (Integer/parseInt id) (:id %)) novo-produto)
            (ok (:produtos @db))))

        ))))
