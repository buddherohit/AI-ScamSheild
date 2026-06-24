# ==============================================================================
# AI ScamShield - Developer Environment Setup & Diagnostics Script
# Operating System: Windows (PowerShell)
# ==============================================================================

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "             AI ScamShield - Developer Diagnostic Setup                " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

$errorsCount = 0

# Helper function to print audit status
function Check-Tool {
    param (
        [string]$Name,
        [string]$Command,
        [string]$VerifyArgs,
        [string]$ExpectedPattern,
        [string]$InstallInstructions
    )

    Write-Host "Checking for $Name... " -NoNewline -ForegroundColor White
    $commandExists = Get-Command $Command -ErrorAction SilentlyContinue

    if ($commandExists) {
        $output = & $Command $VerifyArgs 2>&1
        if ($output -match $ExpectedPattern) {
            Write-Host "SUCCESS (Found)" -ForegroundColor Green
            return $true
        } else {
            Write-Host "WARNING (Found, but version mismatch)" -ForegroundColor Yellow
            Write-Host "  -> Version Info: $output" -ForegroundColor DarkGray
            Write-Host "  -> System recommends: $ExpectedPattern" -ForegroundColor DarkGray
            return $true
        }
    } else {
        Write-Host "FAILED" -ForegroundColor Red
        Write-Host "  -> $Name was not found in System PATH variables." -ForegroundColor Red
        Write-Host "  -> Instructions: $InstallInstructions" -ForegroundColor DarkGray
        $script:errorsCount++
        return $false
    }
}

# 1. Check for Java JRE/JDK 21
Check-Tool -Name "Java JDK (v21)" `
           -Command "java" `
           -VerifyArgs "-version" `
           -ExpectedPattern "21\." `
           -InstallInstructions "Download Eclipse Temurin 21 from: https://adoptium.net/temurin/releases/?version=21"

# 2. Check for Apache Maven
Check-Tool -Name "Apache Maven" `
           -Command "mvn" `
           -VerifyArgs "-v" `
           -ExpectedPattern "Apache Maven" `
           -InstallInstructions "Install Maven and add its bin directory to PATH. Instruction details: https://maven.apache.org/install.html"

# 3. Check for Node.js (v20+)
Check-Tool -Name "Node.js (v20+)" `
           -Command "node" `
           -VerifyArgs "-v" `
           -ExpectedPattern "v(20|21|22)\." `
           -InstallInstructions "Download Node.js from: https://nodejs.org/"

# 4. Check for Docker
Check-Tool -Name "Docker Daemon" `
           -Command "docker" `
           -VerifyArgs "-v" `
           -ExpectedPattern "Docker version" `
           -InstallInstructions "Install Docker Desktop: https://www.docker.com/products/docker-desktop/"

Write-Host ""
Write-Host "----------------------------------------------------------------------" -ForegroundColor Cyan
Write-Host "Environment Initializations" -ForegroundColor Cyan
Write-Host "----------------------------------------------------------------------"

# 5. Handle .env configuration files
$rootDir = Resolve-Path ".."
$envExamplePath = Join-Path $rootDir ".env.example"
$envPath = Join-Path $rootDir ".env"

if (Test-Path $envPath) {
    Write-Host "-> Environment file '.env' already exists at root. Skipping initialization." -ForegroundColor Green
} else {
    if (Test-Path $envExamplePath) {
        Copy-Item -Path $envExamplePath -Destination $envPath
        Write-Host "-> Created '.env' file successfully from template." -ForegroundColor Green
    } else {
        Write-Host "-> ERROR: .env.example template could not be located at root!" -ForegroundColor Red
        $errorsCount++
    }
}

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
if ($errorsCount -eq 0) {
    Write-Host " Setup diagnostics completed successfully! Your machine is ready to run. " -BackgroundColor Green -ForegroundColor Black
    Write-Host " Run commands: " -ForegroundColor White
    Write-Host "  1. Start Database:      docker compose up postgres -d" -ForegroundColor Yellow
    Write-Host "  2. Launch Backend:      cd backend; mvn spring-boot:run" -ForegroundColor Yellow
    Write-Host "  3. Launch Frontend:     cd frontend; npm install; npm run dev" -ForegroundColor Yellow
} else {
    Write-Host " Setup diagnostics found $errorsCount issues. Please resolve them and re-run. " -BackgroundColor Red -ForegroundColor White
}
Write-Host "======================================================================" -ForegroundColor Cyan
