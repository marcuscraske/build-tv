dashboardUtils = {

    currentTime: function()
    {
        return new Date().getTime();
    },

    queryParam: function (key)
    {
        var urlParams = this.queryParams();
        var value = null;

        for (var i = 0; i < urlParams.length; i++)
        {
            if (urlParams[i][0] == key)
            {
                value = urlParams[i][1];
            }
        }

        return value;
    },

    queryParams: function()
    {
        var params = [];
        var url = document.URL.split("?")[1];

        if (url != null)
        {
            var parts = url.split("&");
            var indexSplit;
            var item, k, v;

            for (var i = 0; i < parts.length; i++)
            {
                item = parts[i];
                indexSplit = item.indexOf("=");

                if (indexSplit != -1 && indexSplit < item.length)
                {
                    k = item.substring(0, indexSplit);
                    v = item.substring(indexSplit + 1);
                }
                else
                {
                    k = item;
                    v = true;
                }

                params.push([k, v]);
            }
        }

        return params;
    }

};
