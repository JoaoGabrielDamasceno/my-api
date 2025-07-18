# Script para testar as funcionalidades dos treinos da academia

Write-Host "=== Testando API de Treinos da Academia ===" -ForegroundColor Green

# 1. Criar um treino
Write-Host "`n1. Criando um treino..." -ForegroundColor Yellow
$treinoData = @{
    exercicio = "Supino Reto"
    data = "2024-01-15"
    series = @(
        @{serie = 1; repeticoes = 12; peso = 60},
        @{serie = 2; repeticoes = 10; peso = 65},
        @{serie = 3; repeticoes = 8; peso = 70}
    )
} | ConvertTo-Json -Depth 3

$response = Invoke-RestMethod -Uri "http://localhost:8080/create-treino" -Method POST -Body $treinoData -ContentType "application/edn"
Write-Host "Resposta: $response" -ForegroundColor Cyan

# 2. Criar outro treino
Write-Host "`n2. Criando outro treino..." -ForegroundColor Yellow
$treinoData2 = @{
    exercicio = "Agachamento"
    data = "2024-01-15"
    series = @(
        @{serie = 1; repeticoes = 15; peso = 80},
        @{serie = 2; repeticoes = 12; peso = 85},
        @{serie = 3; repeticoes = 10; peso = 90},
        @{serie = 4; repeticoes = 8; peso = 95}
    )
} | ConvertTo-Json -Depth 3

$response2 = Invoke-RestMethod -Uri "http://localhost:8080/create-treino" -Method POST -Body $treinoData2 -ContentType "application/edn"
Write-Host "Resposta: $response2" -ForegroundColor Cyan

# 3. Criar um terceiro treino com peso constante
Write-Host "`n3. Criando um terceiro treino..." -ForegroundColor Yellow
$treinoData3 = @{
    exercicio = "Rosca Direta"
    data = "2024-01-15"
    series = @(
        @{serie = 1; repeticoes = 15; peso = 20},
        @{serie = 2; repeticoes = 15; peso = 20},
        @{serie = 3; repeticoes = 15; peso = 20}
    )
} | ConvertTo-Json -Depth 3

$response3 = Invoke-RestMethod -Uri "http://localhost:8080/create-treino" -Method POST -Body $treinoData3 -ContentType "application/edn"
Write-Host "Resposta: $response3" -ForegroundColor Cyan

# 4. Listar todos os treinos
Write-Host "`n4. Listando todos os treinos..." -ForegroundColor Yellow
$treinos = Invoke-RestMethod -Uri "http://localhost:8080/treinos" -Method GET
Write-Host "Treinos encontrados:" -ForegroundColor Cyan
$treinos | ConvertTo-Json -Depth 3

# 5. Buscar treinos por data (se houver treinos)
if ($treinos -and $treinos.Count -gt 0) {
    Write-Host "`n5. Buscando treinos por data..." -ForegroundColor Yellow
    $data = "2024-01-15"
    $treinosPorData = Invoke-RestMethod -Uri "http://localhost:8080/treinos/data/$data" -Method GET
    Write-Host "Treinos da data $data:" -ForegroundColor Cyan
    $treinosPorData | ConvertTo-Json -Depth 3
}

Write-Host "`n=== Teste concluído ===" -ForegroundColor Green 