var MAPPER = (function (_) {
    var expose = {};

    var defaultSide = 100;
    var heightFactorPkg = 100;
    var heightFactorCls = 20;
    var heightFactorMthd = 10;
    var minHeight = 5;
    var sideFactorMthd = 20;
    var sideDistanceMthd = 20;
    var minSide = 10;
    var colorsSet = [0xBB0000, 0x00BB00, 0x0000BB, 0xBBBB00, 0xBB00BB, 0x00BBBB, 0x44BB44, 0xBB4444,
                    0x4444BB, 0x4488BB, 0x44BB88, 0x8844BB, 0x88BB44, 0xBB4488, 0xBB8844];

    var xorFold = function(str) {
        return _.reduce(str, function(acc, x){ return acc ^ x.charCodeAt(0); }, 0);
    }

    expose.packageSizes = function(metricsJSON) {
        var res = new Array();

        metrics = JSON.parse(metricsJSON);
        pkgs = metrics['packages'];
        
        var gridRows = Math.ceil(Math.sqrt(_.size(pkgs)));
        var count = 0;
        
        for (var key in pkgs) {
            if (pkgs.hasOwnProperty(key)) {
                var cur = {};
                cur.label = key;
                cur.side = defaultSide;
                cur.height = Math.max(minHeight, heightFactorPkg * _.size(pkgs[key]['classes']));
                cur.x = (count % gridRows) * defaultSide * 1.5;
                cur.z = Math.floor(count / gridRows) * defaultSide * 1.5;
                cur.color = colorsSet[0];
                res.push(cur);
                count++;
            }
        }
        
        return res;
    }

    expose.classSizes = function(metricsJSON) {
        var res = new Array();

        metrics = JSON.parse(metricsJSON);
        pkgs = metrics['packages'];

        var gridRows = _.size(pkgs);
        var count = 0;

        for (var key in pkgs) {
            if (pkgs.hasOwnProperty(key)) {
               var classes = pkgs[key]['classes'];
               for (var key2 in classes) {
                    if (classes.hasOwnProperty(key2)) {
                        var cur = {};
                        cur.label = key2;
                        cur.side = defaultSide;
                        cur.height = Math.max(minHeight, heightFactorCls * _.size(classes[key2]['methods']));
                        cur.x = (count % gridRows) * defaultSide * 1.5;
                        cur.z = Math.floor(count / gridRows) * defaultSide * 1.5;
                        cur.color = colorsSet[xorFold(key) % colorsSet.length];
                        res.push(cur);
                        count++;
                    }
               }
               count += gridRows - (count % gridRows);
            }
        }

        return res;
    }

    expose.methodComplex = function(metricsJSON) {
        var res = new Array();

        metrics = JSON.parse(metricsJSON);
        pkgs = metrics['packages'];

        var classesBlocks = [];
        for (var key in pkgs) {
            if (pkgs.hasOwnProperty(key)) {
                var classes = pkgs[key]['classes'];
                var methodCubes = [];
                for (var key2 in classes) {
                    if (classes.hasOwnProperty(key2)) {
                        var methods = classes[key2]['methods'];
                        for (var key3 in methods) {
                            if (methods.hasOwnProperty(key3)) {
                                var cur = {};
                                cur.label = "<b>" + key3 + "</b> in class <b>" + key2 + "</b> in package <b>" + key + "</b>";
                                cur.label += "<br />Size: <b>" + methods[key3]['size'] + "</b><br />Complexity: <b>" + methods[key3]['complexity'] + "</b>";
                                cur.side = Math.max(minSide, sideFactorMthd * methods[key3]['complexity']); 
                                cur.w = cur.h = cur.side + sideDistanceMthd;   // For the packer
                                cur.height = Math.max(minHeight, heightFactorMthd * methods[key3]['size']);
                                cur.color = colorsSet[xorFold(key2) % colorsSet.length];
                                methodCubes.push(cur);
                            }
                        }
                    }
                }
                methodCubes.sort(function(a,b) {return b.side - a.side} );
                var packer = new GrowingPacker();
                packer.fit(methodCubes);

                classBlock = {
                    cubes: methodCubes,
                    w: packer.root.w + sideDistanceMthd*2,
                    h: packer.root.h + sideDistanceMthd*2
                }
                classesBlocks.push(classBlock);
            }
        }
        classesBlocks.sort(function(a,b) {return (b.w * b.h) - (a.w * a.h)} );
        var packer = new GrowingPacker();
        packer.fit(classesBlocks);

        var classBlock, cube;
        for (i in classesBlocks) {
            classBlock = classesBlocks[i];
            for (j in classBlock.cubes) {
                cube = classBlock.cubes[j];
                cube.x += classBlock.x;
                cube.z += classBlock.z;
            }
            res = res.concat(classBlock.cubes);
        }

        return res;
    }

    expose.cubesToArrays = function(cubes) {
        var res = [];
        res.push(_.map(cubes, function(cube) { return cube.height; }));
        res.push(_.map(cubes, function(cube) { return cube.side; }));
        res.push(_.map(cubes, function(cube) { return cube.x; }));
        res.push(_.map(cubes, function(cube) { return cube.z; }));
        res.push(_.map(cubes, function(cube) { return cube.label; }));
        res.push(cubes.length);
        return res;
    }

    return expose;
})(_);
