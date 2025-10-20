(ns my-api.migrations
  "Namespace para migrações e populamento de dados.
  
  Para executar manualmente via REPL:
  
  (require '[my-api.migrations :as mig])
  (mig/migrar-tudo!)
  
  Ou migrações específicas:
  (mig/popular-exercicios!)
  "
  (:require [my-api.bd-exercicio :as exercicio-db]
            [clojure.string :as str]))

;; ============================================
;; DADOS INICIAIS - EXERCÍCIOS PADRÃO
;; ============================================

(def exercicios-padrao
  "Lista de exercícios comuns de academia para popular o banco inicial."
  [;; PEITO
   {:nome "Supino Reto" :categoria "peito"}
   {:nome "Supino Inclinado" :categoria "peito"}
   {:nome "Supino Declinado" :categoria "peito"}
   {:nome "Crucifixo" :categoria "peito"}
   {:nome "Flexão de Braço" :categoria "peito"}
   {:nome "Crossover" :categoria "peito"}
   
   ;; COSTAS
   {:nome "Barra Fixa" :categoria "costas"}
   {:nome "Puxada Frontal" :categoria "costas"}
   {:nome "Remada Curvada" :categoria "costas"}
   {:nome "Remada Baixa" :categoria "costas"}
   {:nome "Remada Alta" :categoria "costas"}
   {:nome "Pulldown" :categoria "costas"}
   {:nome "Levantamento Terra" :categoria "costas"}
   
   ;; PERNAS
   {:nome "Agachamento Livre" :categoria "pernas"}
   {:nome "Agachamento Hack" :categoria "pernas"}
   {:nome "Leg Press" :categoria "pernas"}
   {:nome "Extensora" :categoria "pernas"}
   {:nome "Flexora" :categoria "pernas"}
   {:nome "Cadeira Adutora" :categoria "pernas"}
   {:nome "Cadeira Abdutora" :categoria "pernas"}
   {:nome "Panturrilha em Pé" :categoria "pernas"}
   {:nome "Panturrilha Sentado" :categoria "pernas"}
   {:nome "Afundo" :categoria "pernas"}
   {:nome "Stiff" :categoria "pernas"}
   
   ;; OMBROS
   {:nome "Desenvolvimento com Barra" :categoria "ombros"}
   {:nome "Desenvolvimento com Halteres" :categoria "ombros"}
   {:nome "Elevação Lateral" :categoria "ombros"}
   {:nome "Elevação Frontal" :categoria "ombros"}
   {:nome "Remada Alta (Ombros)" :categoria "ombros"}
   {:nome "Crucifixo Invertido" :categoria "ombros"}
   {:nome "Encolhimento" :categoria "ombros"}
   
   ;; BRAÇOS
   {:nome "Rosca Direta" :categoria "bracos"}
   {:nome "Rosca Alternada" :categoria "bracos"}
   {:nome "Rosca Martelo" :categoria "bracos"}
   {:nome "Rosca Concentrada" :categoria "bracos"}
   {:nome "Rosca Scott" :categoria "bracos"}
   {:nome "Tríceps Pulley" :categoria "bracos"}
   {:nome "Tríceps Testa" :categoria "bracos"}
   {:nome "Tríceps Francês" :categoria "bracos"}
   {:nome "Mergulho em Paralelas" :categoria "bracos"}
   
   ;; ABDÔMEN
   {:nome "Abdominal Supra" :categoria "abdomen"}
   {:nome "Abdominal Infra" :categoria "abdomen"}
   {:nome "Abdominal Oblíquo" :categoria "abdomen"}
   {:nome "Prancha" :categoria "abdomen"}
   {:nome "Prancha Lateral" :categoria "abdomen"}
   {:nome "Elevação de Pernas" :categoria "abdomen"}
   
   ;; CARDIO
   {:nome "Esteira" :categoria "cardio"}
   {:nome "Bicicleta Ergométrica" :categoria "cardio"}
   {:nome "Elíptico" :categoria "cardio"}
   {:nome "Transport" :categoria "cardio"}
   {:nome "Pular Corda" :categoria "cardio"}])

;; ============================================
;; FUNÇÕES DE MIGRAÇÃO
;; ============================================

(defn popular-exercicios!
  "Popula o banco com exercícios padrão de academia.
  Pode ser executado múltiplas vezes sem duplicar dados."
  []
  (println "\n>>> Iniciando população de exercícios...")
  (let [exercicios-existentes (set (map :nome-interno (exercicio-db/listar-exercicios)))]
    (doseq [exercicio exercicios-padrao]
      (let [nome-interno-str (-> (:nome exercicio)
                                  str/trim
                                  str/lower-case
                                  (str/replace #"\s+" "-"))]
        (when-not (contains? exercicios-existentes nome-interno-str)
          (try
            (exercicio-db/inserir-exercicio! exercicio)
            (println "✓ Exercício cadastrado:" (:nome exercicio))
            (catch Exception e
              (println "⚠ Erro ao cadastrar" (:nome exercicio) ":" (.getMessage e)))))))
    (println "✓ Populamento concluído!\n")))

(defn migrar-tudo!
  "Executa todas as migrações disponíveis."
  []
  (println "\n╔════════════════════════════════════╗")
  (println "║   EXECUTANDO TODAS AS MIGRAÇÕES   ║")
  (println "╚════════════════════════════════════╝\n")
  
  (popular-exercicios!)
  
  (println "╔════════════════════════════════════╗")
  (println "║   MIGRAÇÕES CONCLUÍDAS COM SUCESSO ║")
  (println "╚════════════════════════════════════╝\n"))

(comment
  ;; Exemplos de uso no REPL:
  
  ;; Popular apenas os exercícios
  (popular-exercicios!)
  
  ;; Executar todas as migrações
  (migrar-tudo!)
  )

