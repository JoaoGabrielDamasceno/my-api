# 🔄 Migrações e População de Dados

Este documento explica como executar migrações no banco de dados do GymStatus.

## 📋 Comportamento Automático

Quando o servidor é iniciado, os exercícios padrão são **automaticamente populados** no banco de dados. Esta operação é **idempotente** (pode ser executada múltiplas vezes sem criar duplicatas).

Você verá no console:

```
=== Inicializando banco de dados ===
✓ Exercício cadastrado: Supino Reto
✓ Exercício cadastrado: Agachamento Livre
...
✓ Populamento concluído!
=== Inicialização concluída ===
```

## 🔧 Execução Manual via REPL

Se você quiser executar as migrações manualmente:

### 1. Iniciar o REPL

```bash
cd my-api
lein repl
```

### 2. Carregar o namespace de migrações

```clojure
(require '[my-api.migrations :as mig])
```

### 3. Executar migrações

```clojure
;; Popular apenas os exercícios
(mig/popular-exercicios!)

;; Ou executar todas as migrações de uma vez
(mig/migrar-tudo!)
```

## 📊 Exercícios Incluídos

A migração popula o banco com 60+ exercícios organizados por grupo muscular:

- **🫁 Peito** (6 exercícios): Supino Reto, Supino Inclinado, Crucifixo, etc.
- **🏋️ Costas** (7 exercícios): Barra Fixa, Remada Curvada, Levantamento Terra, etc.
- **🦵 Pernas** (11 exercícios): Agachamento, Leg Press, Extensora, etc.
- **💪 Ombros** (7 exercícios): Desenvolvimento, Elevação Lateral, etc.
- **💪 Braços** (9 exercícios): Rosca Direta, Tríceps Pulley, etc.
- **🧘 Abdômen** (6 exercícios): Abdominal Supra, Prancha, etc.
- **🏃 Cardio** (5 exercícios): Esteira, Bicicleta, Elíptico, etc.

## ⚠️ Importante

- **Sem Duplicatas**: Os exercícios que já existem no banco não serão cadastrados novamente
- **Seguro**: Pode executar múltiplas vezes sem problemas
- **Automático**: Executa automaticamente ao iniciar o servidor

## 🔍 Verificar Exercícios Cadastrados

Via REPL:

```clojure
(require '[my-api.bd-exercicio :as ex])

;; Listar todos os exercícios
(ex/listar-exercicios)

;; Listar todas as categorias
(ex/listar-categorias)
```

Via API:

```bash
# Listar exercícios
curl http://localhost:8080/exercicios

# Listar categorias
curl http://localhost:8080/exercicios/categorias
```

## 📝 Adicionar Novas Migrações

Para adicionar novas migrações no futuro:

1. Edite o arquivo `src/my_api/migrations.clj`
2. Adicione uma nova função para sua migração
3. Adicione-a à função `migrar-tudo!`

Exemplo:

```clojure
(defn minha-nova-migracao!
  "Descrição da migração"
  []
  (println ">>> Executando nova migração...")
  ;; seu código aqui
  (println ">>> Migração concluída!"))

(defn migrar-tudo!
  []
  ;; ...
  (popular-exercicios!)
  (minha-nova-migracao!)  ;; adicione aqui
  ;; ...
  )
```

