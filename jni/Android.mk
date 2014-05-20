LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog
LOCAL_MODULE    := hackdavilk
LOCAL_SRC_FILES := ctripdavilk.c
include $(BUILD_SHARED_LIBRARY)
