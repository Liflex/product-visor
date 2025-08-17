param(
  [string]$Host, [int]$Port=5432, [string]$User, [string]$Db, [string]$Out, [string]$Password
)
$env:PGPASSWORD=$Password
pg_dump -h $Host -p $Port -U $User -d $Db -Fc -f $Out






