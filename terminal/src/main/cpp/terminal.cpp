
#include <jni.h>
#include <string>

#include <cstdlib>
#include <fcntl.h>
#include <android/log.h>
#include <unistd.h>
#include <asm-generic/ioctls.h>
#include <pty.h>
#include <sys/wait.h>
#include <thread> // make sure this is included

int master_fd = 0;


// Global reference to JavaVM
JavaVM *gJvm = nullptr;

// Store a global reference to the Kotlin object (TerminalBridge instance)
jobject gTerminalBridgeObj = nullptr;

// Called once during JNI_OnLoad
jint JNI_OnLoad(JavaVM *vm, void *) {
    gJvm = vm;
    return JNI_VERSION_1_6;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_terminal_pty_PseudoTerminal_startPty(JNIEnv *env, jobject thiz) {

    gTerminalBridgeObj = env->NewGlobalRef(thiz);

    //first, create a pty descriptor and get the master fd
    master_fd = posix_openpt(O_RDWR);
    if (master_fd == -1) {
        __android_log_print(ANDROID_LOG_DEBUG, "master_fd", "FAILED");
    }
    __android_log_print(ANDROID_LOG_DEBUG, "master_fd", "SUCCESS");

    // grant, unlock pty
    if (grantpt(master_fd) < 0 || unlockpt(master_fd) < 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "grant+unlock", "FAILED");
    }
    __android_log_print(ANDROID_LOG_DEBUG, "grant+unlock", "SUCCESS");

    //get slave name using the master fd
    char *slave_name = ptsname(master_fd);
    __android_log_print(ANDROID_LOG_DEBUG, "SLAVE_NAME", "%s", slave_name);

    // fork to create parent and child process, get Process ID (pid)
    int pid = fork();

    // Check if pid is child or parent process. for value = 0, the process is child. if -1, then it is parent. otherwise, it is parent process
    if (pid == 0) {
        close(master_fd);

        // Create new session and set controlling terminal to slave
        if (setsid() < 0) {
            exit(1);
        }

        int slave_fd = open(slave_name, O_RDWR);
        if (slave_fd < 0) {
            exit(1);
        }

        // Set slave_fd as stdin, stdout, stderr for the child
        dup2(slave_fd, STDIN_FILENO);
        dup2(slave_fd, STDOUT_FILENO);
        dup2(slave_fd, STDERR_FILENO);

        if (slave_fd > STDERR_FILENO) {
            close(slave_fd);
        }

        // Now the child is fully attached to the slave PTY
        // Do something or exec a shell/command here
        execlp("/system/bin/sh", "sh", nullptr); // interactive shell


        //sleep(5); // keep child alive to observe logs

    } else if (pid < 0) {
        // Error occurred
        __android_log_print(ANDROID_LOG_DEBUG, "pid", "ERROR");
        exit(1);
    } else {
        // This is Parent process
        __android_log_print(ANDROID_LOG_DEBUG, "pid", "PARENT %d", pid);

        std::thread reader([]() {
            JNIEnv *threadEnv;
            if (gJvm->AttachCurrentThread(&threadEnv, nullptr) != 0) return;

            jclass cls = threadEnv->GetObjectClass(gTerminalBridgeObj);
            jmethodID onOutputMethod = threadEnv->GetMethodID(cls, "onOutputFromNative", "([B)V");

            char buffer[1024];
            while (true) {
                ssize_t bytesRead = read(master_fd, buffer, sizeof(buffer));
                if (bytesRead > 0) {
                    jbyteArray byteArray = threadEnv->NewByteArray(bytesRead);
                    threadEnv->SetByteArrayRegion(byteArray, 0, bytesRead,
                                                  reinterpret_cast<jbyte *>(buffer));

                    threadEnv->CallVoidMethod(gTerminalBridgeObj, onOutputMethod, byteArray);
                    threadEnv->DeleteLocalRef(byteArray);
                } else if (bytesRead == 0 || errno == EIO || errno == EBADF) {
                    break;
                }
            }

            gJvm->DetachCurrentThread();
        });
        reader.detach();
    }

}
extern "C"
JNIEXPORT void JNICALL
Java_com_terminal_pty_PseudoTerminal_send(JNIEnv *env, jobject ignored, jcharArray input,
                                          jboolean interactive) {
    // Get length of Java char array
    jsize length = env->GetArrayLength(input);

    // Access elements (UTF-16 from Java's jchar)
    jchar *c_input = env->GetCharArrayElements(input, nullptr);

    // Convert jchar[] (UTF-16) to std::string (UTF-8 assumed)
    std::string full_command;
    full_command.reserve(length + 1);

    for (int i = 0; i < length; i++) {
        full_command.push_back(static_cast<char>(c_input[i]));
        // ⚠️ Note: This only works for ASCII characters.
        // If you want proper UTF-8, you need a conversion step.
    }
    /*bool is_interactive = interactive;
    if (is_interactive) {
        full_command.push_back('\n'); // simulate Enter*/
        // }


        // Write to PTY master fd
        write(master_fd, full_command.c_str(), full_command.length());

        // Release memory
        env->ReleaseCharArrayElements(input, c_input, 0);
    }

extern "C"
JNIEXPORT void JNICALL
Java_com_terminal_pty_PseudoTerminal_simulateNewLine(JNIEnv *env, jobject ignored) {
    std::string full_command = "\n"; // Simulate Enter key press
    write(master_fd,full_command.c_str(), full_command.length());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_terminal_pty_PseudoTerminal_updateWinSize(JNIEnv *ignored, jobject ignore, jint row,
                                                   jint col) {
    struct winsize ws{};
    ws.ws_row = row;  // Or get from Java/Kotlin
    ws.ws_col = col;  // Or get from Java/Kotlin
    ws.ws_xpixel = 1080;
    ws.ws_ypixel = 2460;

    if (ioctl(master_fd, TIOCSWINSZ, &ws) == -1) {
        __android_log_print(ANDROID_LOG_ERROR, "PTY", "Failed to set window size: %s",
                            strerror(errno));
    } else {
        __android_log_print(ANDROID_LOG_DEBUG, "PTY", "Window size set to %dx%d", ws.ws_col,
                            ws.ws_row);
    }
}


// Define master_fd as global or static to access it here

