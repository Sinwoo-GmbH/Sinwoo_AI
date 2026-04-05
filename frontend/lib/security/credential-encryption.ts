import type { CredentialKeyResponse } from "@/lib/api/auth-contract";

function toArrayBuffer(bytes: Uint8Array): ArrayBuffer {
  return bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength) as ArrayBuffer;
}

function decodeBase64(value: string): ArrayBuffer {
  const normalized = value.replace(/\s+/g, "");
  const binary = window.atob(normalized);
  const bytes = new Uint8Array(binary.length);

  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index);
  }

  return toArrayBuffer(bytes);
}

function encodeBase64(bytes: Uint8Array): string {
  let binary = "";
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return window.btoa(binary);
}

export async function encryptCredentialPassword(
  plainPassword: string,
  credentialKey: CredentialKeyResponse
): Promise<string> {
  if (!window.isSecureContext || !window.crypto?.subtle) {
    throw new Error("Secure credential encryption is not available in this browser.");
  }

  if (credentialKey.alg !== "RSA_OAEP_SHA256") {
    throw new Error("Unsupported credential encryption algorithm.");
  }

  const passwordBytes = new TextEncoder().encode(plainPassword);
  const importedKey = await window.crypto.subtle.importKey(
    "spki",
    decodeBase64(credentialKey.publicKey),
    {
      name: "RSA-OAEP",
      hash: "SHA-256",
    },
    false,
    ["encrypt"]
  );

  const encrypted = await window.crypto.subtle.encrypt(
    { name: "RSA-OAEP" },
    importedKey,
    toArrayBuffer(passwordBytes)
  );

  return encodeBase64(new Uint8Array(encrypted));
}
