# 🏄‍♂️ KiteMe - Kitesurf Social Network

![Version](https://img.shields.io/badge/version-3.0-blue)
![Android](https://img.shields.io/badge/Android-7.0+-green)
![Languages](https://img.shields.io/badge/languages-PT--BR%20%7C%20EN-orange)

## About

KiteMe is a social network app for kitesurfing enthusiasts. Discover spots, buy/sell equipment, chat with our AI assistant KiteBot, and connect with the community!

## Features

- 🌍 **Bilingual** - Portuguese (BR) and English
- 📍 **Spots** - Find the best kitesurfing beaches in Brazil
- 🛒 **Marketplace** - Buy and sell used equipment
- 🏨 **Accommodations** - Find lodging near spots
- 💬 **Forum** - Discuss techniques and share experiences
- 🤖 **KiteBot** - AI assistant specialized in kitesurfing
- 🎤 **Voice** - Speech recognition and text-to-speech

## Screenshots

Coming soon...

## Download

- **APK**: `kiteme-v3.0.apk` (direct install)
- **AAB**: `kiteme-v3.0.aab` (Play Store upload)

## Tech Stack

- **Android** - Native (Kotlin)
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Backend**: Node.js + Express + SQLite
- **AI**: OpenRouter API

## Backend

The backend is deployed on Railway. Local development:

```bash
cd backend
npm install
npm run dev
```

Production URL: `https://kitesurf-brasil-api-production.up.railway.app`

## Building

```bash
# Set Java Home
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Build
cd android
./gradlew assembleRelease  # APK
./gradlew bundleRelease    # AAB
```

## Signing

The app uses `kiteme-release.keystore` for signing. Keep this file safe - you cannot update the app on Play Store without it!

## Privacy Policy

See [PRIVACY_POLICY.md](PRIVACY_POLICY.md)

## Play Store

See [PLAY_STORE_LISTING.md](PLAY_STORE_LISTING.md) for all listing information.

## License

© 2026 Titanio Films. All rights reserved.

## Contact

- Email: contact@titaniofilms.com
- Developer: Titanio Films
