param(
  [string]$TokenUri = "http://localhost:9099/oauth2/jwks",
  [string]$ClientId = "svc_product_visor",
  [string]$ClientSecret = "secret",
  [string]$Scope = "internal"
)

Write-Output "Client ID: $ClientId"
Write-Output "Client Secret: $ClientSecret"
Write-Output "Scope: $Scope"
Write-Output "Configure your services with:"
Write-Output "oauth2.client.token-uri=http://localhost:9099/oauth2/token"
Write-Output "oauth2.client.client-id=$ClientId"
Write-Output "oauth2.client.client-secret=$ClientSecret"
Write-Output "oauth2.client.scope=$Scope"



