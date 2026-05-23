# HomeTree Frontend

Use the local Node 22 runtime for this frontend. Node 24 on Windows has been verified to hang during `next build` before the compile phase.

## Local Environment

Copy `.env.example` to `.env.local` and keep secrets out of source control.

```powershell
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=http://localhost:8080/ws
NEXT_PUBLIC_GOOGLE_CLIENT_ID=645146644572-a5l4dmpa6lrvat5l1mfsue5k03g29f6o.apps.googleusercontent.com
```

## Safe Commands

```powershell
.\node_modules\.bin\node.cmd node_modules\eslint\bin\eslint.js . --max-warnings=0
.\node_modules\.bin\node.cmd node_modules\typescript\bin\tsc --noEmit
$env:NEXT_TELEMETRY_DISABLED="1"
.\node_modules\.bin\node.cmd node_modules\next\dist\bin\next build
.\node_modules\.bin\node.cmd node_modules\next\dist\bin\next dev
```

Do not use global Node 24 for production builds.
