#!/bin/bash

# Disable text terminal blanking
sed "/BLANK_TIME/d"     -i /etc/kbd/config
sed "/POWERDOWN_TIME/d" -i /etc/kbd/config

echo "BLANK_TIME=0"     >> /etc/kbd/config
echo "POWERDOWN_TIME=0" >> /etc/kbd/config

echo "Monitor blanking disabled"

# Disable X11 blanking
# TODO: no longer exists, may be able to drop this section...
#X11_TARGET="/etc/xdg/lxsession/LXDE/autostart"

#sed -i '/xset/d' ${X11_TARGET}

#echo -e "\n@xset s noblank" >> ${X11_TARGET}
#echo -e "\n@xset s off" >> ${X11_TARGET}
#echo -e "\n@xset -dpms" >> ${X11_TARGET}
