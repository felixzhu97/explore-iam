# IAM iOS (SwiftUI)

Native hello-world shell for Explore IAM. Authorization Code + login for
relying parties still run on the Authorization Server (`:9100`).

## Requirements

- Xcode 16+
- iOS 17+ simulator or device
- Local IAM API: `./gradlew bootRun` (issuer `http://localhost:9100`)

## Open

```bash
cd src/main/ios
open IAM.xcodeproj
```

## Demo login

- Username: `demo`
- Password: `demo-password`

Seeded public clients (PKCE required):

| client_id | redirect |
| --- | --- |
| `explore-ai-ios` | `com.explore.ai://oauth/callback` |
| `explore-chat-ios` | `com.explore.chat://oauth/callback` |

## Test

```bash
cd src/main/ios
xcodebuild test -scheme IAM \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  CODE_SIGNING_ALLOWED=NO
```

Bundle ID: `com.explore.iam`
