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

(def db (amnesia/cria-db {"pedidos"  {:indice (atom 0)
                                      :modelo Pedido
                                      :itens  []}
                          "produtos" {:indice (atom 0)
                                      :modelo Produto
                                      :itens  []}}))

(defn id-igual? [id item]
  (= (Integer/parseInt id) (:id item)))

(defn cria-contexto [^String nome esquema db]
  (context (str "/" nome) []
    :tags [nome]

    (GET "/" []
      :return [esquema]
      :summary (str "Listar todos os " nome)
      (ok (get-in @db [nome :itens])))

    (GET "/:id" [id]
      :return [esquema]
      :summary "Retornar o item por id."
      (let [resultado (amnesia/pesquisa-item nome (partial id-igual? id) db)]
        (if (empty? (:itens resultado))
          (not-found)
          (ok (:itens resultado)))))

    (POST "/" []
      :return [esquema]
      :body [item s/Any]
      :summary "Criar um item"
      (let [item-indexado (first (:itens (amnesia/adiciona-item nome item db)))]
        (created "" item-indexado)))

    (DELETE "/:id" [id]
      :summary "Excluir um item"
      (if (empty? (:itens (amnesia/pesquisa-item nome (partial id-igual? id) db)))
        (not-found)
        (do (amnesia/remove-item nome (partial id-igual? id) db)
            (ok))))

    (PUT "/:id" [id]
      :body [item esquema]
      :summary "Alterar um item"
      (if (empty? (:itens (amnesia/pesquisa-item nome (partial id-igual? id) db)))
        (not-found)
        (let [resultado (amnesia/atualiza-item nome (partial id-igual? id) item db)]
          (ok (first (:itens resultado))))))))

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

      (cria-contexto "pedidos" Pedido db)
      (cria-contexto "produtos" Produto db))))