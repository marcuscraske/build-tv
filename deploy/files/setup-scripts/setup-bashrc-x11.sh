#!/bin/bash

# Remove startx line, in case it already exists
sed -i '/startx/d' /home/wallboard/.bashrc

# Append startx to end of bashrc file
echo -e "\nstartx" >> /home/wallboard/.bashrc

echo "bashrc setup for startx"
