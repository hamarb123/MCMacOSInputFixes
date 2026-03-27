#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/.."
/usr/bin/env dotnet run -c Release --file ./util/Packager.cs
