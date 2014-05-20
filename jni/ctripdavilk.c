#include <string.h>
#include <stdint.h>
#include <jni.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <dirent.h>
#include <time.h>
#include <errno.h>
#include <pthread.h>
#include <dlfcn.h>

#include <sys/ioctl.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/inotify.h>
#include <sys/limits.h>
#include <sys/poll.h>

#include <linux/fb.h>
#include <linux/kd.h>
#include <linux/input.h>
#include <android/log.h>

#define USE_ASHMEM 1

#ifdef USE_ASHMEM
#include <linux/ashmem.h>
#endif /* USE_ASHMEM */

#define TAG "Ctrip::JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

typedef struct teather {
	char* time;
	int point;
} teather;

struct student {
	char* name;
	int score;
	teather* currenteam;
} stu;

typedef struct CtripAllocHdr {
    int     curOffset;          /* offset where next data goes */
    pthread_mutex_t lock;       /* controls updates to this struct */

    char*   mapAddr;            /* start of mmap()ed region */
    int     mapLength;          /* length of region */
    int     firstOffset;        /* for chasing through */

    short*  writeRefCount;      /* for ENFORCE_READ_ONLY */
} CtripAllocHdr;


int ashmem_create_region(const char *name, size_t size)
{
    int fd, ret;

    fd = open("/dev/ashmem", O_RDWR);
    if (fd < 0)
        return fd;

    if (name) {
        char buf[ASHMEM_NAME_LEN];

        strlcpy(buf, name, sizeof(buf));
        ret = ioctl(fd, ASHMEM_SET_NAME, buf);
        if (ret < 0)
            goto error;
    }

    ret = ioctl(fd, ASHMEM_SET_SIZE, size);
    if (ret < 0)
        goto error;
    return fd;

error:
    close(fd);
    return ret;
}

teather* mTeam;
#define MEM_DEVICE "/dev/mem"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;

	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		return -1;
	}
	LOGD("Jvm malloc %p", vm);
//
	void *handle = dlopen("/system/lib/libdvm.so", RTLD_LAZY);
	void *p_gDvm = (void*) dlsym(handle, "gDvm");
	char* str = (int) p_gDvm;
	LOGD("boot malloc %s", str);
	str = (int) p_gDvm + 4;
	LOGD("boot malloc %s", str);
	int* boot = (int) p_gDvm + 8;
	LOGD("boot malloc %d", (*boot)/1024);
	boot = (int) p_gDvm + 12;
	LOGD("boot malloc %d", (*boot)/1024);
	boot = (int) p_gDvm + 16;
	LOGD("boot malloc %d", (*boot)/1024);
	int i = 0;
	int archPosition = (int) p_gDvm;
	for (i = 0; i < 300; i++) {
		int *t = i * 4 + (int) p_gDvm;
		if (*t == vm) {
			archPosition = i * 4 + (int) p_gDvm;
			LOGE("position match %d", i);
			break;
		}
	}

	int linearPosInGvm = archPosition + 6 * 4;
	int* linearmalloc = linearPosInGvm;

	CtripAllocHdr **allocHdr = linearPosInGvm;

	LOGD("CtripAllocHdr->curOffset: %d",(*allocHdr)->curOffset);
	LOGD("CtripAllocHdr->mapAddr: %p", (*allocHdr)->mapAddr);
	LOGD("CtripAllocHdr->mapLength: %d", (*allocHdr)->mapLength);
	LOGD("CtripAllocHdr->firstOffset: %d", (*allocHdr)->firstOffset);
	LOGD("CtripAllocHdr->writeRefCount: %d", (*allocHdr)->writeRefCount);

	LOGD("CtripAllocHdr location %p", (*allocHdr));

	//Method 1
//	char* newAddr = mmap(NULL, 8 * 1024 * 1024, PROT_READ | PROT_WRITE,
//	        MAP_PRIVATE | MAP_ANON, -1, 0);
//	if (newAddr == MAP_FAILED) {
//		LOGD("LinearAlloc mmap(%d) failed: %s\n", 8 * 1024 * 1024,
//			strerror(errno));
//		free(newAddr);
//	}

	//Method 2
	int fd;
	fd = ashmem_create_region("dalvik-LinearAlloc", 8 * 1024 * 1024);
	char* newAddr = mmap(NULL, 8 * 1024 * 1024, PROT_READ | PROT_WRITE,
			MAP_SHARED, fd, 0);
	close(fd);

	LOGD("Create new map: %p", newAddr);

	unsigned char * addr = (*allocHdr)->mapAddr;

	mprotect(addr, (*allocHdr)->mapLength, PROT_READ);
	memcpy(newAddr, addr, (*allocHdr)->mapLength);

	munmap(addr, (*allocHdr)->mapLength);

//	memcpy(newAddr+(*allocHdr)->firstOffset, addr + (*allocHdr)->firstOffset, (*allocHdr)->curOffset - (*allocHdr)->firstOffset + 1);
	LOGD("Memcpy");

	(*allocHdr)->mapLength = 8 * 1024 * 1024;
	(*allocHdr)->mapAddr = newAddr;

	LOGD("Kick ass!!!");

//	LOGD("LinearAlloc location %p", *linearmalloc);
//	unsigned int starPositon = *linearmalloc;
//
//	char* oldMapAddr = starPositon + 4 * 2;
//	LOGD("!!!!oldMapAddr location: %p", oldMapAddr);
//
//	int *lenghtvalue = starPositon + 4 * 3;
//
//	LOGD("maplength value %d", *lenghtvalue);
//
//	int oldsize = *lenghtvalue;
//	char* newAddr = mmap(NULL,8 * 1024 * 1024, PROT_READ | PROT_WRITE,
//	        MAP_PRIVATE | MAP_ANON, -1, 0);
//	LOGD("creat new map ");

//	int j;
//	for (j = 0; j < oldsize; j++)
//	{
//		LOGD("Content: %.2x", oldMapAddr[j]);
//	}
//	memcpy(newAddr, oldMapAddr, oldsize);
//	LOGD("copy map");

//	*oldMapAddr = newAddr;
//	LOGD("reloc address");
//	*lenghtvalue = 8 * 1024 * 1024;
//	LOGD("maplength value %d", *lenghtvalue);
//	int fd;
//	int *buf;
//	char *logbuf;
//
//	fd = open("/dev/mem", O_RDWR);
//	if (fd < 0) {
//		printf("open /dev/mem failed\n");
//		return 0;
//	}
//
//	logbuf = (char *) mmap(starPositon + 4 * 3, 2048, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
//	if (buf < 0) {
//		printf("mmap /dev/mem failed\n");
//		return 0;
//	}
//	*buf = 16 * 1024 * 1024;
//	LOGD("maplength value %d", *lenghtvalue);
//	memcpy(buf, logbuf, 4);
//	munmap(starPositon + 4 * 3, 2048);
//	close(fd);

//	LOGD("maplength value %d", *lenghtvalue);

//
//	int* linerpos = (int)&vm+6*4;
//
//	int* lenghtvalue = (int)* linerpos+4*3;
//
//	LOGD("maplength value %d",*lenghtvalue);

//
//	stu.name = (char*)malloc(sizeof(char));
//	strcpy(stu.name,"test12121121");
//	stu.score = 99;
//	mTeam = (teather*)malloc(sizeof(teather));
//	(*mTeam).time = 4;
//	(*mTeam).point = 45;
//	stu.currenteam = mTeam;
//
//	LOGD("stu malloc %X",&stu);
//	LOGD("&name malloc %X",&stu.name);
//	LOGD("&name malloc %X",&stu.score);
//	LOGD("&name malloc %X",&stu.currenteam);
//	int* pos = (int)&stu+2*4;
//	LOGD("stu malloc %X",&stu);
//	int* value = (int)* pos+4;
//	LOGD("pos malloc %X",pos);
//	LOGD("pos malloc %X",*pos);
//	LOGD("value malloc %d",*value);
//	*value = 126;
//	LOGD("value malloc %d",stu.currenteam->point);
//	LOGD("name malloc %s",stu.name);
//	LOGD("&score malloc %X",&stu.score);

//	if (!registerNatives(env)) {//注册
//		return -1;
//	}
	/* success -- return valid version number */
	result = JNI_VERSION_1_4;

	return result;
}
