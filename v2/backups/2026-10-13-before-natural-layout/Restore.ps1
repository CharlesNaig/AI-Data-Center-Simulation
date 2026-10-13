param(
    [string]$TargetV2 = (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent)
)

$ErrorActionPreference = 'Stop'
$targetRoot = [IO.Path]::GetFullPath($TargetV2)
$snapshotRoot = Join-Path $PSScriptRoot 'snapshot'
if (-not (Test-Path -LiteralPath (Join-Path $targetRoot 'models/ai-data-center-community-v2.nlogox'))) {
    throw 'Target must be an existing V2 folder containing the community model.'
}
$entries = Get-Content -Raw -LiteralPath (Join-Path $PSScriptRoot 'manifest.json') | ConvertFrom-Json

function Get-ContentHash([string]$Path) {
    $sha = [Security.Cryptography.SHA256]::Create()
    try { return ([BitConverter]::ToString($sha.ComputeHash([IO.File]::ReadAllBytes($Path)))).Replace('-', '').ToLowerInvariant() }
    finally { $sha.Dispose() }
}

function Resolve-Child([string]$Root, [string]$Relative) {
    if ([IO.Path]::IsPathRooted($Relative) -or ($Relative -split '[/\\]') -contains '..') { throw 'Invalid backup path.' }
    $resolved = [IO.Path]::GetFullPath((Join-Path $Root $Relative))
    $boundary = $Root.TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    if (-not $resolved.StartsWith($boundary, [StringComparison]::OrdinalIgnoreCase)) { throw 'Backup path escaped its folder.' }
    return $resolved
}

# Verify the complete snapshot before changing any current files.
foreach ($entry in $entries) {
    $saved = Resolve-Child $snapshotRoot $entry.path
    if ((Get-ContentHash $saved) -ne $entry.sha256) { throw "Backup hash mismatch: $($entry.path)" }
    $null = Resolve-Child $targetRoot $entry.path
}

$saveCurrent = Join-Path $targetRoot ('backups/before-restore-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [Guid]::NewGuid().ToString('N').Substring(0, 6))
foreach ($entry in $entries) {
    $current = Resolve-Child $targetRoot $entry.path
    if (Test-Path -LiteralPath $current) {
        $preserved = Resolve-Child $saveCurrent $entry.path
        New-Item -ItemType Directory -Force -Path (Split-Path $preserved -Parent) | Out-Null
        Copy-Item -LiteralPath $current -Destination $preserved
    }
}
foreach ($entry in $entries) {
    $saved = Resolve-Child $snapshotRoot $entry.path
    $current = Resolve-Child $targetRoot $entry.path
    New-Item -ItemType Directory -Force -Path (Split-Path $current -Parent) | Out-Null
    Copy-Item -LiteralPath $saved -Destination $current -Force
    if ((Get-ContentHash $current) -ne $entry.sha256) { throw "Restored hash mismatch: $($entry.path)" }
}
Write-Output "Restored the original V2 layout ($($entries.Count) files, SHA-256 verified)."
Write-Output "Saved the replaced version first: $saveCurrent"
Write-Output 'Close and reopen the model in NetLogo to see the restored design. Other files and Obsidian notes were not changed.'
