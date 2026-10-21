# Changelog

## 0.1.1 — 2026-10-20

Initial public release of `@cnhongwei/react-native-contact-picker`.

### Added

- `pickContactPhone()` returns one selected phone number and display name, or `null` on cancellation.
- Native Android and iOS pickers without requesting broad contacts permissions.
- React Native New Architecture support with TypeScript Codegen and TurboModules.
- Explicit number selection for contacts with multiple phone numbers, busy-state errors and lifecycle cleanup.
- JavaScript API tests, Android Robolectric tests, CI builds, and an example with sample contacts and a manual test checklist.

### Compatibility

- Requires React Native 0.88.0 or later and React 19.3.0 or later; currently validated with 0.88.0 / 19.3.0.
- Requires New Architecture and a native build; Expo Go is not supported.
- Android library minimum API is 24; the consuming app and React Native may require a higher minimum.
- iOS minimum deployment target follows the installed React Native version.
