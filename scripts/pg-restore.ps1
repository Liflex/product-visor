param(
  [string]$Host, [int]$Port=5432, [string]$User, [string]$Db, [string]$Dump, [string]$Password
)
$env:PGPASSWORD=$Password
createdb -h $Host -p $Port -U $User $Db 2>$null
pg_restore -h $Host -p $Port -U $User -d $Db -c -j 4 $Dump





