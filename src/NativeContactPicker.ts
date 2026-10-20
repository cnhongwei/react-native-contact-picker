import { TurboModuleRegistry, type TurboModule } from 'react-native';

export type ContactPhone = {
  name: string;
  phoneNumber: string;
};

export interface Spec extends TurboModule {
  pickContactPhone(): Promise<ContactPhone | null>;
}

// Resolve lazily so importing a shared barrel does not require a native binary.
export function getNativeContactPicker(): Spec {
  return TurboModuleRegistry.getEnforcing<Spec>('ContactPicker');
}
