/*
 * [CS-0010] [CS-0020] [CS-0030] Quality Standards, AI Rules & Java Coding Rules Compliance.
 * Reference: FR-20260809-004 / TSK-20260809-004.7 - OpenAI Whisper C++ JNI Implementation.
 */
#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include "whisper.h"

#define LOG_TAG "AeonFlux_WhisperJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_aeonflux_app_core_media_whisper_WhisperNativeBridge_nativeInitContext(
        JNIEnv *env,
        jobject thiz,
        jstring model_path) {
    if (model_path == nullptr) {
        LOGE("[WHISPER-JNI-INIT-ERROR] Model path string is null.");
        return 0;
    }

    const char *path = env->GetStringUTFChars(model_path, nullptr);
    LOGI("[WHISPER-JNI-INIT] Initializing C++ whisper_context from model path: %s", path);

    struct whisper_context_params cparams = whisper_context_default_params();
    struct whisper_context *ctx = whisper_init_from_file_with_params(path, cparams);

    env->ReleaseStringUTFChars(model_path, path);

    if (ctx == nullptr) {
        LOGE("[WHISPER-JNI-INIT-ERROR] Failed to load Whisper context from file!");
        return 0;
    }

    LOGI("[WHISPER-JNI-INIT-SUCCESS] Whisper C++ context created successfully at pointer %p", ctx);
    return reinterpret_cast<jlong>(ctx);
}

JNIEXPORT jstring JNICALL
Java_com_aeonflux_app_core_media_whisper_WhisperNativeBridge_nativeTranscribePcm(
        JNIEnv *env,
        jobject thiz,
        jlong context_ptr,
        jfloatArray pcm_samples,
        jstring language) {
    if (context_ptr == 0) {
        LOGE("[WHISPER-JNI-TRANSCRIBE-ERROR] Null Whisper context pointer!");
        return env->NewStringUTF("");
    }

    if (pcm_samples == nullptr) {
        LOGE("[WHISPER-JNI-TRANSCRIBE-ERROR] Null PCM samples float array!");
        return env->NewStringUTF("");
    }

    struct whisper_context *ctx = reinterpret_cast<struct whisper_context *>(context_ptr);
    jsize num_samples = env->GetArrayLength(pcm_samples);
    jfloat *samples = env->GetFloatArrayElements(pcm_samples, nullptr);

    const char *lang_str = "fr";
    if (language != nullptr) {
        lang_str = env->GetStringUTFChars(language, nullptr);
    }

    LOGI("[WHISPER-JNI-TRANSCRIBE] Starting C++ whisper_full inference on %d float PCM samples (Lang: %s)", num_samples, lang_str);

    struct whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.print_progress = false;
    params.print_special = false;
    params.print_realtime = false;
    params.print_timestamps = false;
    params.translate = false;
    params.language = lang_str;
    params.n_threads = 4;
    params.no_context = true;
    params.single_segment = true;
    params.greedy.best_of = 1;

    int result_code = whisper_full(ctx, params, samples, num_samples);

    env->ReleaseFloatArrayElements(pcm_samples, samples, JNI_ABORT);
    if (language != nullptr) {
        env->ReleaseStringUTFChars(language, lang_str);
    }

    if (result_code != 0) {
        LOGE("[WHISPER-JNI-TRANSCRIBE-ERROR] whisper_full returned error code: %d", result_code);
        return env->NewStringUTF("");
    }

    std::string decoded_text = "";
    int num_segments = whisper_full_n_segments(ctx);
    for (int i = 0; i < num_segments; i++) {
        const char *text = whisper_full_get_segment_text(ctx, i);
        if (text != nullptr) {
            decoded_text += text;
        }
    }

    LOGI("[WHISPER-JNI-TRANSCRIBE-SUCCESS] Decoded %d segments (%d chars): '%s'", num_segments, (int)decoded_text.length(), decoded_text.c_str());
    return env->NewStringUTF(decoded_text.c_str());
}

JNIEXPORT void JNICALL
Java_com_aeonflux_app_core_media_whisper_WhisperNativeBridge_nativeFreeContext(
        JNIEnv *env,
        jobject thiz,
        jlong context_ptr) {
    if (context_ptr != 0) {
        struct whisper_context *ctx = reinterpret_cast<struct whisper_context *>(context_ptr);
        LOGI("[WHISPER-JNI-FREE] Freeing Whisper context pointer %p", ctx);
        whisper_free(ctx);
    }
}

} // extern "C"
