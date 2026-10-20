# @cnhongwei/react-native-contact-picker

Select **one phone number and its display name** through the native contact picker, without requesting address-book access. Built with `create-react-native-library` 0.63.1: TypeScript Codegen + TurboModule, Kotlin on Android and Objective-C++ on iOS. MIT licensed.

## Usage

```tsx
import { pickContactPhone } from '@cnhongwei/react-native-contact-picker';

const selection = await pickContactPhone();
if (selection) {
  // Fill editable fields. Only submit when the user confirms your form.
  setName(selection.name);
  setPhone(selection.phoneNumber);
}
```

`Promise<ContactPhone | null>` returns `{ name: string, phoneNumber: string }`. Cancellation resolves to `null`; failures reject with a `code`. The phone number retains the device's formatting, so normalize it in the consuming app if necessary. A missing display name is an empty string.

Both platforms let the user choose a specific number for contacts with multiple phone numbers. No contacts are enumerated, uploaded or logged. No email addresses, photos or other contact properties are returned.

## Native behavior

- Android: `ACTION_PICK` with `CommonDataKinds.Phone.CONTENT_URI`; query **only the returned URI** for `DISPLAY_NAME` and `NUMBER`. The temporary grant is sufficient; there is no `READ_CONTACTS` / `WRITE_CONTACTS` declaration or permission request, and no fallback to broad access. No query by contact ID or URI sub-path traversal.
- iOS: `CNContactPickerViewController` with phone-property selection. Contacts without phone numbers are disabled, and multiple numbers require explicit selection. No `CNContactStore` authorization or `NSContactsUsageDescription` is needed.
- Repeated calls reject with `E_PICKER_BUSY`. Cancellation preserves the caller's existing form values. Module invalidation rejects pending work and releases native resources.

Errors include `E_PICKER_BUSY`, `E_NO_ACTIVITY` / `E_NO_VIEW_CONTROLLER`, `E_UNAVAILABLE`, `E_PRESENT_PICKER`, `E_INVALID_RESULT`, `E_READ_CONTACT`, and `E_INVALIDATED`. A missing linked native module also rejects. Keep manual input available if a device has no compatible picker.

## Installation and compatibility

After publication, install with `npm install @cnhongwei/react-native-contact-picker`. For local testing, install the packed library into your app, install iOS pods, then rebuild the native application. Expo development builds/prebuild are supported through React Native autolinking; Expo Go and an OTA-only installation are not supported. New Architecture is required. The generated example targets React Native 0.88.0, Android API 24+ (the host may require a higher minimum), and React Native's current iOS deployment target. Earlier React Native versions have not been verified.

```sh
corepack yarn install
corepack yarn prepare
npm pack --ignore-scripts
# In the consuming app:
yarn add /path/to/cnhongwei-react-native-contact-picker-0.1.1.tgz
```

The library does not remove permissions added by other packages. Remove broad contact permissions in the host app separately when no longer needed. A permission-free picker does not exempt the app's data use from store policies.

## Development and validation

```sh
corepack yarn typecheck
corepack yarn lint
corepack yarn test
corepack yarn test:android
corepack yarn prepare
cd example/android
./gradlew :app:assembleDebug
# iOS:
cd ../ios
pod install
xcodebuild -workspace ContactPickerExample.xcworkspace -scheme ContactPickerExample \
  -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' \
  CODE_SIGNING_ALLOWED=NO build
```

See [the example guide](example/README.md) for simulator setup, sample contacts and manual test cases.

JavaScript tests cover lazy native-module loading, selection, cancellation and error propagation. Android Robolectric tests cover the URI scope/projection, missing data, cancellation, repeated calls, invalid results and lifecycle cleanup. The example demonstrates selection, cancellation and error handling without logging contact data.

Before a release, test on physical Android and iOS devices with no contacts permission granted: single/multiple numbers, contacts without numbers, cancel/back/swipe dismissal, repeat taps, background/foreground, and different Android contact providers. Confirm the final app manifest contains no broad contacts permissions and the app does not request authorization.
