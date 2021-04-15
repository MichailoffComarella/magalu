(ns magalu.amnesia)

(defn cria-db
  ([] (atom {}))
  ([estado-inicial] (atom estado-inicial)))

(defn adiciona-colecao [nome db]
  (swap! db assoc nome {:indice (atom 0)
                        :itens  []}))

(defn remove-colecao [nome db]
  (swap! db dissoc nome))

(defn adiciona-item [colecao item db]
  (swap! (get-in @db [colecao :indice]) inc)
  (let [item-indexado (assoc item :id @(get-in @db [colecao :indice]))]
    (swap! db update-in [colecao :itens] conj item-indexado)))

(defn remove-item [colecao predicado db]
  (swap! db update-in [colecao :itens] (fn [valor-velho] (vec (remove predicado valor-velho)))))

(defn remove-todos-itens [colecao db]
  (reset! (get-in @db [colecao :indice]) 0)
  (swap! db update-in [colecao :itens] #(replace % [])))

(defn pesquisa-item [colecao predicado db]
  (filter predicado (get-in @db [colecao :itens])))

(defn atualiza-colecao [predicado novo-item colecao]
  (mapv #(if (predicado %)
           (merge % novo-item)
           %)
        colecao))

(defn atualiza-item [colecao predicado novo-item db]
  (swap! db update-in [colecao :itens] (partial atualiza-colecao predicado novo-item)))

