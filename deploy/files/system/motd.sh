#!/bin/bash

# The pretty header for MOTD
IFS='' read -r -d '' MOTD_HEADER <<'EOF'
|  \                |  \|  \      |  \                                        |  \  |  \
| $$____   __    __  \$$| $$  ____| $$       ______ ____    ______   _______   \$$ _| $$_     ______    ______
| $$    \ |  \  |  \|  \| $$ /      $$      |      \    \  /      \ |       \ |  \|   $$ \   /      \  /      \
| $$$$$$$\| $$  | $$| $$| $$|  $$$$$$$      | $$$$$$\$$$$\|  $$$$$$\| $$$$$$$\| $$ \$$$$$$  |  $$$$$$\|  $$$$$$\
| $$  | $$| $$  | $$| $$| $$| $$  | $$      | $$ | $$ | $$| $$  | $$| $$  | $$| $$  | $$ __ | $$  | $$| $$   \$$
| $$__/ $$| $$__/ $$| $$| $$| $$__| $$      | $$ | $$ | $$| $$__/ $$| $$  | $$| $$  | $$|  \| $$__/ $$| $$
| $$    $$ \$$    $$| $$| $$ \$$    $$      | $$ | $$ | $$ \$$    $$| $$  | $$| $$   \$$  $$ \$$    $$| $$
 \$$$$$$$   \$$$$$$  \$$ \$$  \$$$$$$$       \$$  \$$  \$$  \$$$$$$  \$$   \$$ \$$    \$$$$   \$$$$$$  \$$
EOF


let upSeconds="$(/usr/bin/cut -d. -f1 /proc/uptime)"
let secs=$((${upSeconds}%60))
let mins=$((${upSeconds}/60%60))
let hours=$((${upSeconds}/3600%24))
let days=$((${upSeconds}/86400))
UPTIME=`printf "%d days, %02dh%02dm%02ds" "$days" "$hours" "$mins" "$secs"`
TEMP=$(echo $(($(cat /sys/class/thermal/thermal_zone0/temp) / 1000)))
RECENT_LOGON=$(last -n 5)
CURR_FREQ=$(echo $(($(cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq)/1000)))
HOSTNAME=$(cat /etc/hostname)

# get the load averages
read one five fifteen rest < /proc/loadavg

echo "$(tput bold ; tput setaf 1)"
echo "${MOTD_HEADER}"

echo "$(tput setaf 2)
${HOSTNAME}
`uname -srmo`

$(tput setaf 3)
${RECENT_LOGON}

$(tput setaf 2)
Date               : `date +"%A, %e %B %Y, %r"`
Uptime             : ${UPTIME}
Temperature        : ${TEMP} c
Current CPU Freq   : ${CURR_FREQ} MHz
Memory             : `cat /proc/meminfo | grep MemFree | awk {'print $2'}`kB (Free) / `cat /proc/meminfo | grep MemTotal | awk {'print $2'}`kB (Total)
Load Averages      : ${one}, ${five}, ${fifteen} (1, 5, 15 min)
Running Processes  : `ps ax | wc -l | tr -d " "`
IP Addresses       : `/sbin/ifconfig eth0 | /bin/grep "inet addr" | /usr/bin/cut -d ":" -f 2 | /usr/bin/cut -d " " -f 1` and `wget -q -O - http://icanhazip.com/ | tail`

$(tput sgr0)"
