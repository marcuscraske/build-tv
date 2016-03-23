snowfallController = {

    snowImage: "content/share-price/money.png",
    numberOfFlakes: 30,
    sizeMin: 20,
    sizeMax: 120,
    flagRender: false,

    setup: function(snowImage, numberOfFlakes, sizeMin, sizeMax)
    {
        if (snowImage != null)
        {
            this.snowImage = snowImage;
        }
        if (numberOfFlakes != null)
        {
            this.numberOfFlakes = numberOfFlakes;
        }
        if (sizeMin != null)
        {
            this.sizeMin = sizeMin;
        }
        if (sizeMin != null)
        {
            this.sizeMax = sizeMax;
        }

        console.info("snowfallController - setup params - image: " + this.snowImage + ", num flakes: " + this.numberOfFlakes +
                        ", size min: " + this.sizeMin + ", size max: " + this.sizeMax);

        if (typeof(window.pageYOffset) == "number")
        {
            snow_browser_width = window.innerWidth;
            snow_browser_height = window.innerHeight;
        }
        else if (document.body && (document.body.scrollLeft || document.body.scrollTop))
        {
            snow_browser_width = document.body.offsetWidth;
            snow_browser_height = document.body.offsetHeight;
        }
        else if (document.documentElement && (document.documentElement.scrollLeft || document.documentElement.scrollTop))
        {
            snow_browser_width = document.documentElement.offsetWidth;
            snow_browser_height = document.documentElement.offsetHeight;
        }
        else
        {
            snow_browser_width = 500;
            snow_browser_height = 500;
        }

        snow_dx = [];
        snow_xp = [];
        snow_yp = [];
        snow_am = [];
        snow_stx = [];
        snow_sty = [];

        for (i = 0; i < this.numberOfFlakes; i++)
        {
            snow_dx[i] = 0;
            snow_xp[i] = Math.random()*(snow_browser_width-50);
            snow_yp[i] = Math.random()*snow_browser_height;
            snow_am[i] = Math.random()*20;
            snow_stx[i] = 0.02 + Math.random()/10;
            snow_sty[i] = 0.7 + Math.random();

            randomSize = Math.floor(this.sizeMin + (Math.random() * (this.sizeMax - this.sizeMin + 1)));

            if (i > 0)
            {
                $("body").append(
                    "<div id=\"snow_flake" + i + "\" class=\"snow-flake\" style=\"width: " +
                    randomSize + "; height: " + randomSize + "; position:absolute;z-index:" + i + "\"><\img src=\"" +
                    this.snowImage + "\"></div>");
            }
            else
            {
                $("body").append("<div id=\"snow_flake0\" style=\"position:absolute; z-index:0\"><img width=\"" + randomSize + "\" height=\"" + randomSize + "\" src=\"" + this.snowImage + "\" /></div>");
            }
        }
    },

    render: function ()
    {
        if (this.flagRender)
        {
            for (i = 0; i < this.numberOfFlakes; i++)
            {
                snow_yp[i] += snow_sty[i];
                if (snow_yp[i] > snow_browser_height-50)
                {
                    snow_xp[i] = Math.random()*(snow_browser_width-snow_am[i]-30);
                    snow_yp[i] = 0;
                    snow_stx[i] = 0.02 + Math.random()/10;
                    snow_sty[i] = 0.7 + Math.random();
                }
                snow_dx[i] += snow_stx[i];
                document.getElementById("snow_flake"+i).style.top=snow_yp[i]+"px";
                document.getElementById("snow_flake"+i).style.left=snow_xp[i] + snow_am[i]*Math.sin(snow_dx[i])+"px";
            }

            snow_time = setTimeout("snowfallController.render();", 10);
        }
    },

    start: function()
    {
        // Set render flag to true...
        this.flagRender = true;

        // Setup snowflake elements
        this.setup();

        // Start rendering...
        this.render();
    },

    stop: function ()
    {
        // Set render flag to false to stop rendering...
        this.flagRender = false;

        // Remove snowflakes...
        for (i = 0; i < this.numberOfFlakes; i++)
        {
            $(".snow-flake").remove();
        }
    }

};