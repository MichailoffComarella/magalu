(ns magalu.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(s/defschema Produto
  {(s/optional-key :id) s/Num
   :nome                s/Str
   :valor               s/Num
   :quantidade          s/Num})

(s/defschema Pedido
  {(s/optional-key :id) s/Num
   :itens               [Produto]})

(def db (atom []))

(def index (atom 1))

(def index-produto (atom 1))

(def db-produto (atom []))

(defn salvar-pedido [pedido]
  (let [pedido-indexado (assoc pedido :id @index)]
    (swap! db conj pedido-indexado)
    (swap! index inc)
    pedido-indexado))

(defn salvar-produto [produto]
  (let [produto-indexado (assoc produto :id @index-produto)]
    (swap! db-produto conj produto-indexado)
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
          (ok @db))

        (GET "/:id" [id]
          :return Pedido
          :summary "Retornar o pedido por id."
          (if-let [pedido (first (filter #(= (Integer/parseInt id) (:id %)) @db))]
            (ok pedido)
            (not-found)))

        (POST "/" []
          :return Pedido
          :body [pedido Pedido]
          :summary "Fazer um pedido"
          (created "" (salvar-pedido pedido)))

        (DELETE "/:id" [id]
          :summary "Excluir um pedido"
          (let [pedidos (remove #(= (Integer/parseInt id) (:id %)) @db)]
            (reset! db pedidos)
            (ok)))

        (PUT "/:id" [id]
          :return [Pedido]
          :body [pedido Pedido]
          :summary "Corrigir um pedido"
          (let [pedidos (remove #(= (Integer/parseInt id) (:id %)) @db)]
            (reset! db pedidos)
            (swap! db conj (assoc pedido :id (Integer/parseInt id)))
            (ok @db))))

      (context "/produtos" []
        :tags ["produtos"]

        ;(GET "/")
        ;(GET "/:id")

        (POST "/" []
          :return Produto
          :body [produto Produto]
          :summary "Criar um produto"
          (created "" (salvar-produto produto)))

        ;(DELETE "/:id")
        ;(PUT "/:id")

        )

      )))
