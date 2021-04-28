(ns magalu.amnesia
  (:require [schema.core :as s
             :include-macros true]))

(s/defschema Produto
  {:nome       s/Str
   :valor      s/Num
   :quantidade s/Num})

(s/defschema Pedido
  {:itens [Produto]})

(defrecord Resultado [itens-afetados itens erros])

(defn- recupera-indice-colecao [db colecao]
  (get-in @db [colecao :indice]))

(defn- recupera-modelo-colecao [db colecao]
  (get-in @db [colecao :modelo]))

(defn- proximo-indice [db colecao]
  (swap! (recupera-indice-colecao db colecao) inc))

(defn- recupera-itens-colecao [db colecao]
  (get-in @db [colecao :itens]))

(defn- remove-itens-colecao [db colecao predicado]
  (swap! db update-in [colecao :itens] (partial remove predicado)))

(defn cria-db
  ([] (atom {}))
  ([estado-inicial] (atom estado-inicial)))

(defn adiciona-colecao [nome esquema db]
  (swap! db assoc nome {:indice (atom 0)
                        :modelo esquema
                        :itens  []}))

(defn remove-colecao [nome db]
  (swap! db dissoc nome))

(defn adiciona-item [colecao item db]
  (try
    (s/validate (recupera-modelo-colecao db colecao) item)
    (let [indice (proximo-indice db colecao)
          item-indexado (assoc item :id indice)]
      (swap! db update-in [colecao :itens] conj item-indexado)
      (->Resultado 1 [item-indexado] {}))
    (catch Exception e
      (->Resultado 0 [] (-> e ex-data :error)))))

(defn remove-item [colecao predicado db]
  (let [itens (recupera-itens-colecao db colecao)
        itens-removidos (filterv predicado itens)]
    (remove-itens-colecao db colecao predicado)
    (->Resultado (count itens-removidos) itens-removidos {})))

(defn remove-todos-itens [colecao db]
  (let [itens (recupera-itens-colecao db colecao)]
    (swap! db update-in [colecao :itens] #(replace % []))
    (->Resultado (count itens) itens {})))

(defn pesquisa-item [colecao predicado db]
  (let [itens (filterv predicado (recupera-itens-colecao db colecao))]
    (->Resultado (count itens) itens {})))

(defn atualiza-colecao [predicado novo-item colecao]
  (mapv #(if (predicado %)
           (merge % novo-item)
           %)
        colecao))

(defn atualiza-item [colecao predicado novo-item db]
  (swap! db update-in [colecao :itens] (partial atualiza-colecao predicado novo-item))
  (let [itens (filterv predicado (recupera-itens-colecao db colecao))]
    (->Resultado (count itens) itens {})))
