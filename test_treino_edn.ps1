# Script para testar as funcionalidades dos treinos usando formato EDN

Write-Host "=== Testando API de Treinos com EDN ===" -ForegroundColor Green

# 1. Criar um treino em EDN
Write-Host "`n1. Criando um treino com EDN..." -ForegroundColor Yellow
$treinoEdn = @'
{
  :exercicio "Supino Reto"
  :data "2024-01-15"
  :series [
    {:serie 1 :repeticoes 12 :peso 60}
    {:serie 2 :repeticoes 10 :peso 65}
    {:serie 3 :repeticoes 8 :peso 70}
  ]
}
'@

$response = Invoke-RestMethod -Uri "http://localhost:8080/create-treino" -Method POST -Body $treinoEdn -ContentType "application/edn"
Write-Host "Resposta: $response" -ForegroundColor Cyan

# 2. Criar outro treino em EDN
Write-Host "`n2. Criando outro treino com EDN..." -ForegroundColor Yellow
$treinoEdn2 = @'
{
  :exercicio "Agachamento"
  :data "2024-01-15"
  :series [
    {:serie 1 :repeticoes 15 :peso 80}
    {:serie 2 :repeticoes 12 :peso 85}
    {:serie 3 :repeticoes 10 :peso 90}
    {:serie 4 :repeticoes 8 :peso 95}
  ]
}
'@

$response2 = Invoke-RestMethod -Uri "http://localhost:8080/create-treino" -Method POST -Body $treinoEdn2 -ContentType "application/edn"
Write-Host "Resposta: $response2" -ForegroundColor Cyan

# 3. Criar treino com peso constante em EDN
Write-Host "`n3. Criando treino com peso constante..." -ForegroundColor Yellow
$treinoEdn3 = @'
{
  :exercicio "Rosca Direta"
  :data "2024-01-15"
  :series [
    {:serie 1 :repeticoes 15 :peso 20}
    {:serie 2 :repeticoes 15 :peso 20}
    {:serie 3 :repeticoes 15 :peso 20}
  ]
}
'@

$response3 = Invoke-RestMethod -Uri "http://localhost:8080/create-treino" -Method POST -Body $treinoEdn3 -ContentType "application/edn"
Write-Host "Resposta: $response3" -ForegroundColor Cyan

# 4. Listar todos os treinos
Write-Host "`n4. Listando todos os treinos..." -ForegroundColor Yellow
$treinos = Invoke-RestMethod -Uri "http://localhost:8080/treinos" -Method GET
Write-Host "Treinos encontrados:" -ForegroundColor Cyan
$treinos | ConvertTo-Json -Depth 5

# 5. Buscar treinos por data
if ($treinos -and $treinos.Count -gt 0) {
    Write-Host "`n5. Buscando treinos por data..." -ForegroundColor Yellow
    $data = "2024-01-15"
    $treinosPorData = Invoke-RestMethod -Uri "http://localhost:8080/treinos/data/$data" -Method GET
    Write-Host "Treinos da data $data:" -ForegroundColor Cyan
    $treinosPorData | ConvertTo-Json -Depth 5
}

Write-Host "`n=== Teste EDN concluído ===" -ForegroundColor Green 