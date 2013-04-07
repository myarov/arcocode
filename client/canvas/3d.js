//3d.js
//������ ����������� ��������� 3D ����������� � Canvas-������ CityFrame

c3dl.addMainCallBack(canvasMain, "CityFrame");

// main
function canvasMain(canvasName){

 // �����
 scn = new c3dl.Scene();
 scn.setCanvasTag(canvasName);

 // ������
 renderer = new c3dl.WebGL();
 renderer.createRenderer(this);

 scn.setRenderer(renderer);
 scn.init(canvasName);

 //�������� �������� �����
 if(renderer.isReady() )
 {

 // ������
 var cam = new c3dl.FreeCamera();

 cam.setPosition(new Array(200.0, 300.0, 500.0));

 cam.setLookAtPoint(new Array(0.0, 0.0, 0.0));

 scn.setCamera(cam);

 // ������ �����
 scn.startScene();
 }
}