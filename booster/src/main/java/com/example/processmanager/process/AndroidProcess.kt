package com.example.processmanager.process

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import java.io.IOException
import java.util.*

open class AndroidProcess(val pId: Int) : Parcelable {
    var name: String = getProcessName()

    constructor(parcel: Parcel) : this(parcel.readInt()) {
        name = parcel.readString() ?: ""
    }

    private fun getProcessName(): String {
        var cmdLine = ""
        try {
            cmdLine = ProcFile.readFile(String.format("/proc/%d/cmdline", pId)).trim()
            if (cmdLine.isEmpty()) {
                return Stat.get(pId).getComm() ?: ""
            }
        } catch (e: Exception) {
        }
        return cmdLine
    }

    /**
     * Read the contents of a file in /proc/[pid]/[filename].
     *
     * @param filename the relative path to the file.
     * @return the contents of the file.
     * @throws IOException if the file does not exist or we don't have read permissions.
     */
    @Throws(IOException::class)
    open fun read(filename: String): String {
        return ProcFile.readFile(String.format(Locale.ENGLISH, "/proc/%d/%s", pId, filename))
    }

    /**
     *
     * /proc/[pid]/attr/current (since Linux 2.6.0)
     *
     *
     * The contents of this file represent the current security attributes of the process.
     *
     *
     * In SELinux, this file is used to get the security context of a process. Prior to Linux
     * 2.6.11, this file could not be used to set the security context (a write was always denied),
     * since SELinux limited process security transitions to execve(2) (see the description of
     * /proc/[pid]/attr/exec, below).  ince Linux 2.6.11, SELinux lifted this restriction and began
     * supporting "set" operations via writes to this node if authorized by policy, although use of
     * this operation is only suitable for applications that are trusted to maintain any desired
     * separation between the old and new security contexts.  Prior to Linux 2.6.28, SELinux did not
     * allow threads within a multi- threaded process to set their security context via this node as
     * it would yield an inconsistency among the security contexts of the threads sharing the same
     * memory space. Since Linux 2.6.28, SELinux lifted this restriction and began supporting "set"
     * operations for threads within a multithreaded process if the new security context is bounded
     * by the old security context, where the bounded relation is defined in policy and guarantees
     * that the new security context has a subset of the permissions of the old security context.
     * Other security modules may choose to support "set" operations via writes to this node.
     *
     * @return the contents of /proc/[pid]/attr/current
     * @throws IOException if the file does not exist or we don't have read permissions.
     */
    @Throws(IOException::class)
    fun attr_current(): String {
        return read("attr/current")
    }

    /**
     *
     * /proc/[pid]/cmdline
     *
     *
     * This read-only file holds the complete command line for the process, unless the process is
     * a zombie. In the latter case, there is nothing in this file: that is, a read on this file will
     * return 0 characters. The command-line arguments appear in this file as a set of strings
     * separated by null bytes ('\0'), with a further null byte after the last string.
     *
     * @return the name of the process. (note: process name may be empty. In case it is empty get
     * the process name from /proc/[pid]/stat).
     * @throws IOException if the file does not exist or we don't have read permissions.
     * @see .name
     */
    @Throws(IOException::class)
    open fun cmdline(): String {
        return read("cmdline")
    }

    /**
     *
     * /proc/[pid]/cgroup (since Linux 2.6.24)
     *
     *
     * This file describes control groups to which the process/task belongs. For each cgroup
     * hierarchy there is one entry containing colon-separated fields of the form:
     *
     *
     * 5:cpuacct,cpu,cpuset:/daemons
     *
     *
     * The colon-separated fields are, from left to right:
     *
     *
     *  1. hierarchy ID number
     *  1. set of subsystems bound to the hierarchy
     *  1. control group in the hierarchy to which the process belongs
     *
     *
     *
     * This file is present only if the CONFIG_CGROUPS kernel configuration option is enabled.
     */
    @Throws(IOException::class)
    open fun cgroup(): Cgroup {
        return Cgroup.get(pId)
    }


    /**
     *
     * /proc/[pid]/oom_score (since Linux 2.6.11)
     *
     *
     * This file displays the current score that the kernel gives to this process for the
     * purpose of selecting a process for the OOM-killer. A higher score means that the
     * process is more likely to be selected by the OOM-killer.
     *
     *
     * The basis for this score is the amount of memory used by the process, with
     * increases (+) or decreases (-) for factors including:
     *
     *
     *  * whether the process creates a lot of children using fork(2)(+);
     *  * whether the process has been running a long time, or has used a lot of CPU time (-);
     *  * whether the process has a low nice value (i.e., &gt; 0) (+);
     *  * whether the process is privileged (-); and
     *  * whether the process is making direct hardware access (-).
     *
     *
     *
     * The oom_score also reflects the adjustment specified by the oom_score_adj
     * or oom_adj setting for the process.
     *
     * @return the oom_score value for this process
     * @throws IOException if the file does not exist or we don't have read permissions.
     */
    @Throws(IOException::class)
    open fun oom_score(): Int {
        return read("oom_score").toInt()
    }

    /**
     *
     * /proc/[pid]/oom_adj (since Linux 2.6.11)
     *
     *
     * This file can be used to adjust the score used to select which process should be killed in
     * an out-of-memory (OOM) situation. The kernel uses this value for a bit-shift operation of the
     * process's oom_score value: valid values are in the* range -16 to +15, plus the special value
     * -17, which disables OOM-killing altogether for this process. A positive score increases the
     * likelihood of this process being killed by the OOM-killer; a negative score decreases the
     * likelihood.
     *
     *
     * The default value for this file is 0; a new process inherits its parent's oom_adj setting.
     * A process must be privileged (CAP_SYS_RESOURCE) to update this file.
     *
     *
     * Since Linux 2.6.36, use of this file is deprecated in favor of
     * /proc/[pid]/oom_score_adj.
     *
     * @return the oom_adj value for this process
     * @throws IOException if the file does not exist or we don't have read permissions.
     */
    @Throws(IOException::class)
    open fun oom_adj(): Int {
        return read("oom_adj").toInt()
    }

    /**
     *
     * /proc/[pid]/oom_score_adj (since Linux 2.6.36)
     *
     *
     * This file can be used to adjust the badness heuristic used to select which process gets
     * killed in out-of-memory conditions.
     *
     *
     * The badness heuristic assigns a value to each candidate task ranging from 0 (never kill) to
     * 1000 (always kill) to determine which process is targeted. The units are roughly a proportion
     * along that range of allowed memory the process may allocate from, based on an estimation of
     * its current memory and swap use. For example, if a task is using all allowed memory, its
     * badness score will be 1000.  If it is using half of its allowed memory, its score will be
     * 500.
     *
     *
     * There is an additional factor included in the badness score: root processes are given 3%
     * extra memory over other tasks.
     *
     *
     * The amount of "allowed" memory depends on the context in which the OOM-killer was called.
     * If it is due to the memory assigned to the allocating task's cpuset being exhausted, the
     * allowed memory represents the set of mems assigned to that cpuset (see cpuset(7)). If it is
     * due to a mempolicy's node(s) being exhausted, the allowed memory represents the set of
     * mempolicy nodes. If it is due to a memory limit (or swap limit) being reached, the allowed
     * memory is that configured limit. Finally, if it is due to the entire system being out of
     * memory, the allowed memory represents all allocatable resources.
     *
     *
     * The value of oom_score_adj is added to the badness score before it is used to determine
     * which task to kill.  Acceptable values range from -1000 (OOM_SCORE_ADJ_MIN) to +1000
     * (OOM_SCORE_ADJ_MAX). This allows user space to control the preference for OOM-killing, ranging
     * from always preferring a certain task or completely disabling it from OOM killing.  The lowest
     * possible value, -1000, is equivalent to disabling OOM- killing entirely for that task, since
     * it will always report a badness score of 0.
     *
     *
     * Consequently, it is very simple for user space to define the amount of memory to consider
     * for each task.  Setting a oom_score_adj value of +500, for example, is roughly equivalent to
     * allowing the remainder of tasks sharing the same system, cpuset, mempolicy, or memory
     * controller resources to use at least 50% more memory.  A value of -500, on the other hand,
     * would be roughly equivalent to discounting 50% of the task's allowed memory from being
     * considered as scoring against the task.
     *
     *
     * For backward compatibility with previous kernels, /proc/[pid]/oom_adj can still be used to
     * tune the badness score.  Its value is scaled linearly with oom_score_adj.
     *
     *
     * Writing to /proc/[pid]/oom_score_adj or /proc/[pid]/oom_adj will change the other with its
     * scaled value.
     *
     * @return the oom_score_adj value for this process
     * @throws IOException if the file does not exist or we don't have read permissions.
     */
    @Throws(IOException::class)
    open fun oom_score_adj(): Int {
        return read("oom_score_adj").toInt()
    }

    /**
     *
     * /proc/[pid]/stat
     *
     *
     * Status information about the process.  This is used by ps(1). It is defined in the kernel
     * source file fs/proc/array.c.
     *
     *
     * The fields, in order, with their proper scanf(3) format specifiers, are:
     *
     *
     *
     *  1. pid  %d The process ID.
     *
     *  1. comm  %s The filename of the executable, in parentheses. This is visible whether or not
     * the executable is swapped out.
     *
     *  1. state  %c One of the following characters, indicating process state:
     *
     *  * R  Running
     *  * S  Sleeping in an interruptible wait
     *  * D  Waiting in uninterruptible disk sleep
     *  * Z  Zombie
     *  * T  Stopped (on a signal) or (before Linux 2.6.33) trace stopped
     *  * t  Tracing stop (Linux 2.6.33 onward)
     *  * W  Paging (only before Linux 2.6.0)
     *  * X  Dead (from Linux 2.6.0 onward)
     *  * x  Dead (Linux 2.6.33 to 3.13 only)
     *  * K  Wakekill (Linux 2.6.33 to 3.13 only)
     *  * W  Waking (Linux 2.6.33 to 3.13 only)
     *  * P  Parked (Linux 3.9 to 3.13 only)
     *
     *
     *
     *  1. ppid %d The PID of the parent of this process.
     *
     *  1. pgrp %d The process group ID of the process.
     *
     *  1. session %d The session ID of the process.
     *
     *  1. tty_nr %d The controlling terminal of the process.  (The minor device number is contained
     * in the combination of bits 31 to 20 and 7 to 0; the major device number is in bits 15 to 8.)
     *
     *
     *  1. tpgid %d The ID of the foreground process group of the controlling terminal of the
     * process.
     *
     *  1. flags %u The kernel flags word of the process.  For bit meanings, see the PF_* defines in
     * the Linux kernel source file include/linux/sched.h.  Details depend on the kernel version.
     * The format for this field was %lu before Linux 2.6.
     *
     *  1. minflt %lu The number of minor faults the process has made which have not required
     * loading a memory page from disk.
     *
     *  1. cminflt %lu The number of minor faults that the process's waited-for children have
     * made
     *
     *  1. majflt  %lu The number of major faults the process has made which have required loading a
     * memory page from disk.
     *
     *  1. cmajflt  %lu The number of major faults that the process's waited-for children have
     * made
     *
     *  1. utime  %lu Amount of time that this process has been scheduled in user mode, measured in
     * clock ticks (divide by sysconf(_SC_CLK_TCK)).  This includes guest time,   guest_time (time
     * spent running a virtual CPU, see below), so that applications that are not aware of the guest
     * time field do not lose that time from their calculations.
     *
     *  1. stime  %lu Amount of time that this process has been scheduled in kernel mode, measured
     * in clock ticks (divide by sysconf(_SC_CLK_TCK)).
     *
     *  1. cutime  %ld Amount of time that this process's waited-for children have been scheduled in
     * user mode, measured in clock ticks (divide by sysconf(_SC_CLK_TCK)). (See also times(2).)
     * This includes guest time, cguest_time (time spent running a virtual CPU, see below).
     *
     *  1. cstime  %ld Amount of time that this process's waited-for children have been scheduled in
     * kernel mode, measured in clock ticks (divide by sysconf(_SC_CLK_TCK)).
     *
     *  1. priority  %ld (Explanation for Linux 2.6) For processes running a real-time scheduling
     * policy (policy below; see sched_setscheduler(2)), this is the negated scheduling priority,
     * minus one; that is, a number in the range -2 to -100, corresponding to real-time priorities 1
     * to 99.  For processes running under a non-real-time scheduling policy, this is the raw nice
     * value (setpriority(2)) as represented in the kernel.  The kernel stores nice values as numbers
     * in the range 0 (high) to 39 (low), corresponding to the user-visible nice range of -20 to 19.
     * Before Linux 2.6, this was a scaled value based on the scheduler weighting given to this
     * process
     *
     *  1. nice  %ld The nice value (see setpriority(2)), a value in the range 19 (low priority) to
     * -20 (high priority).
     *
     *  1. num_threads  %ld Number of threads in this process (since Linux 2.6). Before kernel 2.6,
     * this field was hard coded to 0 as a placeholder for an earlier removed field.
     *
     *  1. itrealvalue  %ld The time in jiffies before the next SIGALRM is sent to the process due
     * to an interval timer.  Since kernel 2.6.17, this field is no longer maintained, and is hard
     * coded as 0.
     *
     *  1. starttime  %llu The time the process started after system boot.  In kernels before Linux
     * 2.6, this value was expressed in jiffies.  Since Linux 2.6, the value is expressed in clock
     * ticks (divide by sysconf(_SC_CLK_TCK)).
     *
     *  1. The format for this field was %lu before Linux 2.6.  (23) vsize  %lu Virtual memory size
     * in bytes.
     *
     *  1. rss  %ld Resident Set Size: number of pages the process has in real memory.  This is just
     * the pages which count toward text, data, or stack space.  This does not include pages which
     * have not been demand-loaded in, or which are swapped out.
     *
     *  1. rsslim  %lu Current soft limit in bytes on the rss of the process; see the description of
     * RLIMIT_RSS in getrlimit(2).
     *
     *  1. startcode  %lu The address above which program text can run.
     *
     *  1. endcode  %lu The address below which program text can run.
     *
     *  1. startstack  %lu The address of the startForceKill (i.e., bottom) of the stack.
     *
     *  1. kstkesp  %lu The current value of ESP (stack pointer), as found in the kernel stack page
     * for the process.
     *
     *  1. kstkeip  %lu The current EIP (instruction pointer).
     *
     *  1. signal  %lu The bitmap of pending signals, displayed as a decimal number.  Obsolete,
     * because it does not provide information on real-time signals; use /proc/[pid]/status
     * instead
     *
     *  1. blocked  %lu The bitmap of blocked signals, displayed as a decimal number.  Obsolete,
     * because it does not provide information on real-time signals; use /proc/[pid]/status
     * instead
     *
     *  1. sigignore  %lu The bitmap of ignored signals, displayed as a decimal number. Obsolete,
     * because it does not provide information on real-time signals; use /proc/[pid]/status
     * instead
     *
     *  1. sigcatch  %lu The bitmap of caught signals, displayed as a decimal number. Obsolete,
     * because it does not provide information on real-time signals; use /proc/[pid]/status
     * instead.
     *
     *  1. wchan  %lu This is the "channel" in which the process is waiting.  It is the address of a
     * location in the kernel where the process is sleeping.  The corresponding symbolic name can be
     * found in /proc/[pid]/wchan.
     *
     *  1. nswap  %lu Number of pages swapped (not maintained).
     *
     *  1. cnswap  %lu Cumulative nswap for child processes (not maintained).
     *
     *  1. exit_signal  %d  (since Linux 2.1.22) Signal to be sent to parent when we die.
     *
     *  1. processor  %d  (since Linux 2.2.8) CPU number last executed on.
     *
     *  1. rt_priority  %u  (since Linux 2.5.19) Real-time scheduling priority, a number in the
     * range 1 to 99 for processes scheduled under a real-time policy, or 0, for non-real-time
     * processes (see sched_setscheduler(2)).
     *
     *  1. policy  %u  (since Linux 2.5.19) Scheduling policy (see sched_setscheduler(2)). Decode
     * using the SCHED_* constants in linux/sched.h.  The format for this field was %lu before Linux
     * 2.6.22.
     *
     *  1. delayacct_blkio_ticks  %llu  (since Linux 2.6.18) Aggregated block I/O delays, measured
     * in clock ticks (centiseconds).
     *
     *  1. guest_time  %lu  (since Linux 2.6.24) Guest time of the process (time spent running a
     * virtual CPU for a guest operating system), measured in clock ticks (divide by
     * sysconf(_SC_CLK_TCK)).
     *
     *  1. cguest_time  %ld  (since Linux 2.6.24) Guest time of the process's children, measured in
     * clock ticks (divide by sysconf(_SC_CLK_TCK)).
     *
     *  1. start_data  %lu  (since Linux 3.3) Address above which program initialized and
     * uninitialized (BSS) data are placed.
     *
     *  1. end_data  %lu  (since Linux 3.3) Address below which program initialized and
     * uninitialized (BSS) data are placed.
     *
     *  1. start_brk  %lu  (since Linux 3.3) Address above which program heap can be expanded with
     * brk(2).
     *
     *  1. arg_start  %lu  (since Linux 3.5) Address above which program command-line arguments
     * (argv) are placed.
     *
     *  1. arg_end  %lu  (since Linux 3.5) Address below program command-line arguments (argv) are
     * placed.
     *
     *  1. env_start  %lu  (since Linux 3.5) Address above which program environment is placed.
     *
     *  1. env_end  %lu  (since Linux 3.5) Address below which program environment is placed.
     *
     *  1. exit_code  %d  (since Linux 3.5) The thread's exit status in the form reported by
     * waitpid(2).
     *
     *
     *
     *
     * if the file does not exist or we don't have read permissions.
     */
    @Throws(IOException::class)
    open fun stat(): Stat? {
        return Stat.get(pId)
    }

    /**
     *
     * Provides information about memory usage, measured in pages.
     *
     *
     * The columns are:
     *
     *
     *  * size       (1) total program size (same as VmSize in /proc/[pid]/status)
     *  * resident   (2) resident set size (same as VmRSS in /proc/[pid]/status)
     *  * share      (3) shared pages (i.e., backed by a file)
     *  * text       (4) text (code)
     *  * lib        (5) library (unused in Linux 2.6)
     *  * data       (6) data + stack
     *  * dt         (7) dirty pages (unused in Linux 2.6)
     *
     *
     *
     * if the file does not exist or we don't have read permissions.
     */
    @Throws(IOException::class)
    open fun statm(): Statm {
        return Statm.get(pId)
    }

    /**
     *
     * /proc/[pid]/status
     *
     *
     * Provides much of the information in /proc/[pid]/stat and /proc/[pid]/statm in a format
     * that's
     * easier for humans to parse.
     *
     *
     * Here's an example:
     *
     * <pre>
     * $ cat /proc/$$/status
     * Name:   bash
     * State:  S (sleeping)
     * Tgid:   3515
     * Pid:    3515
     * PPid:   3452
     * TracerPid:      0
     * Uid:    1000    1000    1000    1000
     * Gid:    100     100     100     100
     * FDSize: 256
     * Groups: 16 33 100
     * VmPeak:     9136 kB
     * VmSize:     7896 kB
     * VmLck:         0 kB
     * VmPin:         0 kB
     * VmHWM:      7572 kB
     * VmRSS:      6316 kB
     * VmData:     5224 kB
     * VmStk:        88 kB
     * VmExe:       572 kB
     * VmLib:      1708 kB
     * VmPMD:         4 kB
     * VmPTE:        20 kB
     * VmSwap:        0 kB
     * Threads:        1
     * SigQ:   0/3067
     * SigPnd: 0000000000000000
     * ShdPnd: 0000000000000000
     * SigBlk: 0000000000010000
     * SigIgn: 0000000000384004
     * SigCgt: 000000004b813efb
     * CapInh: 0000000000000000
     * CapPrm: 0000000000000000
     * CapEff: 0000000000000000
     * CapBnd: ffffffffffffffff
     * Seccomp:        0
     * Cpus_allowed:   00000001
     * Cpus_allowed_list:      0
     * Mems_allowed:   1
     * Mems_allowed_list:      0
     * voluntary_ctxt_switches:        150
     * nonvoluntary_ctxt_switches:     545
    </pre> *
     *
     *
     * The fields are as follows:
     *
     *
     *  1. Name: Command run by this process.
     *  1. State: Current state of the process.  One of "R (running)", "S (sleeping)", "D (disk
     * sleep)",
     * "T (stopped)", "T (tracing stop)", "Z (zombie)", or "X (dead)".
     *  1. Tgid: Thread group ID (i.e., Process ID).
     *  1. Pid: Thread ID (see gettid(2)).
     *  1. PPid: PID of parent process.
     *  1. TracerPid: PID of process tracing this process (0 if not being traced).
     *  1. Uid, Gid: Real, effective, saved set, and filesystem UIDs (GIDs).
     *  1. FDSize: Number of file descriptor slots currently allocated.
     *  1. Groups: Supplementary group list.
     *  1. VmPeak: Peak virtual memory size.
     *  1. VmSize: Virtual memory size.
     *  1. VmLck: Locked memory size (see mlock(3)).
     *  1. VmPin: Pinned memory size (since Linux 3.2).  These are pages that can't be moved because
     * something needs to directly access physical memory.
     *  1. VmHWM: Peak resident set size ("high water mark").
     *  1. VmRSS: Resident set size.
     *  1. VmData, VmStk, VmExe: Size of data, stack, and text segments.
     *  1. VmLib: Shared library code size.
     *  1. VmPTE: Page table entries size (since Linux 2.6.10).
     *  1. VmPMD: Size of second-level page tables (since Linux 4.0).
     *  1. VmSwap: Swapped-out virtual memory size by anonymous private pages; shmem swap usage is
     * not
     * included (since Linux 2.6.34).
     *  1. Threads: Number of threads in process containing this thread.
     *  1. SigQ: This field contains two slash-separated numbers that relate to queued signals for
     * the
     * real user ID of this process.  The first of these is the number of currently queued signals
     * for
     * this real user ID, and the second is the resource limit on the number of queued signals for
     * this
     * process (see the description of RLIMIT_SIGPENDING in getrlimit(2)).
     *  1. SigPnd, ShdPnd: Number of signals pending for thread and for process as a whole (see
     * pthreads(7) and signal(7)).
     *  1. SigBlk, SigIgn, SigCgt: Masks indicating signals being blocked, ignored, and caught (see
     * signal(7)).
     *  1. CapInh, CapPrm, CapEff: Masks of capabilities enabled in inheritable, permitted, and
     * effective sets (see capabilities(7)).
     *  1. CapBnd: Capability Bounding set (since Linux 2.6.26, see capabilities(7)).
     *  1. Seccomp: Seccomp mode of the process (since Linux 3.8, see seccomp(2)). 0 means
     * SECCOMP_MODE_DISABLED; 1 means SECCOMP_MODE_STRICT; 2 means SECCOMP_MODE_FILTER. This field is
     * provided only if the kernel was built with the CONFIG_SECCOMP kernel configuration option
     * enabled.
     *  1. Cpus_allowed: Mask of CPUs on which this process may run (since Linux 2.6.24, see
     * cpuset(7)).
     *  1. Cpus_allowed_list: Same as previous, but in "list format" (since Linux 2.6.26, see
     * cpuset(7)).
     *  1. Mems_allowed: Mask of memory nodes allowed to this process (since Linux 2.6.24, see
     * cpuset(7)).
     *  1. Mems_allowed_list: Same as previous, but in "list format" (since Linux 2.6.26, see
     * cpuset(7)).
     * voluntary_ctxt_switches, nonvoluntary_ctxt_switches: Number of voluntary and involuntary
     * context
     * switches (since Linux 2.6.23).
     *
     *
     *
     * if the file does not exist or we don't have read permissions.
     */
    @Throws(IOException::class)
    open fun status(): Status? {
        return Status.get(pId)
    }

    /**
     * The symbolic name corresponding to the location in the kernel where the process is sleeping.
     *
     * @return the contents of /proc/[pid]/wchan
     * @throws IOException if the file does not exist or we don't have read permissions.
     */
    @Throws(IOException::class)
    open fun wchan(): String? {
        return read("wchan")
    }



    override fun writeToParcel(dest: Parcel, p1: Int) {
        dest.writeInt(pId)
        dest.writeString(name)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AndroidProcess> {
        override fun createFromParcel(parcel: Parcel): AndroidProcess {
            return AndroidProcess(parcel)
        }

        override fun newArray(size: Int): Array<AndroidProcess?> {
            return arrayOfNulls(size)
        }
    }
}