#include <jni.h>
#include <fcntl.h>
#include <unistd.h>
#include <linux/input.h>
#include <stdio.h>
#include <string.h>
#include <android/log.h>

#define LOG_TAG "KEYPAD_JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define KEYPAD_EVENT_DEV "/dev/input/event5"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_jni_1mosysf_GameActivity_onKeyInput(JNIEnv *env, jobject thiz) {
    struct input_event ev;
    int fd = open(KEYPAD_EVENT_DEV, O_RDONLY);
    if (fd < 0) {
        LOGE("Failed to open device: %s", KEYPAD_EVENT_DEV);
        return env->NewStringUTF("ERROR_OPEN");
    }

    while (1) {
        ssize_t n = read(fd, &ev, sizeof(ev));
        if (n == sizeof(ev)) {
            if (ev.type == EV_KEY && ev.value == 1) {
                close(fd);

                const char* result;

                switch (ev.code) {
                    case KEY_1: result = "KEY_1"; break;
                    case KEY_2: result = "KEY_2"; break;
                    case KEY_3: result = "KEY_3"; break;
                    case KEY_4: result = "KEY_4"; break;
                    case KEY_5: result = "KEY_5"; break;
                    case KEY_6: result = "KEY_6"; break;
                    case KEY_7: result = "KEY_7"; break;
                    case KEY_8: result = "KEY_8"; break;
                    case KEY_9: result = "KEY_9"; break;
                    case KEY_0: result = "KEY_0"; break;
                    case KEY_ENTER: result = "KEY_ENTER"; break;
                    default: result = "UNKNOWN"; break;
                }

                LOGI("KEY PRESS: code = %d → %s", ev.code, result);
                return env->NewStringUTF(result);
            }
        } else if (n == -1) {
            perror("read error");
            close(fd);
            return env->NewStringUTF("ERROR_READ");
        }
    }

    close(fd);
    return env->NewStringUTF("ERROR_LOOP");
}
