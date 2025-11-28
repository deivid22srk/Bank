#include <jni.h>
#include <string>
#include <android/log.h>
#include <sys/system_properties.h>

#define LOG_TAG "BancoNetwork"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bancoapp_security_NetworkSecurity_isDeviceSecure(
    JNIEnv* env,
    jobject) {
    
    char value[PROP_VALUE_MAX];
    
    if (__system_property_get("ro.debuggable", value) > 0) {
        if (strcmp(value, "1") == 0) {
            LOGW("Device is debuggable");
            return JNI_FALSE;
        }
    }
    
    if (__system_property_get("ro.secure", value) > 0) {
        if (strcmp(value, "0") == 0) {
            LOGW("Device is not secure");
            return JNI_FALSE;
        }
    }
    
    LOGI("Device security check passed");
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_bancoapp_security_NetworkSecurity_validateConnection(
    JNIEnv* env,
    jobject,
    jstring url) {
    
    if (url == nullptr) {
        return JNI_FALSE;
    }

    const char* urlStr = env->GetStringUTFChars(url, nullptr);
    std::string urlString(urlStr);
    env->ReleaseStringUTFChars(url, urlStr);

    if (urlString.find("https://") != 0) {
        LOGW("Connection is not HTTPS: %s", urlString.c_str());
        return JNI_FALSE;
    }

    if (urlString.find("firebaseio.com") != std::string::npos ||
        urlString.find("googleapis.com") != std::string::npos ||
        urlString.find("google.com") != std::string::npos) {
        LOGI("Connection to trusted domain: %s", urlString.c_str());
        return JNI_TRUE;
    }

    LOGW("Unknown domain: %s", urlString.c_str());
    return JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bancoapp_security_NetworkSecurity_obfuscateEndpoint(
    JNIEnv* env,
    jobject,
    jstring endpoint) {
    
    if (endpoint == nullptr) {
        return nullptr;
    }

    const char* endpointStr = env->GetStringUTFChars(endpoint, nullptr);
    std::string obfuscated = std::string(endpointStr);
    env->ReleaseStringUTFChars(endpoint, endpointStr);

    for (size_t i = 0; i < obfuscated.length(); ++i) {
        obfuscated[i] = obfuscated[i] ^ 0x5A;
    }

    return env->NewStringUTF(obfuscated.c_str());
}
