# Contact Picker example

This app uses the library source in the parent workspace. Tap **Choose contact phone** to select a name and one phone number. The result appears below the button; cancellation preserves the previous selection and errors appear as status text.

## Setup and run

Run these commands from the repository root. Use the Node version in `.nvmrc`, the pinned Yarn 4.11.0, JDK 17 and an installed Android SDK. iOS requires macOS, Xcode and CocoaPods through Bundler.

```sh
yarn install --immutable
cd example
bundle install
bundle exec pod install --project-directory=ios
cd ..
yarn example start
```

Keep Metro running. In another terminal, with a simulator or emulator already booted:

```sh
# Android
yarn example android

# iOS
yarn example ios
```

When multiple devices are running, use `yarn example android --help` or `yarn example ios --help` to select the intended device. Android emulators can reach Metro through `adb -s <serial> reverse tcp:8081 tcp:8081`.

JavaScript changes reload through Metro; native changes require a rebuild. If the library package name changes and Gradle still reports the old module name, remove the generated `example/android/build/generated/autolinking` directory and rebuild to refresh the autolinking cache.

## Sample contacts

[fixtures/test-contacts.vcf](fixtures/test-contacts.vcf) contains six fictional test contacts: Chinese names, English names, single and multiple phone numbers, international and local number formats, and a contact with only an email address. Import once per simulator to avoid duplicates. Existing contacts do not need to be removed.

On an iOS simulator, use its UUID from `xcrun simctl list devices booted`:

```sh
xcrun simctl addmedia <device-uuid> example/fixtures/test-contacts.vcf
```

On Android, copy the file to the emulator, then use the Contacts app's import-from-file option (the menu varies by provider):

```sh
adb -s <serial> push example/fixtures/test-contacts.vcf /sdcard/Download/test-contacts.vcf
```

The import happens outside the example app. Do not add contacts permissions to the example to seed data.

## Manual test checklist

- Select a contact with one number and confirm the displayed name and number.
- Select **Alice Test** or **张三（测试）** and choose each of their numbers separately.
- Confirm number formatting is preserved, including country codes and spaces.
- Check that a contact without a phone number cannot produce a selection (disabled on iOS or excluded by the Android phone picker).
- Cancel with the picker button, Android Back or iOS swipe dismissal; the previous selection should remain.
- Try repeated taps and background/foreground transitions while the picker is open.
- Confirm no address-book permission is requested. Development networking permissions are separate from contacts access.

Run on physical devices and different Android contact providers before release; simulator checks alone do not cover those environments.

For automated checks and native build commands, see the [root README](../README.md#development-and-validation).
