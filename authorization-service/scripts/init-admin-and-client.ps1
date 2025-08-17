param(
  [string]$ApiBase = "http://localhost:9099",
  [string]$AdminUser = "admin",
  [string]$AdminPass = "admin",
  [string]$Roles = "ADMIN,USER",
  [string]$ClientId = "svc_product_visor",
  [string]$ClientSecret = "secret",
  [string]$Scope = "internal"
)

Write-Output "== Registered Client (in-memory) =="
Write-Output "client_id=$ClientId"
Write-Output "client_secret=$ClientSecret"
Write-Output "scope=$Scope"

Write-Output "== Create admin user via REST =="
$body = @{ username=$AdminUser; password=$AdminPass; roles=$Roles } | ConvertTo-Json
try {
  Invoke-RestMethod -Method Post -Uri "$ApiBase/api/users" -ContentType 'application/json' -Body $body | Out-Null
  Write-Output "Admin user ensured: $AdminUser"
} catch {
  Write-Output "Create admin request failed (maybe exists): $_"
}

Write-Output "== OAuth2 client settings for services =="
Write-Output "oauth2.client.token-uri=$ApiBase/oauth2/token"
Write-Output "oauth2.client.client-id=$ClientId"
Write-Output "oauth2.client.client-secret=$ClientSecret"
Write-Output "oauth2.client.scope=$Scope"



