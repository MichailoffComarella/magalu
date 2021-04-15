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
                                     :itens  []}
                          "produtos" {:indice (atom 0)
                                     :itens  []}}))

(defn id-igual? [id item]
  (= (Integer/parseInt id) (:id item)))

(defn busca-todos [colecao db]
  (ok (get-in @db [colecao :itens])))

(defn busca-por-id [colecao id db]
  (if-let [item (first (amnesia/pesquisa-item colecao (partial id-igual? id) db))]
    (ok item)
    (not-found)))

(defn cria-item [colecao item db]
  (amnesia/adiciona-item colecao item db)
  (created "" item))

(defn remove-item [colecao id db]
  (if (empty? (amnesia/pesquisa-item colecao (partial id-igual? id) db))
    (not-found)
    (do (amnesia/remove-item colecao (partial id-igual? id) db)
        (ok))))

(defn altera-item [colecao id alteracao db]
  (if (empty? (amnesia/pesquisa-item colecao (partial id-igual? id) db))
    (not-found)
    (let [db-atualizado (amnesia/atualiza-item colecao (partial id-igual? id) alteracao db)]
      (->> (get-in db-atualizado [colecao :itens])
           (filter (partial id-igual? id))
           (first)
           (ok)))))

(defn cria-contexto [^String nome esquema db]
  (context (str "/" nome) []
    :tags [nome]

    (GET "/" []
      :return [esquema]
      :summary (str "Listar todos os " nome)
      (busca-todos nome db))

    (GET "/:id" [id]
      :return esquema
      :summary "Retornar o item por id."
      (busca-por-id nome id db))

    (POST "/" []
      :return esquema
      :body [item esquema]
      :summary "Criar um item"
      (cria-item nome item db))

    (DELETE "/:id" [id]
      :summary "Excluir um item"
      (remove-item nome id db))

    (PUT "/:id" [id]
      :body [item esquema]
      :summary "Alterar um item"
      (altera-item nome id item db))))

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