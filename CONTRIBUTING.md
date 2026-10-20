# Contributing

Contributions are always welcome, no matter how large or small!

We want this community to be friendly and respectful to each other. Please follow it in all your interactions with the project. Before contributing, please read the [code of conduct](./CODE_OF_CONDUCT.md).

## Development workflow

This project is a monorepo managed using [Yarn workspaces](https://yarnpkg.com/features/workspaces). It contains the following packages:

- The library package in the root directory.
- An example app in the `example/` directory.

To get started with the project, make sure you have the correct version of [Node.js](https://nodejs.org/) installed. See the [`.nvmrc`](./.nvmrc) file for the version used in this project. Use Yarn 4.11.0, as pinned in `package.json`.

Run `yarn` in the root directory to install the required dependencies for each package:

```sh
yarn
```

> Since the project relies on Yarn workspaces, you cannot use [`npm`](https://github.com/npm/cli) for development without manually migrating.

The [example app](./example/README.md) demonstrates usage of the library. You need to run it to test any changes you make.

It is configured to use the local version of the library, so any changes you make to the library's source code will be reflected in the example app. Changes to the library's JavaScript code will be reflected in the example app without a rebuild, but native code changes will require a rebuild of the example app.

If you want to use Android Studio or Xcode to edit the native code, you can open the `example/android` or `example/ios` directories respectively in those editors. To edit the Objective-C or Swift files, open `example/ios/ContactPickerExample.xcworkspace` in Xcode and find the source files at `Pods > Development Pods > ContactPicker`.

To edit the Java or Kotlin files, open `example/android` in Android studio and find the source files at `cnhongwei_react-native-contact-picker` under `Android`.

You can use various commands from the root directory to work with the project.

To start the packager:

```sh
yarn example start
```

To run the example app on Android:

```sh
yarn example android
```

To run the example app on iOS:

```sh
yarn example ios
```

The library requires React Native New Architecture. The example uses React Native 0.88.0 and React 19.3.0. Install iOS dependencies with `cd example && bundle install && bundle exec pod install --project-directory=ios` before the first iOS build.

Make sure your code passes TypeScript:

```sh
yarn typecheck
```

To check for linting errors, run the following:

```sh
yarn lint
```

To fix formatting errors, run the following:

```sh
yarn lint --fix
```

### Scripts

- `yarn`: install workspace dependencies.
- `yarn typecheck`: check TypeScript types.
- `yarn lint`: check ESLint and formatting.
- `yarn test`: run JavaScript API tests.
- `yarn test:android`: run Android Robolectric tests (requires JDK 17 and the Android SDK).
- `yarn prepare`: build the library JavaScript and type declarations.
- `yarn example start`: start Metro.
- `yarn example android`: build and run the Android example.
- `yarn example ios`: build and run the iOS example.

Before submitting, run the type, lint, JavaScript and Android checks above, build both native examples, and follow the [manual test checklist](./example/README.md#manual-test-checklist). Do not commit generated builds, Pods, local SDK paths or simulator address books.

### Sending a pull request

> **Working on your first pull request?** You can learn how from this _free_ series: [How to Contribute to an Open Source Project on GitHub](https://app.egghead.io/playlists/how-to-contribute-to-an-open-source-project-on-github).

When you're sending a pull request:

- Prefer small pull requests focused on one change.
- Verify that linters and tests are passing.
- Review the documentation to make sure it looks good.
- Follow the pull request template when opening a pull request.
- For pull requests that change the API or implementation, discuss with maintainers first by opening an issue.
