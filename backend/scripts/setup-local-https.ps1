[CmdletBinding()]
param(
    [string]$KeystorePath,
    [string]$Alias = "unimarket-local",
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($KeystorePath)) {
    $KeystorePath = Join-Path $projectRoot ".local\https\unimarket-local.p12"
}
$KeystorePath = [System.IO.Path]::GetFullPath($KeystorePath)
$httpsDirectory = Split-Path -Parent $KeystorePath
$certificatePath = Join-Path $httpsDirectory "unimarket-local.crt"
$environmentPath = Join-Path $httpsDirectory "https-env.ps1"

$keytool = (Get-Command keytool -ErrorAction Stop).Source
if (Test-Path $KeystorePath) {
    if (-not $Force) {
        throw "Keystore already exists at '$KeystorePath'. Use -Force to replace it."
    }
    Remove-Item -LiteralPath $KeystorePath -Force
}

New-Item -ItemType Directory -Path $httpsDirectory -Force | Out-Null

$password = $env:UNIMARKET_SSL_KEY_STORE_PASSWORD
if ([string]::IsNullOrWhiteSpace($password)) {
    $bytes = New-Object byte[] 36
    $random = [Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $random.GetBytes($bytes)
    } finally {
        $random.Dispose()
    }
    $password = [Convert]::ToBase64String($bytes)
}

$generateArguments = @(
    "-genkeypair",
    "-alias", $Alias,
    "-keyalg", "RSA",
    "-keysize", "2048",
    "-sigalg", "SHA256withRSA",
    "-validity", "825",
    "-storetype", "PKCS12",
    "-keystore", $KeystorePath,
    "-storepass", $password,
    "-keypass", $password,
    "-dname", "CN=localhost,OU=Local Development,O=UniMarket,L=Cape Town,ST=Western Cape,C=ZA",
    "-ext", "SAN=dns:localhost,ip:127.0.0.1",
    "-ext", "KU=digitalSignature,keyEncipherment",
    "-ext", "EKU=serverAuth",
    "-noprompt"
)
& $keytool @generateArguments
if ($LASTEXITCODE -ne 0) {
    throw "keytool failed to create the local PKCS12 keystore."
}

$exportArguments = @(
    "-exportcert",
    "-rfc",
    "-alias", $Alias,
    "-keystore", $KeystorePath,
    "-storetype", "PKCS12",
    "-storepass", $password,
    "-file", $certificatePath
)
& $keytool @exportArguments
if ($LASTEXITCODE -ne 0) {
    throw "keytool created the keystore but failed to export its public certificate."
}

$projectPrefix = [System.IO.Path]::GetFullPath($projectRoot).TrimEnd("\") + "\"
if (-not $KeystorePath.StartsWith($projectPrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "The local keystore must be created inside the project root."
}
$relativeKeystore = $KeystorePath.Substring($projectPrefix.Length).Replace("\", "/")
$escapedPassword = $password.Replace("'", "''")
$escapedAlias = $Alias.Replace("'", "''")
$environmentLines = @(
    "`$env:SPRING_PROFILES_ACTIVE = 'https'",
    "`$env:UNIMARKET_SSL_KEY_STORE = 'file:./$relativeKeystore'",
    "`$env:UNIMARKET_SSL_KEY_STORE_PASSWORD = '$escapedPassword'",
    "`$env:UNIMARKET_SSL_KEY_ALIAS = '$escapedAlias'",
    "`$env:REFRESH_COOKIE_SECURE = 'true'",
    "`$env:JWT_ISSUER = 'https://localhost:8443'"
)
Set-Content -LiteralPath $environmentPath -Value $environmentLines -Encoding utf8

Write-Host "Created local HTTPS keystore: $KeystorePath"
Write-Host "Exported public certificate: $certificatePath"
Write-Host "Created ignored environment helper: $environmentPath"
Write-Host "From the project root, load it with: . .\.local\https\https-env.ps1"
Write-Warning "The environment helper contains the local keystore password. Keep .local private."
