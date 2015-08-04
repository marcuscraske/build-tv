#!/bin/bash
# *********************************************************************************
# Installs an image onto a disk i.e. the Pi's memory card
#
# Author(s):    limpygnome
# Version:      1.0
# *********************************************************************************

# Output available images
IMAGES=$(ls ./images)

# Ask for image to use
echo "Select image to install:"
select IMAGE in $IMAGES;
do
    if [[ $IMAGE ]]; then
        echo ""  
        break
    fi
done

# Output current state and devices/partitions etc
DEVICES=$(lsblk -o NAME,FSTYPE,SIZE,MOUNTPOINT,LABEL)

echo "Available devices:"
echo "$DEVICES"
echo ""

# Ask for device to use
echo "Select disk (do not select partition):"
DEVICES=$(lsblk -nl -o NAME)
select DEVICE in $DEVICES;
do
    if [[ $DEVICE ]]; then
        DEVICE="/dev/$DEVICE"
        echo ""
        break
    fi
done

# Output pending operation/summary
echo "Confirm"
echo "***************************************************************"
echo "The following image:"
echo "      $IMAGE"
echo ""
echo "Will be written to:"
echo "      $DEVICE"
echo ""
echo "ARE YOU ABSOLUTELY SURE? This may wipe a disk..."
echo ""
echo "Continue? (y)"

# Check we want to continue
read CONTINUE

if [[ "$CONTINUE" != "y" ]]; then
    echo "Aborted."
    exit 0
fi

# Write image to device
echo "Writing image to target..."

pv -tpreb "./images/$IMAGE" | dd of="$DEVICE"

echo "Complete!"


