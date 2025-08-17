param(
  [string]$DbUrl = "Host=localhost;Port=5433;Database=product_visor;Username=postgres;Password=postgres",
  [string]$ClientId = "svc_product_visor",
  [string]$ClientSecret = "secret",
  [string]$ClientName = "Product Visor Service",
  [string]$Scopes = "internal",
  [string]$AuthMethods = "client_secret_basic",
  [string]$Grants = "client_credentials,refresh_token"
)

# Note: для простоты сообщаем настройки; фактическая запись выполняется приложением через API/код и JDBCRepo
Write-Output "Insert a registered client via application startup or SQL. Example values:"
Write-Output "client_id=$ClientId"
Write-Output "client_secret=$ClientSecret"
Write-Output "scopes=$Scopes"
Write-Output "auth_methods=$AuthMethods"
Write-Output "grants=$Grants"



