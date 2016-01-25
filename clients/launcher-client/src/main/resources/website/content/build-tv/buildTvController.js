// Hook onload event so that the page can load...
$(window).ready(function(){
    buildTvController.setup();
});

// Build TV controller, used to poll build TV service and render current status of Jenkins
buildTvController = {

    setup: function()
    {
    }

};
