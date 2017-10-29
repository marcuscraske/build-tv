#!/bin/bash

# *********************************************************************************
# Dumps a disk to an image file.
# *********************************************************************************

# Check we're sudo
if [[ "$EUID" != 0 ]]; then
    echo "This script must be ran with sudo"
    exit 1
fi

# Output current state and devices/partitions etc
DEVICES=$(lsblk -o NAME,FSTYPE,SIZE,MOUNTPOINT,LABEL)

echo "Available devices:"
echo "$DEVICES"
echo ""

# Ask for device to use
echo "Select disk (do not select partition) to dump:"
DEVICES=$(lsblk -nl -o NAME)
select DEVICE in $DEVICES;
do
    if [[ $DEVICE ]]; then
        DEVICE="/dev/$DEVICE"
        echo ""
        break
    fi
done

# Ask for memorable description
echo "Enter a memorable name for this image (avoid spaces):"
read IMAGE

DATESTR=$(date +"%Y-%m-%d_%H-%M-%S")
IMAGE="${DATESTR}_${IMAGE}.img"

# Output pending operation/summary
echo "Confirm"
echo "***************************************************************"
echo "The following device:"
echo "      $DEVICE"
echo ""
echo "Will be dumped to the image:"
echo "      $IMAGE"
echo ""
echo "Continue? (y)"

# Check we want to continue
read CONTINUE

if [[ "$CONTINUE" != "y" ]]; then
    echo "Aborted."
    exit 0
fi

# Write image to device
echo "Dumping disk to file..."

pv -tpreb "$DEVICE" | dd of="./images/$IMAGE"

echo "Complete!"


