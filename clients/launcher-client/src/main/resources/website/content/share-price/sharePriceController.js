// API: http://download.finance.yahoo.com/d/quotes.csv?s=WPG.L&f=snl1c1p2&e=.csv
// https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22WPG.L%22)&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=
// https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22WPG.L%22)&format=json&diagnostics=false&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=


// Hook onload event so that the page can load...
$(window).ready(function(){
    sharePriceController.setup();
});

/*
    todo
*/
sharePriceController = {

    /* The API to use for querying the share price. - replace ABC.D with stoc */
    sharePriceApiUrl: "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%22ABC.D%22)&format=json&diagnostics=false&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=",

    /* The rate at which to poll for changes. */
    pollRate: 30000,

    /* The old data; we avoid changes when new data is the same. */
    pollOldData: null,

    setup: function()
    {
        // Setup polling
        this.setupPoll();
    },

    setupPoll: function()
    {
        console.info("sharePriceController - polling share price...");

        setInterval(function(){
            try
            {
                $.getJSON(sharePriceController.sharePriceApiUrl, function(data) {

                    sharePriceController.pollHandle(data);

                }).fail(function(){
                    console.error("sharePriceController - failed to retrieve share price data");
                });
            }
            catch (e)
            {
                console.error("sharePriceController - failed to poll: " + e);
            }
        }, this.pollRate);
    },

    pollHandle: function(data)
    {
        // Check if data has even changed
        if (this.pollOldData != null && JSON.stringify(data) == JSON.stringify(this.pollOldData))
        {
            console.debug("sharePriceController - polled data unchanged");
            return;
        }



    }

};
