(ns magalu.amnesia)

(defn cria-db
  ([] (atom {}))
  ([estado-inicial] (atom estado-inicial)))

(defn adiciona-colecao [db nome]
  (swap! db assoc nome []))

(defn remove-colecao [db nome]
  (swap! db dissoc nome))

(defn adiciona-item [db colecao item]
  (swap! db update colecao conj item))

(defn remove-item [db colecao predicado]
  (swap! db update colecao (fn [valor-velho] (vec (remove predicado valor-velho)))))

(defn remove-todos-itens [db colecao]
  (swap! db update colecao #(replace % [])))

(defn pesquisa-item [db colecao predicado]
  (filter predicado (colecao @db)))

(defn atualiza-item [db colecao predicado novo-item]
  (swap! db update colecao (fn [valor-velho]
                             (->> valor-velho
                                  (remove predicado)
                                  (cons novo-item)
                                  (vec)))))

