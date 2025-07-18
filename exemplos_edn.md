# Exemplos de Formato EDN para Treinos

## O que é EDN?

EDN (Extensible Data Notation) é o formato de dados nativo do Clojure, similar ao JSON mas com algumas diferenças:

- Usa `:chaves` em vez de `"chaves"`
- Usa `[]` para vetores e `{}` para mapas
- Não precisa de aspas em strings simples
- Suporta símbolos e keywords

## Exemplos de Treinos em EDN

### 1. Treino Básico
```edn
{
  :exercicio "Supino Reto"
  :data "2024-01-15"
  :series [
    {:serie 1 :repeticoes 12 :peso 60}
    {:serie 2 :repeticoes 10 :peso 65}
    {:serie 3 :repeticoes 8 :peso 70}
  ]
}
```

### 2. Treino com Peso Constante
```edn
{
  :exercicio "Rosca Direta"
  :data "2024-01-15"
  :series [
    {:serie 1 :repeticoes 15 :peso 20}
    {:serie 2 :repeticoes 15 :peso 20}
    {:serie 3 :repeticoes 15 :peso 20}
  ]
}
```

### 3. Treino com Muitas Séries
```edn
{
  :exercicio "Agachamento"
  :data "2024-01-15"
  :series [
    {:serie 1 :repeticoes 15 :peso 80}
    {:serie 2 :repeticoes 12 :peso 85}
    {:serie 3 :repeticoes 10 :peso 90}
    {:serie 4 :repeticoes 8 :peso 95}
    {:serie 5 :repeticoes 6 :peso 100}
  ]
}
```

## Como Usar com curl

### Criar treino com EDN:
```bash
curl -X POST http://localhost:8080/create-treino \
  -H "Content-Type: application/edn" \
  -d '{
    :exercicio "Supino Reto"
    :data "2024-01-15"
    :series [
      {:serie 1 :repeticoes 12 :peso 60}
      {:serie 2 :repeticoes 10 :peso 65}
      {:serie 3 :repeticoes 8 :peso 70}
    ]
  }'
```

## Diferenças entre JSON e EDN

| JSON | EDN |
|------|-----|
| `"chave"` | `:chave` |
| `"valor"` | `"valor"` |
| `123` | `123` |
| `true/false` | `true/false` |
| `null` | `nil` |

## Vantagens do EDN

1. **Nativo do Clojure**: Não precisa de parsing adicional
2. **Keywords**: Permite usar `:chaves` diretamente
3. **Mais legível**: Sintaxe mais limpa
4. **Extensível**: Suporta tipos de dados customizados

## Exemplo Completo de Arquivo EDN

```edn
;; Treinos da semana
[
  {
    :exercicio "Supino Reto"
    :data "2024-01-15"
    :series [
      {:serie 1 :repeticoes 12 :peso 60}
      {:serie 2 :repeticoes 10 :peso 65}
      {:serie 3 :repeticoes 8 :peso 70}
    ]
  }
  {
    :exercicio "Agachamento"
    :data "2024-01-15"
    :series [
      {:serie 1 :repeticoes 15 :peso 80}
      {:serie 2 :repeticoes 12 :peso 85}
      {:serie 3 :repeticoes 10 :peso 90}
    ]
  }
]
``` 