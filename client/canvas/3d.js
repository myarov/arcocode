//3d.js
//—крипт выполн€ющий отрисовку 3D изображени€ в Canvas-панели CityFrame

c3dl.addMainCallBack(canvasMain, "CityFrame");

// main
function canvasMain(canvasName){

 // сцена
 scn = new c3dl.Scene();
 scn.setCanvasTag(canvasName);

 // рендер
 renderer = new c3dl.WebGL();
 renderer.createRenderer(this);

 scn.setRenderer(renderer);
 scn.init(canvasName);

 //создание объектов сцены
 if(renderer.isReady() )
 {

 // камера
 var cam = new c3dl.FreeCamera();

 cam.setPosition(new Array(200.0, 300.0, 500.0));

 cam.setLookAtPoint(new Array(0.0, 0.0, 0.0));

 scn.setCamera(cam);

 // запуск сцены
 scn.startScene();
 }
}