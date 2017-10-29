// Hook onload event so that the page can load...
$(window).ready(function(){
    sharePriceController.setup();
});

/*
    Used to render a share price ticker dashboard.
*/
sharePriceController = {

    /* The current share price symbol being displayed. */
    sharePriceSymbol: null,

    /* The API to use for querying the share price. */
    sharePriceApiUrl: null,

    /* The rate at which to poll for changes. */
    pollRate: 30000,

    /* The old data; we avoid changes when new data is the same. */
    pollOldData: null,

    setup: function()
    {
        // Fetch symbol from URL params
        this.sharePriceSymbol = dashboardUtils.queryParam("symbol");

        if (this.sharePriceSymbol == null || this.sharePriceSymbol.length == 0)
        {
            this.sharePriceSymbol = "XXX";
            console.error("sharePriceController - no symbol specified; specify ?symbol=<share symbol here> at the end of this URL to set share to watch");
        }

        // Build share price URL
        this.sharePriceApiUrl = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22" + this.sharePriceSymbol + "%22)&format=json&diagnostics=false&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";

        // Output config
        console.debug("sharePriceController - watching symbol: " + this.sharePriceSymbol);
        console.debug("sharePriceController - usin URL: " + this.sharePriceApiUrl);

        // Update title
        $("#share-symbol").text(this.sharePriceSymbol);

        // Setup polling
        this.setupPoll();

        // Start raining money
        this.rainMoney();
    },

    setupPoll: function()
    {
        setInterval(this.poll, this.pollRate);
        console.debug("sharePriceController - polling setup - interval: " + this.pollRate);

        // Poll immediately...
        this.poll();
    },

    poll: function()
    {
        try
        {
            console.debug("sharePriceController - polling...");

            $.getJSON(sharePriceController.sharePriceApiUrl, function(data) {

                sharePriceController.pollHandle(data);
                console.debug("sharePriceController - updated");

            }).fail(function() {
                console.error("sharePriceController - failed to retrieve share price data");
            });
        }
        catch (e)
        {
            console.error("sharePriceController - failed to poll: " + e);
        }
    },

    pollHandle: function(data)
    {
        // Check if data has even changed
        if (this.pollOldData != null && JSON.stringify(data) == JSON.stringify(this.pollOldData))
        {
            console.debug("sharePriceController - polled data unchanged");
            return;
        }

        // Update fields
        var stockData = data["query"]["results"]["quote"];

        this.updateField(stockData, "LastTradePriceOnly", "#field-last-trade");
        this.updateTicker(stockData, "Change", "#field-change", "", "p");
        this.updateTicker(stockData, "ChangeinPercent", "#field-change-percent", "", "");
        this.updateField(stockData, "Bid", "#field-bid");
        this.updateField(stockData, "Ask", "#field-ask");
        this.updateField(stockData, "PreviousClose", "#field-previous-close");
        this.updateTicker(stockData, "ChangeinPercent", "#field-previous-close-change");
        this.updateField(stockData, "Open", "#field-open");

        this.updateField(stockData, "YearLow", "#field-year-low");
        this.updateTicker(stockData, "ChangeFromYearLow", "#field-year-low-change");

        this.updateField(stockData, "YearHigh", "#field-year-high");
        this.updateTicker(stockData, "ChangeFromYearHigh", "#field-year-high-change");

        this.updateField(stockData, "MarketCapitalization", "#field-market-cap", "£");
        this.updateField(stockData, "Volume", "#field-volume");

        // Rain money if open share value is positive
        var currentValue = stockData["LastTradePriceOnly"];
    },

    updateField: function(stockData, item, target, preSymbol, postSymbol)
    {
        var isPreSymbol = (preSymbol != null && preSymbol.length > 0);
        var isPostSymbol = (postSymbol != null && postSymbol.length > 0);

        var value = stockData[item];

        if (value != null)
        {
            var actualValue;

            if (isPreSymbol || isPostSymbol)
            {
                // Append symbols...
                if (preSymbol)
                {
                    actualValue = value.replace(preSymbol, "");

                    if (value.indexOf(preSymbol) == -1)
                    {
                        value = preSymbol + value;
                    }
                }
                else
                {
                    actualValue = value.replace(postSymbol, "");

                    if (value.indexOf(postSymbol) == -1)
                    {
                        value += postSymbol;
                    }
                }
            }
            else
            {
                actualValue = value;
            }

            // Update value
            $(target).text(value);

            return actualValue;
        }
        else
        {
            console.error("sharePriceController - unable to find JSON item - item: " + item)
        }
    },

    updateTicker: function(stockData, item, target, preSymbol, postSymbol)
    {
        // Update field
        var value = this.updateField(stockData, item, target, preSymbol != null ? preSymbol : "", postSymbol != null ? postSymbol : "%");

        // Set class based on value being either positive or negative
        if (value >= 0)
        {
            $(target).addClass("ticker-high")
        }
        else
        {
            $(target).addClass("ticker-low")
        }
    },

    rainMoney: function()
    {
        snowfallController.start();

        console.debug("sharePriceController - raining money enabled");
    },

    clearMoneyRain: function()
    {
        snowfallController.stop();
        console.debug("sharePriceController - raining money cleared");
    }

};
