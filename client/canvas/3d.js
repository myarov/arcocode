//3d.js
//CityFrame

c3dl.addMainCallBack(canvasMain, "CityFrame");
//Экспорт моделей
c3dl.addModel("canvas/models/SimpleBuilding.dae");
c3dl.addModel("canvas/models/ground.dae");

//Данные, принимаемые с сервера
// ЗАГЛУШКА!!!!
var class_count = 10;
var height=new Array (1,2,3,1,2,3,1,2,3,4);
var length=new Array (1,1,1,2,2,2,1,1,1,2);
var x_pos=new Array (0,200,400,0,200,400,0,200,400,0);
var z_pos=new Array (0,0,0,200,200,200,400,400,400,600);
//-----------------------

//тестовая модель для калибровки камеры
c3dl.addModel("canvas/models/duck.dae");
var duck;

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
            case 40://стрелка вниз
                cam.pitch(Math.PI * 0.025);//наклон вниз
            break;
            case 87://W
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
                cam.setPosition([pos[0],pos[1]-10,pos[2]]);//смещение вниз
            break;
            case 40://стрелка вниз
                cam.setPosition([pos[0],pos[1],pos[2]+10]);//смещение вперед
            break;
            case 87://W
                cam.setPosition([pos[0],pos[1]+10,pos[2]]);//смещение вверх
            break;
            case 38://стрелка вверх
                cam.setPosition([pos[0],pos[1],pos[2]-10]);//смещение назад
            break;
        }
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
	
		//добавление зданий
		/*for(var i = 0; i < class_count; i++){
	
			var building = new c3dl.Collada();	
			building.init("canvas/models/SimpleBuilding.dae");
			
			var ScaleVector = new Array (length[i], height[i], length[i]);
			var CoordVector = new Array (x_pos[i], 0.0, z_pos[i]);
		
			building.setPickable(false);
			
			building.scale(ScaleVector);
			building.setPosition (CoordVector);
			
			building.setTexture("canvas/textures/building.png");
			
			scn.addObjectToScene(building);
							
		}*/
	
		//инициализация модели для калибровки камеры
		duck = new c3dl.Collada();
		duck.init("canvas/models/duck.dae");
		duck.setTexture("canvas/textures/duck.png");
		duck.setPosition(new Array(0.0, 0.0, 0.0));
		duck.setPickable(false);
		scn.addObjectToScene(duck);
		
		//создание земли
		/*var ground = new c3dl.Collada();
		ground.init("canvas/models/ground.dae");
		ground.setTexture("canvas/textures/grass.png");
		ground.scale(new Array(1.0, 1.0, 1.0));
		ground.setPosition (new Array(0.0, 0.0, 0.0));
		ground.setPickable(false);
		scn.addObjectToScene(ground);*/
	
		//камера
		var cam = new c3dl.FreeCamera();
		cam.setPosition(new Array(0.0, 2500.0, 2500.0));
		cam.setLookAtPoint(new Array(0.0, 0.0, 0.0));
		scn.setCamera(cam);
		
		//опрос клавиатуры
		scn.setKeyboardCallback(up);
	
		//запуск сцены
		scn.startScene();
	}
	//конец рендера
}