(ns magalu.amnesia)

(defn cria-db []
  (atom {}))

(defn cria-colecao [db nome]
  (swap! db conj ((keyword nome) [])))

(defn adiciona-colecao [db colecao]
  (swap! db conj colecao))

(defn remove-colecao [db colecao]
  ())