# Rotate between windows
``
xdotool search --all --onlyvisible --name chromium | sed -n '3p' | xargs -I {} xdotool windowactivate {}
``

Where 3 is the line to use. Seems to pickup incorrect windows, needs further investigation, may avoid reopening
windows or alt+tabbing.