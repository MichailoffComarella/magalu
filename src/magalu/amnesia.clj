(ns magalu.amnesia)

(defn cria-db
  ([] (atom {}))
  ([estado-inicial] (atom estado-inicial)))

(defn adiciona-colecao [db nome]
  (swap! db assoc nome []))

(defn remove-colecao [db nome]
  (swap! db dissoc nome))
