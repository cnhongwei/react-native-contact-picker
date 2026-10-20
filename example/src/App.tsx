import { useState } from 'react';
import { Button, Text, View, StyleSheet } from 'react-native';
import {
  pickContactPhone,
  type ContactPhone,
} from '@cnhongwei/react-native-contact-picker';

export default function App() {
  const [contact, setContact] = useState<ContactPhone | null>(null);
  const [busy, setBusy] = useState(false);
  const [status, setStatus] = useState(
    'Choose one phone number. No contacts permission is requested.'
  );

  async function pick() {
    setBusy(true);
    try {
      const selection = await pickContactPhone();
      if (selection) setContact(selection);
      setStatus(
        selection ? 'Selected' : 'Cancelled — previous selection preserved'
      );
    } catch (error) {
      setStatus(
        error instanceof Error ? error.message : 'Could not open the picker'
      );
    } finally {
      setBusy(false);
    }
  }

  return (
    <View style={styles.container}>
      <Button title="Choose contact phone" onPress={pick} disabled={busy} />
      <Text accessibilityRole="alert">{status}</Text>
      {contact && (
        <Text>
          {contact.name}: {contact.phoneNumber}
        </Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, gap: 16, justifyContent: 'center' },
});
