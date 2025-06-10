#include <jni.h>
#include <string>
#include <android/log.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>

#define LCD_DEV "/dev/fpga_text_lcd"
#define LOG_TAG "LCD_JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_example_jni_1mosysf_GameActivity_sendTurnToBoard(JNIEnv* env, jobject thiz, jstring turnText) {
    const char* text = env->GetStringUTFChars(turnText, NULL);  // C++에서는 nullptr 사용 가능

    int fd = open("/dev/fpga_textlcd", O_WRONLY);
    if (fd < 0) {
        LOGI("LCD open failed");
        env->ReleaseStringUTFChars(turnText, text);
        return;
    }

    write(fd, text, strlen(text));  // LCD에 문자열 출력

    close(fd);
    LOGI("LCD 출력됨: %s", text);
    env->ReleaseStringUTFChars(turnText, text);
}
