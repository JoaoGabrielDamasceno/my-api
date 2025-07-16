# Teste simples da API
Write-Host "Testando rota GET /"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/" -Method GET
    Write-Host "Status: $($response.StatusCode)"
    Write-Host "Content: $($response.Content)"
} catch {
    Write-Host "Erro: $($_.Exception.Message)"
}

Write-Host "`nTestando rota POST /create-user"
try {
    $body = "{:id `"joao`" :password `"123456`" :name `"Joao`" :age 27}"
    $headers = @{"Content-Type"="application/edn"}
    $response = Invoke-WebRequest -Uri "http://localhost:8080/create-user" -Method POST -Headers $headers -Body $body
    Write-Host "Status: $($response.StatusCode)"
    Write-Host "Content: $($response.Content)"
} catch {
    Write-Host "Erro: $($_.Exception.Message)"
    Write-Host "Status Code: $($_.Exception.Response.StatusCode)"
} 