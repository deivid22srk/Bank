use jni::JNIEnv;
use jni::objects::{JClass, JString, JByteArray};
use jni::sys::jbyteArray;
use aes_gcm::{
    aead::{Aead, KeyInit},
    Aes256Gcm, Nonce,
};
use sha2::{Sha256, Digest};
use rand::RngCore;

#[no_mangle]
pub extern "C" fn Java_com_bancoseguro_app_security_NativeCrypto_encryptData(
    env: JNIEnv,
    _class: JClass,
    data: JByteArray,
    key: JByteArray,
) -> jbyteArray {
    let data_bytes = match env.convert_byte_array(&data) {
        Ok(bytes) => bytes,
        Err(_) => return JByteArray::default().into_raw(),
    };

    let key_bytes = match env.convert_byte_array(&key) {
        Ok(bytes) => bytes,
        Err(_) => return JByteArray::default().into_raw(),
    };

    if key_bytes.len() != 32 {
        return JByteArray::default().into_raw();
    }

    let cipher = match Aes256Gcm::new_from_slice(&key_bytes) {
        Ok(c) => c,
        Err(_) => return JByteArray::default().into_raw(),
    };

    let mut nonce_bytes = [0u8; 12];
    rand::thread_rng().fill_bytes(&mut nonce_bytes);
    let nonce = Nonce::from_slice(&nonce_bytes);

    let ciphertext = match cipher.encrypt(nonce, data_bytes.as_ref()) {
        Ok(ct) => ct,
        Err(_) => return JByteArray::default().into_raw(),
    };

    let mut result = nonce_bytes.to_vec();
    result.extend_from_slice(&ciphertext);

    match env.byte_array_from_slice(&result) {
        Ok(arr) => arr.into_raw(),
        Err(_) => JByteArray::default().into_raw(),
    }
}

#[no_mangle]
pub extern "C" fn Java_com_bancoseguro_app_security_NativeCrypto_decryptData(
    env: JNIEnv,
    _class: JClass,
    encrypted_data: JByteArray,
    key: JByteArray,
) -> jbyteArray {
    let encrypted_bytes = match env.convert_byte_array(&encrypted_data) {
        Ok(bytes) => bytes,
        Err(_) => return JByteArray::default().into_raw(),
    };

    let key_bytes = match env.convert_byte_array(&key) {
        Ok(bytes) => bytes,
        Err(_) => return JByteArray::default().into_raw(),
    };

    if key_bytes.len() != 32 || encrypted_bytes.len() < 12 {
        return JByteArray::default().into_raw();
    }

    let cipher = match Aes256Gcm::new_from_slice(&key_bytes) {
        Ok(c) => c,
        Err(_) => return JByteArray::default().into_raw(),
    };

    let nonce = Nonce::from_slice(&encrypted_bytes[..12]);
    let ciphertext = &encrypted_bytes[12..];

    let plaintext = match cipher.decrypt(nonce, ciphertext) {
        Ok(pt) => pt,
        Err(_) => return JByteArray::default().into_raw(),
    };

    match env.byte_array_from_slice(&plaintext) {
        Ok(arr) => arr.into_raw(),
        Err(_) => JByteArray::default().into_raw(),
    }
}

#[no_mangle]
pub extern "C" fn Java_com_bancoseguro_app_security_NativeCrypto_hashPassword(
    mut env: JNIEnv,
    _class: JClass,
    password: JString,
    salt: JByteArray,
) -> jbyteArray {
    let password_str: String = match env.get_string(&password) {
        Ok(s) => s.into(),
        Err(_) => return JByteArray::default().into_raw(),
    };

    let salt_bytes = match env.convert_byte_array(&salt) {
        Ok(bytes) => bytes,
        Err(_) => return JByteArray::default().into_raw(),
    };

    let mut hasher = Sha256::new();
    hasher.update(password_str.as_bytes());
    hasher.update(&salt_bytes);
    
    for _ in 0..10000 {
        let temp_hash = hasher.finalize_reset();
        hasher.update(temp_hash);
        hasher.update(&salt_bytes);
    }
    
    let result = hasher.finalize();

    match env.byte_array_from_slice(&result) {
        Ok(arr) => arr.into_raw(),
        Err(_) => JByteArray::default().into_raw(),
    }
}

#[no_mangle]
pub extern "C" fn Java_com_bancoseguro_app_security_NativeCrypto_generateKey(
    env: JNIEnv,
    _class: JClass,
) -> jbyteArray {
    let mut key = [0u8; 32];
    rand::thread_rng().fill_bytes(&mut key);

    match env.byte_array_from_slice(&key) {
        Ok(arr) => arr.into_raw(),
        Err(_) => JByteArray::default().into_raw(),
    }
}

#[no_mangle]
pub extern "C" fn Java_com_bancoseguro_app_security_NativeCrypto_obfuscateTraffic(
    env: JNIEnv,
    _class: JClass,
    data: JByteArray,
) -> jbyteArray {
    let mut data_bytes = match env.convert_byte_array(&data) {
        Ok(bytes) => bytes,
        Err(_) => return JByteArray::default().into_raw(),
    };

    let padding_size = rand::thread_rng().next_u32() % 64 + 16;
    let mut padding = vec![0u8; padding_size as usize];
    rand::thread_rng().fill_bytes(&mut padding);

    let mut result = Vec::new();
    result.extend_from_slice(&(data_bytes.len() as u32).to_be_bytes());
    result.append(&mut data_bytes);
    result.append(&mut padding);

    for i in 0..result.len() {
        result[i] ^= 0x5A ^ ((i % 256) as u8);
    }

    match env.byte_array_from_slice(&result) {
        Ok(arr) => arr.into_raw(),
        Err(_) => JByteArray::default().into_raw(),
    }
}

#[no_mangle]
pub extern "C" fn Java_com_bancoseguro_app_security_NativeCrypto_deobfuscateTraffic(
    env: JNIEnv,
    _class: JClass,
    obfuscated: JByteArray,
) -> jbyteArray {
    let mut obfuscated_bytes = match env.convert_byte_array(&obfuscated) {
        Ok(bytes) => bytes,
        Err(_) => return JByteArray::default().into_raw(),
    };

    for i in 0..obfuscated_bytes.len() {
        obfuscated_bytes[i] ^= 0x5A ^ ((i % 256) as u8);
    }

    if obfuscated_bytes.len() < 4 {
        return JByteArray::default().into_raw();
    }

    let data_size = u32::from_be_bytes([
        obfuscated_bytes[0],
        obfuscated_bytes[1],
        obfuscated_bytes[2],
        obfuscated_bytes[3],
    ]) as usize;

    if obfuscated_bytes.len() < 4 + data_size {
        return JByteArray::default().into_raw();
    }

    let data = &obfuscated_bytes[4..4 + data_size];

    match env.byte_array_from_slice(data) {
        Ok(arr) => arr.into_raw(),
        Err(_) => JByteArray::default().into_raw(),
    }
}
