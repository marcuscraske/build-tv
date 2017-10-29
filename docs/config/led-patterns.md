# LED Patterns
This configuration is used by the notification daemon to show notifications and turn the screen on/off.

Each piece of configuration should be defined as a JSON object inside the following:

````
{
    "intervals" :
    [
        <place here>
    ]
}

````

An example interval JSON object with a notification:

````
{
    "name"          : "Standup Indicator",
    "priority"      : 100,
    "startHour"     : 9,
    "startMinute"   : 45,
    "endHour"       : 10,
    "endMinute"     : 0,
    "pattern"       : "standup",
    "screenOff"     : false,

    "notification"  :
    {
        "header"        : "Standup",
        "text"          : null,
        "type"          : "standup",
        "priority"      : 50,
        "lifespan"      : 0
    }
}

````

- The `notification` object is not mandatory.
- `screenOff` - defines whether the screen should be turned on or off at that point in time.
- `pattern` - when your build TV has an LED strip, this will set the pattern used. Available patterns:
    - `standup` - flashing strobing pink pattern.
    - `build-ok` - solid green light
    - `build-unstable` - slow pulsing yellow light
    - `build-failure` - marquee red light
    - `build-progress` - marquee blue light
    - `jenkins-unavailable` - flashing red light
    - `startup` - gradual fade-in white light
    - `shutdown` - gradual fade-out white light
    - `rainbow` - beautiful rainbow effect
    - 'test' - test sequences for checking the health of LED pixels (different colours, speeds, etc)

### Full Examples

````
{
    "intervals" :
    [
        {
            "name"          : "Standup Indicator",
            "priority"      : 100,
            "startHour"     : 9,
            "startMinute"   : 45,
            "endHour"       : 10,
            "endMinute"     : 0,
            "pattern"       : "standup",
            "screenOff"     : false,

            "notification"  :
            {
                "header"        : "Standup",
                "text"          : null,
                "type"          : "standup",
                "priority"      : 50,
                "lifespan"      : 0
            }
        },
        {
            "name"          : "Build Indicator Off",
            "priority"      : 200,
            "startHour"     : 20,
            "startMinute"   : 0,
            "endHour"       : 6,
            "endMinute"     : 30,
            "pattern"       : "shutdown",
            "screenOff"     : true
        }
    ]
}
````