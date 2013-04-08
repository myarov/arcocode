//3d.js
//CityFrame

c3dl.addMainCallBack(canvasMain, "CityFrame");
//Экспорт моделей
c3dl.addModel("canvas/models/SimpleBuilding.dae");
c3dl.addModel("canvas/models/ground.dae");
c3dl.addModel("canvas/models/logo.dae");
var logo;

var cam = new c3dl.FreeCamera();
var scn;

//Данные, принимаемые с сервера
// ЗАГЛУШКА!!!!
var method_count = 10;
var height=new Array (1,2,3,1,2,3,1,2,3,4);
var length=new Array (1,1,1,2,2,2,1,1,1,2);
var x_pos=new Array (0,200,400,0,200,400,0,200,400,0);
var z_pos=new Array (0,0,0,200,200,200,400,400,400,600);
var method_labels=new Array ("Method 1", "Method 2", "Method 3", "Method 4", "Method 5", "Method 6", "Method 7", "Method 8", "Method 9", "Method 10");
//-----------------------

//опрос нажатия клавиш
function up(event){
    var cam = scn.getCamera();
    if(event.shiftKey) {
        switch(event.keyCode) {//+Shift
            case 65://A
                cam.roll(-Math.PI * 0.025);//наклон налево
                break;
            case 37://стрелка влево
                cam.yaw(Math.PI * 0.025);//поворот направо
                break;
            case 68://D
                cam.roll(Math.PI * 0.025);//наклон направо
                break;
            case 39://стрелка вправо
                cam.yaw(-Math.PI * 0.025);//поворот направо
                break;
            case 83://S
                cam.setPosition([pos[0],pos[1]-10,pos[2]]);//смещение вниз
            break;
            case 40://стрелка вниз
                cam.pitch(Math.PI * 0.025);//наклон вниз
            break;
            case 87://W
                cam.setPosition([pos[0],pos[1]+10,pos[2]]);//смещение вверх
            break;
            case 38://стрелка вверх
                cam.pitch(-Math.PI * 0.025);//наклон вверх
            break;
        }
    }
    else {
        var pos = cam.getPosition();
        switch(event.keyCode) {
            case 65://A
            case 37://стрелка влево
                cam.setPosition([pos[0]-10,pos[1],pos[2]]);//смещение влево
            break;
            case 68://D
            case 39://стрелка вправо
                cam.setPosition([pos[0]+10,pos[1],pos[2]]);//смещение вправо
            break;
            case 83://S
                cam.setPosition([pos[0],pos[1],pos[2]+10]);//смещение назад
            break;          
            case 87://W
                cam.setPosition([pos[0],pos[1],pos[2]-10]);//смещение вперед
            break;
        }
    }
} 

function target(targetObj){	
	var objectClick = targetObj.getObjects();
	
	if( objectClick.length > 0 )
	{
		alert (objectClick[0].method);
	}
}

// main
function canvasMain(canvasName){

	// инициализация сцены
	scn = new c3dl.Scene();
	scn.setCanvasTag(canvasName);
	
	// создание рендера
	renderer = new c3dl.WebGL();
	renderer.createRenderer(this);
	
	scn.setRenderer(renderer);
	scn.init(canvasName);
	 
	// установка цвета фона
	//scn.setBackgroundColor([0,0,0]);
	
	//рендер
	if(renderer.isReady() )
	{
		//инициализация модели логотипа
		logo = new c3dl.Collada();
		logo.init("canvas/models/logo.dae");
		logo.setTexture("canvas/textures/logo.png");
		logo.setPosition(new Array(250.0, 250.0, 250.0));
		logo.scale(new Array(50.0, 50.0, 50.0));
		logo.setPickable(false);
		//scn.addObjectToScene(logo);
	
		//добавление зданий
		for(var i = 0; i < method_count; i++){
	
			var building = new c3dl.Collada();	
			building.init("canvas/models/SimpleBuilding.dae");
			
			var ScaleVector = new Array (length[i], height[i], length[i]);
			var CoordVector = new Array (x_pos[i], 0.0, z_pos[i]);
			
			building.scale(ScaleVector);
			building.setPosition (CoordVector);
			
			building.setTexture("canvas/textures/building.png");
			
			building.method = method_labels[i];
			
			scn.addObjectToScene(building);
							
		}
		
		//создание земли
		var ground = new c3dl.Collada();
		ground.init("canvas/models/ground.dae");
		ground.setTexture("canvas/textures/grass.png");
		ground.scale(new Array(1.0, 1.0, 1.0));
		ground.setPosition (new Array(0.0, 0.0, 0.0));
		ground.setPickable(false);
		scn.addObjectToScene(ground);
	
		//камера
		cam.setPosition(new Array(2000.0, 2000.0, 2000.0));
		cam.setLookAtPoint(new Array(0.0, 0.0, 0.0));
		scn.setCamera(cam);
		
		//опрос клавиатуры
		scn.setKeyboardCallback(up);
	
		//запуск сцены
		scn.startScene();
		
		scn.setPickingCallback(target);
	}
	//конец рендера
}