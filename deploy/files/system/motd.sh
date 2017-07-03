#!/bin/bash

PROCCOUNT=`ps -Afl | wc -l`
PROCCOUNT=`expr $PROCCOUNT - 5`
GROUPZ=`groups`

CURR_FREQ=$(echo $(($(cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq)/1000)))


# The pretty header for MOTD
IFS='' read -r -d '' MOTD_HEADER <<'EOF'
 _           _ _     _   _
| |__  _   _(_) | __| | | |___   __
| '_ \| | | | | |/ _` | | __\ \ / /
| |_) | |_| | | | (_| | | |_ \ V /
|_.__/ \__,_|_|_|\__,_|  \__| \_/
EOF

# Print MOTD
echo -e "\033[1;31m
${MOTD_HEADER}

\033[0;37m+++++++++++++++++: \033[1;37mSystem Stats\033[0;37m :+++++++++++++++++++
   \033[0;37mHostname \033[0;37m= \033[1;32m`hostname`
   \033[0;37mDatetime \033[0;37m= \033[1;32m`date +"%A, %e %B %Y, %r"`
\033[0;35m    \033[0;37mAddress \033[0;37m= \033[1;32m`/sbin/ifconfig eth0 | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'`
\033[0;35m     \033[0;37mKernel \033[0;37m= \033[1;32m`uname -r`
\033[0;35m     \033[0;37mUptime \033[0;37m= \033[1;32m`uptime | sed 's/.*up ([^,]*), .*/1/'`
\033[0;35m     \033[0;37mMemory \033[0;37m= \033[1;32m`cat /proc/meminfo | grep MemTotal | awk {'print $2'}` kB
\033[0;35m   \033[0;37mCPU Temp \033[0;37m= \033[1;32m`expr substr "$(cat /sys/class/thermal/thermal_zone0/temp)" 1 2` c
\033[0;35m   \033[0;37mCPU Freq \033[0;37m= \033[1;32m${CURR_FREQ} MHz

\033[0;37m++++++++++++++++++: \033[1;37mUser Stats\033[0;37m :++++++++++++++++++++
   \033[0;37mUsername \033[0;37m= \033[1;32m`whoami`
\033[0;37m   \033[0;37mSessions \033[0;37m= \033[1;32m`who | grep $USER | wc -l` of $ENDSESSION MAX
\033[0;37m  \033[0;37mProcesses \033[0;37m= \033[1;32m$PROCCOUNT of `ulimit -u` MAX

\033[0;37m+++++++++++++++++: \033[1;37mUsers Online\033[0;37m :++++++++++++++++++
\033[1;32m`who`

\033[0;37m+++++++++++++++++: \033[1;37mRecent Sessions\033[0;37m :+++++++++++++++
\033[1;32m`last -5`

\033[0;37m+++++++++++++++++++: \033[1;37mProcesses\033[0;37m :+++++++++++++++++++
\033[1;32m`ps -eo pcpu,pid,user,args | sort -k 1 -r | head -6`

\033[0;37m++++++++++++++++++++: \033[1;37mStorage\033[0;37m :++++++++++++++++++++
\033[1;32m`df -h`

\033[0;37m+++++++++++++++++++++++++++++++++++++++++++++++++++\033[1;32m
"
