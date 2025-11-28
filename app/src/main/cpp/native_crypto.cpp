#include <jni.h>
#include <string>
#include <vector>
#include <cstring>
#include <android/log.h>

#define LOG_TAG "BancoCrypto"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

class SimpleXORCrypto {
private:
    static constexpr const char* KEY = "BancoApp2024SecureKey!@#$%";
    static constexpr size_t KEY_LENGTH = 26;

public:
    static std::vector<uint8_t> encrypt(const std::vector<uint8_t>& data) {
        std::vector<uint8_t> result(data.size());
        for (size_t i = 0; i < data.size(); ++i) {
            result[i] = data[i] ^ KEY[i % KEY_LENGTH] ^ (i & 0xFF);
        }
        return result;
    }

    static std::vector<uint8_t> decrypt(const std::vector<uint8_t>& data) {
        return encrypt(data);
    }
};

class AdvancedCrypto {
public:
    static std::vector<uint8_t> obfuscate(const std::vector<uint8_t>& data) {
        std::vector<uint8_t> result = data;
        
        for (size_t i = 0; i < result.size(); ++i) {
            result[i] = (result[i] << 3) | (result[i] >> 5);
        }
        
        for (size_t i = 0; i < result.size() / 2; ++i) {
            std::swap(result[i], result[result.size() - 1 - i]);
        }
        
        return SimpleXORCrypto::encrypt(result);
    }

    static std::vector<uint8_t> deobfuscate(const std::vector<uint8_t>& data) {
        std::vector<uint8_t> result = SimpleXORCrypto::decrypt(data);
        
        for (size_t i = 0; i < result.size() / 2; ++i) {
            std::swap(result[i], result[result.size() - 1 - i]);
        }
        
        for (size_t i = 0; i < result.size(); ++i) {
            result[i] = (result[i] >> 3) | (result[i] << 5);
        }
        
        return result;
    }
};

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_bancoapp_security_NativeCrypto_encryptData(
    JNIEnv* env,
    jobject,
    jbyteArray data) {
    
    if (data == nullptr) {
        return nullptr;
    }

    jsize dataLen = env->GetArrayLength(data);
    jbyte* dataBytes = env->GetByteArrayElements(data, nullptr);

    std::vector<uint8_t> input(dataBytes, dataBytes + dataLen);
    env->ReleaseByteArrayElements(data, dataBytes, JNI_ABORT);

    std::vector<uint8_t> encrypted = AdvancedCrypto::obfuscate(input);

    jbyteArray result = env->NewByteArray(encrypted.size());
    env->SetByteArrayRegion(result, 0, encrypted.size(), 
                           reinterpret_cast<const jbyte*>(encrypted.data()));

    LOGI("Encrypted %zu bytes", encrypted.size());
    return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_bancoapp_security_NativeCrypto_decryptData(
    JNIEnv* env,
    jobject,
    jbyteArray data) {
    
    if (data == nullptr) {
        return nullptr;
    }

    jsize dataLen = env->GetArrayLength(data);
    jbyte* dataBytes = env->GetByteArrayElements(data, nullptr);

    std::vector<uint8_t> input(dataBytes, dataBytes + dataLen);
    env->ReleaseByteArrayElements(data, dataBytes, JNI_ABORT);

    std::vector<uint8_t> decrypted = AdvancedCrypto::deobfuscate(input);

    jbyteArray result = env->NewByteArray(decrypted.size());
    env->SetByteArrayRegion(result, 0, decrypted.size(), 
                           reinterpret_cast<const jbyte*>(decrypted.data()));

    LOGI("Decrypted %zu bytes", decrypted.size());
    return result;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bancoapp_security_NativeCrypto_getSecurityToken(
    JNIEnv* env,
    jobject) {
    
    std::string token = "BANCO_SECURE_TOKEN_" + std::to_string(time(nullptr));
    return env->NewStringUTF(token.c_str());
}
