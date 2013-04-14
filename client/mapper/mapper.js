var MAPPER = (function (_) {
    var expose = {};

    expose.packageSizes = function(metricsJSON) {
        var res = new Array();
        
        metrics = JSON.parse(metricsJSON);
        pkgs = metrics['packages'];
        
        for (var key in pkgs) {
            if (pkgs.hasOwnProperty(key)) {
                res.push({name: key, size: _.size(pkgs[key]['classes'])});
            }
        }
        
        return res;
    }
    return expose;
})(_);
