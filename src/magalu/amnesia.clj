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

(defn remove-item [db colecao item]
  (swap! db update colecao (fn [valor-velho] (vec (remove #(= % item) valor-velho)))))

(defn esta-contido? [valor lista]
  (true? (some #(= % valor) lista)))

(defn remove-itens [db colecao itens]
  (swap! db update colecao (fn [valor-velho] (vec (remove #(esta-contido? % itens) valor-velho)))))

(defn remove-todos-itens [db colecao]
  (swap! db update colecao #(replace % [])))

(defn pesquisar-nome [db colecao nome]
  (filter #(= % nome) (colecao @db)))

(defn pesquisar-posicao [db colecao posicao]
  (get (colecao @db) posicao))

(defn atualiza-item-nome [db colecao item novo-item]
  (swap! db update colecao (fn [valor-velho] (conj (vec (remove #(= % item) valor-velho)) novo-item))))

