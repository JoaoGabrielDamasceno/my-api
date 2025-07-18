# API de Treinos da Academia

Esta funcionalidade permite armazenar e gerenciar seus treinos da academia com informações detalhadas sobre exercícios, datas e séries.

## Estrutura dos Dados

### Treino
- **exercicio**: Nome do exercício (string)
- **data**: Data do treino no formato YYYY-MM-DD (string)
- **series**: Vetor de mapas com informações de cada série

### Série (mapa)
- **serie**: Número da série
- **repeticoes**: Quantidade de repetições
- **peso**: Peso usado na série (em kg)

**Exemplo:**
```json
[
  {"serie": 1, "repeticoes": 12, "peso": 60},
  {"serie": 2, "repeticoes": 10, "peso": 65},
  {"serie": 3, "repeticoes": 8, "peso": 70}
]
```

## Endpoints Disponíveis

### 1. Criar Treino
**POST** `/create-treino`

Cria um novo registro de treino.

**Exemplo de requisição:**
```json
{
  "exercicio": "Supino Reto",
  "data": "2024-01-15",
  "series": [
    {"serie": 1, "repeticoes": 12, "peso": 60},
    {"serie": 2, "repeticoes": 10, "peso": 65},
    {"serie": 3, "repeticoes": 8, "peso": 70}
  ]
}
```

### 2. Listar Todos os Treinos
**GET** `/treinos`

Retorna todos os treinos cadastrados.

### 3. Buscar Treino por ID
**GET** `/treino/:id`

Retorna um treino específico pelo seu ID.

### 4. Buscar Treinos por Data
**GET** `/treinos/data/:data`

Retorna todos os treinos de uma data específica.

**Exemplo:** `/treinos/data/2024-01-15`

## Como Usar

1. **Iniciar o servidor:**
   ```bash
   lein run
   ```

2. **Testar com o script PowerShell:**
   ```powershell
   .\test_treino.ps1
   ```

3. **Exemplo de uso com curl:**
   ```bash
   # Criar treino
   curl -X POST http://localhost:8080/create-treino \
     -H "Content-Type: application/edn" \
     -d '{
       "exercicio": "Agachamento",
       "data": "2024-01-15",
       "series": [
         {"serie": 1, "repeticoes": 15, "peso": 80},
         {"serie": 2, "repeticoes": 12, "peso": 85},
         {"serie": 3, "repeticoes": 10, "peso": 90}
       ]
     }'

   # Listar treinos
   curl http://localhost:8080/treinos
   ```

## Formato de Data

As datas devem estar no formato: `YYYY-MM-DD`

**Exemplos:**
- `2024-01-15`
- `2024-12-25`

## Banco de Dados

Os dados são armazenados no Datomic com o seguinte schema:

- `:treino/id` - UUID único do treino
- `:treino/exercicio` - Nome do exercício
- `:treino/data` - Data do treino (string)
- `:treino/series` - Vetor de mapas de séries em formato JSON (string) 