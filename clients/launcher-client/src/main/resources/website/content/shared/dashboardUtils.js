dashboardUtils = {

    currentTime: function()
    {
        return new Date().getTime();
    },

    queryParams: function()
    {
        var params = new Map();
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

                params.set(k, v);
            }
        }

        return params;
    }

};
