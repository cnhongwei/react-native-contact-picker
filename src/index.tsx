import { getNativeContactPicker } from './NativeContactPicker';
import type { ContactPhone } from './NativeContactPicker';

export type { ContactPhone } from './NativeContactPicker';

/** Select one phone number using the system UI. Cancellation resolves to null. */
export async function pickContactPhone(): Promise<ContactPhone | null> {
  return getNativeContactPicker().pickContactPhone();
}
