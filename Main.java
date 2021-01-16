import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

/*
 * Main.java
 * Quinlan Sokol
 * 17/6/2019
 */
public class Main extends Applet implements MouseListener, ActionListener, KeyListener{
	//for double buffering
	Graphics2D offg;
	Image offscreen;
	
	//stores trail and clicks
	ArrayList<Symbol> symbols = new ArrayList<Symbol>();
	//stores collected items
	ArrayList<String> items = new ArrayList<String>();
	//stores background rects on menu
	ArrayList<Rectangle> menuRects = new ArrayList<Rectangle>() {{
		add(new Rectangle(100, 25, 300, 40));
		add(new Rectangle(450, 90, 300, 40));
		add(new Rectangle(-250, 180, 300, 40));
		add(new Rectangle(-50, 255, 300, 40));
		add(new Rectangle(300, 335, 300, 40));
		add(new Rectangle(600, 400, 300, 40));
	}};
	//stores pipe tiles
	ArrayList<Pipe> pipes = new ArrayList<Pipe>() {{
		add(new Pipe(125, 205, new ArrayList<String>() {{
			add("60->240");
			add("60->30");//correct
			add("60->120");
			add("60->180");
		}}));
		add(new Pipe(205, 285, new ArrayList<String>() {{
			add("180->90");
			add("90->270");
			add("210->30");
			add("30->150");//correct
		}}));
		add(new Pipe(285, 365, new ArrayList<String>() {{
			add("150->240");
			add("150->60");//correct
			add("90->180");
			add("270->60");
		}}));
		add(new Pipe(365, 445, new ArrayList<String>() {{
			add("120->270");
			add("240->30");
			add("60->210");//correct
			add("240->180");
		}}));
		add(new Pipe(445, 525, new ArrayList<String>() {{
			add("60->240");
			add("270->240");
			add("210->240");//correct
			add("150->240");
		}}));
	}};
	//for clicks and trail
	Point mousePos = new Point(-10, -10);
	Point prevPos = new Point();
	int trailDelay = 0;
	boolean overRules = false;
	boolean overCredits = false;
	boolean overPlay = false;
	
	//for actionperformed loop
	Timer timer;
	//for switching between scenes
	String gameState = "Menu";
	//variables for incremental text display
	int sentenceDelay = 0;
	String curLine = "";
	String finalLine = "... ... Ugh, your head hurts ... Where are you? ... Who are you? ... And why can't you remember your name?! ... And why is everything black?";
	
	//max size of choice box text
	final int TITLE_MAX_SIZE = 50;
	
	//variables for max amount of parts in a scene
	final int SCENE_1_MAX = 2;
	final int SCENE_2_MAX = 8;
	final int SCENE_2_JARS_MAX = 2;
	final int SCENE_2_MACHINE_MAX = 5;
	final int SCENE_3_MAX = 3;
	final int SCENE_3_ROOM_MAX = 4;
	final int SCENE_3_ROOM_END_MAX = 2;
	final int SCENE_3_ROOM_FIRE_MAX = 3;
	final int SCENE_3_ESCAPE_MAX = 4;
	final int SCENE_4_MAX = 3;
	final int SCENE_4_TANK_MAX = 3;
	final int SCENE_5_MAX = 5;
	final int ROBOT_OIL_MAX = 3;
	final int ROBOT_TALK_1_MAX = 3;
	final int ROBOT_END_MAX = 4;
	final int STAY_END_MAX = 3;
	final int SCENE_6_MAX = 2;
	final int DOOR_ESCAPE_MAX = 3;
	
	//counts parts
	int sceneCount = 1;
	
	//image variables
	Image scene_2_3;
	Image scene_2_4;
	Image scene_2_5;
	Image scene_2_6;
	Image scene_2_jars;
	Image scene_2_machine;
	Image scene_3_1;
	Image scene_3_3;
	Image scene_3_room;
	Image scene_3_room_fire;
	Image scene_3_escape;
	Image scene_3_escape_world;
	Image scene_4_tank;
	Image scene_5;
	Image scene_5_workshop;
	Image scene_5_robot;
	Image scene_5_explosion;
	Image cave_flood;
	Image scene_6;
	Image door_writing;
	Image manImage;
	Image match;
	Image lamp;
	Image fireplace;
	Image water;
		
	//variable for robot conversation
	String robotName = "ALC4";
	int robotCounter = 0;
	//alpha level for 'remember' box
	float boxAlpha = 0f;
	double boxInc = 0.01;
	
	//variables for end screen
	Image runeCircle;
	//alpha levels of circle
	float endAlpha = 0;
	double endInc = 0.01;
	//used for calculating time elapsed
	long startTime;
	boolean hasTimeStopped = false;
	int time;
	//stores what ending has been reached
	String ending;
	
	//conditional variables, adjusts graphics or storyline
	//used at door scene
	int knowledge = 0;
	boolean solveDelay = false;
	boolean seenJars = false;
	boolean seenMachine = false;
	boolean seenScroll = false;
	boolean hasRobot = false;
	boolean atDoor = false;
	boolean didSleep = false;
	String answer = "";
	
	//variables for machine test
	String input = "";
	int incorrectCounter = 0;
	double incorrectAlpha = 0;
	double incorrectInc = 0.04;
	
	//shape of entire applet window
	Rectangle window = new Rectangle(0, 0, 650, 450);
	
	@Override
	public void init(){	
		//initializing applet and adding input listeners
		this.addMouseListener(this);
		this.addKeyListener(this);
		this.setFocusable(true);
		this.setSize(650, 450);
		
		//for double buffering
		offscreen = createImage(650, 450);
		offg = (Graphics2D) offscreen.getGraphics();
		
		//loading custom font
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("Assets/Exocet.ttf")));
		} catch (FontFormatException | IOException e) {e.printStackTrace();}
		
		//initializing image variables
		scene_2_3 = getImage(getDocumentBase(), "Assets/Images/scene_2_3.png");
		scene_2_4 = getImage(getDocumentBase(), "Assets/Images/scene_2_4.png");
		scene_2_5 = getImage(getDocumentBase(), "Assets/Images/scene_2_5.png");
		scene_2_6 = getImage(getDocumentBase(), "Assets/Images/scene_2_6.png");
		scene_2_jars = getImage(getDocumentBase(), "Assets/Images/scene_2_jars.png");
		scene_2_machine = getImage(getDocumentBase(), "Assets/Images/scene_2_machine.png");
		scene_3_1 = getImage(getDocumentBase(), "Assets/Images/scene_3_1.png");
		scene_3_3 = getImage(getDocumentBase(), "Assets/Images/scene_3_3.png");
		scene_3_room = getImage(getDocumentBase(), "Assets/Images/scene_3_room.png");
		scene_3_room_fire = getImage(getDocumentBase(), "Assets/Images/scene_3_room_fire.png");
		scene_3_escape = getImage(getDocumentBase(), "Assets/Images/scene_3_escape.png");
		scene_3_escape_world = getImage(getDocumentBase(), "Assets/Images/scene_3_escape_world.png");
		scene_4_tank = getImage(getDocumentBase(), "Assets/Images/scene_4_tank.png");
		scene_5 = getImage(getDocumentBase(), "Assets/Images/scene_5.png");
		scene_5_workshop = getImage(getDocumentBase(), "Assets/Images/scene_5_workshop.png");
		scene_5_robot = getImage(getDocumentBase(), "Assets/Images/scene_5_robot.png");
		scene_5_explosion = getImage(getDocumentBase(), "Assets/Images/scene_5_explosion.png");
		cave_flood = getImage(getDocumentBase(), "Assets/Images/cave_flood.png");
		scene_6 = getImage(getDocumentBase(), "Assets/Images/scene_6.png");
		door_writing = getImage(getDocumentBase(), "Assets/Images/door_writing.png");
		manImage = getImage(getDocumentBase(), "Assets/Images/man.png");
		match = getImage(getDocumentBase(), "Assets/Images/match.png");
		lamp = getImage(getDocumentBase(), "Assets/Images/lamp.png");
		fireplace = getImage(getDocumentBase(), "Assets/Images/fireplace.png");
		water = getImage(getDocumentBase(), "Assets/Images/water.png");
		runeCircle = getImage(getDocumentBase(), "Assets/Rune Circles/circle1.png");
			
		//starts the actionperformed timer at a 15 milisecond rate
		timer = new Timer(15, this);
		timer.start();
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		//gets mouse pos, no click
		Point pos = this.getMousePosition();
		
		//switches scene depending on state
		
		/*
		 * Basic format for each scene:
		 * checks if the line needs to be updated and if the window has been clicked
		 * if the scene part is still less then the max amount, it updates the line and part count
		 * else, it will change the scene and reset the part count
		 */
		switch(gameState){
		case "Menu":
			//mouse trail code
			if(pos != null && pos.x != prevPos.x && pos.y != prevPos.y) {//if the mouse is onscreen and has moved
				if(trailDelay == 3) {
					symbols.add(new Symbol(getClickImage(), pos, 50));
					prevPos = new Point(pos.x, pos.y);
					trailDelay = 0;
				}else {
					//Separates the symbols so it is not crowded
					trailDelay++;
				}
			}
			
			if(new Rectangle(55, 175, 110, 30).contains(mousePos)) { //clicked 'play'
				gameState = "Scene_1";
				//starts the time counter
				startTime = System.currentTimeMillis();
			}
			
			//checking if the mouse is hovering over the labels
			overPlay = false;
			overRules = false;
			overCredits = false;
			if(pos != null) {
				if(new Rectangle(55, 225, 135, 30).contains(pos)) {
					overRules = true;
				}
				if(new Rectangle(55, 275, 190, 30).contains(pos)) {
					overCredits = true;
				}
				if(new Rectangle(55, 175, 110, 30).contains(pos)) {
					overPlay = true;
				}
			}
			break;
		case "Scene_1":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < SCENE_1_MAX) {
					sceneCount++;
					curLine = "";
					finalLine = "Oh wait ... it's because your eyes are closed. Maybe you should try opening them ... But you still feel so tired...";
				}else {
					gameState = "Scene_1_Choice";
					sceneCount = 1;
				}
			}
			break;
		case "Scene_1_Choice":
			if(new Rectangle(165, 40, 300, 125).contains(mousePos)) {//sleep
				didSleep = true;
				gameState = "Scene_6";
				curLine = "";
				finalLine = "You decide to sleep for a bit longer, and roll over and close your eyes. Later, you are suddenly shook awake by the sound of a voice booming through the cave. ENABLING INTRUDER DETECTION ... BEGINNING SCAN. Jumping up off the ground, you rush through the caves trying to find a way out. Finally, you come across a large, ancient door.";
			}else if(new Rectangle(165, 200, 300, 125).contains(mousePos)) {//wake up
				gameState = "Scene_2";
				finalLine = "You decide the best idea is to get up and figure out where you are, and what has happened to you. However, as you slowly open your eyes...";
				curLine = "";
			}
			break;
		case "Scene_2":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < SCENE_2_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2){
						finalLine = "Your eyes are suddenly filled with blinding light, making you feel as if you are being burnt alive. You quickly close your eyes again, giving yourself a chance to recover. You soon try again, although with much more caution.";
					}else if(sceneCount == 3){
						finalLine = "Now that your eyes are accustomed to the sudden light, you have the chance to examine the room you find yourself in. The image is still blurry, but it slowly begins to gain clarity as you gradually shake off the drowsiness.";
					}else if(sceneCount == 4){
						finalLine = "You can now see that you are in a cave lit by bright lamps and lights along the ceiling. The room seems to be filled with jars, crates and flasks, many of which are filled with strange substances. You also notice a rather large desk tucked in the corner, with many scrolls and notes stacked upon it.";
					}else if(sceneCount == 5){
						finalLine = "As you begin to approach the desk, you notice that one of the scrolls is still open. Curious to see if it will reveal anything about your situation, you decide to read it:";
					}else if(sceneCount == 6){
						//26 July, 1547
						finalLine = "26 Quintilis, MDXLVIII ... \"Today is the final day, the day I have been working towards all this time. At last I will succeed in crafting the Elixir! With this impending success, I will have surpassed the achievements of my ancestors, and finally exceed the constraints of this life!\""; 
					}else if(sceneCount == 7){
						finalLine = "\"Although many of the previous trials showed ... less then ideal results ... I believe this time will be different! I know that all my hard work will come to fruition! In fact, it must...The Alchemist\"   The note ends here.";
					}else if(sceneCount == 8){
						finalLine = "You do not fully understand the meaning of the scroll, but it seems as if you are in an alchemy lab belonging to someone called \'The Alchemist\'. You wonder if they are in the cave somewhere, and if they could help you regain your memories. Turning back away from the table, you decide what to do next.";
					}
				}else {
					gameState = "Scene_2_Choice";
					sceneCount = 1;
				}
			}
			break;
		case "Scene_2_Choice":
			if(new Rectangle(165, 40, 300, 125).contains(mousePos)) {//leave
				gameState = "Scene_3";
				curLine = "";
				finalLine = "Leaving the strange laboratory behind, you now find yourself in a damp tunnel lit by lanterns mounted on the walls. It seems to go on for a while, and you prepare yourself for what might be a long trek through the caves.";
			}else if(new Rectangle(15, 200, 300, 125).contains(mousePos) && !seenMachine) {//examine 'machine'
				gameState = "Scene_2_Machine";
				curLine = "";
				finalLine = "As you examine it closer, you discover that what you thought was a machine is actually a series of connecting tubes and pipes, all leading into stills and alembics. You also notice another open scroll laying nearby, and you decide to read it:";
			}else if(new Rectangle(335, 200, 300, 125).contains(mousePos) && !seenJars) {//examine jars
				gameState = "Scene_2_Jars";
				seenJars = true;
				curLine = "";
				finalLine = "You approach the dusty shelf fixed to the wall, filled to the edge with jars and pots. Each is filled with a strange liquid and other substances you cannot quite identify. However, you manage to recognize three of the substances.";
			}
			break;
		case "Scene_2_Jars":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < SCENE_2_JARS_MAX) {
					sceneCount++;
					curLine = "";
					finalLine = "You can only hold one jar, so you have to choose between the oil, the cold water, or the alcohol. Which do you choose?";
				}
				else {
					gameState = "Scene_2_Jars_Choice";
					sceneCount = 1;
				}
			}
			break;
		case "Scene_2_Jars_Choice":
			if(new Rectangle(165, 40, 300, 125).contains(mousePos)) {//Oil
				curLine = "You choose to take the oil. Where do you go now?";
				gameState = "Scene_2_Choice";
				items.add("Oil");
			}else if(new Rectangle(15, 200, 300, 125).contains(mousePos)) {//Water
				curLine = "You choose to take the water. Where do you go now?";
				gameState = "Scene_2_Choice";
				items.add("Water");
			}else if(new Rectangle(335, 200, 300, 125).contains(mousePos)) {//Alcohol
				curLine = "You choose to take the alcohol. Where do you go now?";
				gameState = "Scene_2_Choice";
				items.add("Alcohol");
			}
			break;
		case "Scene_2_Machine":
			if(!updateLine() && window.contains(mousePos) && sceneCount != 4) {
				if(sceneCount < SCENE_2_MACHINE_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2) {
						finalLine = "18 Maius, MDXLVIII ... \"During my studies, Master has assigned me the task of discovering the differing results of mixtures of the four elements: earth, fire, water, and air. And now, the next step in my research is to discover how many possible mixtures I can create.\"";
					}else if(sceneCount == 3){
						finalLine = "After looking over The Alchemist's notes, you decide it may help you to learn more about alchemy as a potential method of restoring your memory. Now, you decide to complete the problem The Alchemist was trying to solve.";
					}else if(sceneCount == 4){//machine question
						finalLine = "It seems as if The Alchemist was testing solutions which used 7 drops of any combination of the 4 elements. With this in mind, how many different combinations are possible?  (Type in your answer and press enter to submit)";
					}
				}
				else {
					gameState = "Scene_2_Choice";
					seenMachine = true;
					curLine = "Where do you choose to go now?";
					sceneCount = 1;
				}
			}
			else if(new Rectangle(10, 10, 85, 25).contains(mousePos)) {
				gameState = "Scene_2_Choice";
				curLine = "You decide to give up on the machine for now. What do you choose to do next?";
				sceneCount = 1;
				input = "";
			}
			break;
		case "Scene_3":
			//checks if the mouse is over the lantern
			//then switches the mouse cursor
			if(pos != null && (sceneCount == 1 || sceneCount == 2)){
				if(new Rectangle(525, 25, 50, 50).contains(pos)){
					this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}else{
					this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
			
			//checks if lantern is clicked
			if((sceneCount == 1 || sceneCount == 2) && new Rectangle(525, 25, 50, 50).contains(mousePos)){
				curLine = "";
				if(items.contains("Lantern")){
					finalLine= "You already have a lantern!";
				}else{
					items.add("Lantern");
					finalLine= "You picked up a lantern from the wall, thinking it might help you later.";
				}
			}else if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < SCENE_3_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2){
						finalLine = "As you continue to wander through the cave, you begin to wonder about what kind of person The Alchemist may be. Will they be willing to help you, or will they cast you aside into a world you do not remember anything about. Or worse, maybe they will use you in one of their experiments...";
					}else if(sceneCount == 3){
						finalLine = "However, you soon find yourself at a junction, causing your previous train of thought to be interrupted. You can now choose to go left or right, and both passages look exactly the same. Which path do you choose?";
					}
				}else {
					this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					gameState = "Scene_3_Choice";
					sceneCount = 1;
				}
			}
			break;
		case "Scene_3_Choice":
			if(new Rectangle(165, 40, 300, 125).contains(mousePos)) {//left
				gameState = "Scene_4";
				curLine = "";
				finalLine = "As you slowly walk through the corridor to the left, you begin to notice small, thin pipes running along the ceiling. There are four of them: red, green, yellow, and blue." + (knowledge >= 1 ? " You know from solving The Alchemist's machine that these must relate to the 4 elements, fire, earth, air, and water respectively. Although you wonder about what they might hold inside..." : " You wonder about what they must be for.");
			}else if(new Rectangle(165, 200, 300, 125).contains(mousePos)) {//right
				gameState = "Scene_3_Room";
				finalLine = "After a couple minutes of walking down the corridor, you find yourself in what seems to be a bedroom, possibly The Alchemist's? It is designed very simply, with only a bed, some tables, and a lit fireplace in the corner. Also, on one of those tables you spot another open scroll, and decide to investigate.";
				curLine = "";
			}
			break;
		case "Scene_3_Room":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < SCENE_3_ROOM_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2){
						finalLine = "2 Quintilus, MDXLVIII ... \"My work seems to be getting more and more stressful lately, and without anymore progress either. And with Master gone and murdered, my paranoia has only grown alongside my fixation on the Elixir\"";
					}else if(sceneCount == 3){
						finalLine = "\"I have taken to developing a variety of traps throughout the caves to prevent the same from happening to me. I have no one left to trust but myself, and so I must immerse myself in my work, away from the rest of the world.\"";
					}else if(sceneCount == 4){
						finalLine = "The moment you finish reading the scroll, you hear a faint click come from behind, followed shortly by a large rock falling into the doorway, trapping you inside. You also see a faint green gas begin to fill the room from the corners. This is when you suddenly realize you must be caught in one of The Alchemist's traps!";
					}
				}else {
					if(seenJars){
						gameState = "Scene_3_Room_Choice";
					}else{//dies
						gameState = "Scene_3_Room_End";
						curLine = "";
						finalLine = "Paralyzed in fear, you are unable to do anything as the gas slowly moves towards you. It suddenly becomes very hard to breathe, as if the gas is constricting your throat. You start to feel light-headed and your legs become weak.";
					}
					sceneCount = 1;
				}
			}
			break;
		case "Scene_3_Room_Choice":
			//used to correctly size an position the box
			int boxX = items.contains("Oil") ? 120 : items.contains("Water") ? 85 : 60;
			int boxWidth = items.contains("Oil") ? 390 : items.contains("Water") ? 465 : 520;
			if(new Rectangle(110, 40, 420, 125).contains(mousePos)) {//boulder
				gameState = "Scene_3_Room_Boulder";
				curLine = "";
				finalLine = "You rush over to the boulder which is blocking the doorway and launch your fist towards it with all the power you can muster. A loud *crack* reverberates throughout the room, accompanied by a sharp pain shooting up your arm. You broke your wrist.";
			}else if(new Rectangle(boxX, 200, boxWidth, 125).contains(mousePos)) {//fire
				String item = items.contains("Oil") ? "oil" : items.contains("Water") ? "water" : "alcohol";
				gameState = "Scene_3_Room_Fire";
				curLine = "";
				
				if(item.equals("water")){
					finalLine = "You throw the bucket of water into the fire, causing it to be extinguished. After you blow the smoke away, you are surprised to find a secret passage hidden inside!";
				}else{
					finalLine = "You throw the bucket of " + item + " into the fire. It quickly blazes up forcing you to step back away from it, right into the gas.";
				}
			}
			break;
		case "Scene_3_Room_End":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < SCENE_3_ROOM_END_MAX) {
					sceneCount++;
					curLine = "";
					finalLine = "Then you black out...";
				}else {
					gameState = "End";
					ending = "Death";
					sceneCount = 1;
				}
			}
			break;
		case "Scene_3_Room_Boulder":
			if(!updateLine() && window.contains(mousePos)) {
				gameState = "Scene_3_Room_End";
				sceneCount = 1;
				curLine = "";
				finalLine = "Paralyzed in fear, you are unable to do anything as the gas slowly moves towards you. It suddenly becomes very hard to breath, as if the gas is constricting your throat. You start to feel light-headed and your legs become weak.";
			}
			break;
		case "Scene_3_Room_Fire":
			if(!updateLine() && window.contains(mousePos)) {
				String item = items.contains("Oil") ? "oil" : items.contains("Water") ? "water" : "alcohol";
				curLine = "";
				sceneCount = 1;
				
				if(item.equals("water")){
					finalLine = "You quickly dive into the hidden passage and slam the door shut behind you, narrowly avoiding the gas. Leaning against the wall of the chamber, you take a short-lived break to calm down, until you notice gas slowly leaking through the doorway.";
					gameState = "Scene_3_Escape";
				}else{
					finalLine = "Paralyzed in fear, you are unable to do anything as the gas slowly moves towards you. It suddenly becomes very hard to breath, as if the gas is constricting your throat. You start to feel light-headed and your legs become weak.";
					gameState = "Scene_3_Room_End";
				}
			}
			break;
		case "Scene_3_Escape":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < SCENE_3_ESCAPE_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2){
						if(items.contains("Lantern")) {
							finalLine = "You decide to pull out the lantern you took earlier so you can see better in the darkness of the tunnel. It was a good idea to take it with you! ";
						}else {
							finalLine = "The tunnel is pitch black, you cannot see anything! If only you had grabbed a light or a lantern earlier...";
						}
					}else if(sceneCount == 3){
						if(items.contains("Lantern")) {
							finalLine = "As you make your way through the tunnel, you notice pits all along the sides of the walls. You are able to avoid these with the use of the lantern. Eventually, you begin to see a bright light in front of you, it must be the outside world!";
						}else {
							finalLine = "You steadly make your way through the passage by keeping one hand on the wall. This strategy works for a bit, until the wall suddenly runs out. In surprise, you fall to the side and into a pit in the tunnel. You should have brought a light!";
						}
					}else if(sceneCount == 4){
						if(items.contains("Lantern")) {
							finalLine = "You did it! You escaped! However, you still don't remember anything about yourself, or this world.  But you are now free to live and learn!";
						}else {
							sceneCount = 1;
							gameState = "End";
							ending = "Death";
						}
					}
				}else {
					gameState = "End";
					ending = "Escape";
					sceneCount = 1;
				}
			}
			break;
		case "Scene_4":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < SCENE_4_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2){
						finalLine = "Following these pipes, they seem to lead into a large room fill with tanks, containers, jars, and other storage devices. Each of the 4 pipes connects to larger tanks placed in the center of the room. And on each of these tanks, more pipes lead out of them and into other parts of the room and through some of the caves.";
					}else if(sceneCount == 3){
						finalLine = "You belive this must be where The Alchemist stores most of their materials so they can be used later. You are intrigued to learn about how these machines work, but you also feel that you should keep moving through the cave. What do you do?";
					}
				}else {
					gameState = "Scene_4_Choice";
					sceneCount = 1;
				}
			}
			break;
		case "Scene_4_Choice":
			if(new Rectangle(165, 40, 300, 125).contains(mousePos)) {//examine
				gameState = "Scene_4_Tank";
				curLine = "";
				finalLine = "As you walk over to the giant tanks, you notice another open scroll on the ground nearby. You're beginning to wonder why you are finding so many of these. Did The Alchemist maybe want you to find them...";
			}else if(new Rectangle(165, 200, 300, 125).contains(mousePos)) {//leave
				gameState = "Scene_5";
				finalLine = "As you leave the strange storage room behind, you once again begin to think about your situation here. Why is it that you are stuck in an alchemy lab? It must be somehow connected to your amnesia, and if so, The Alchemist must know something about you. You now realize that you must find them if you hope to uncover your past.";
				curLine = "";
			}
			break;
		case "Scene_4_Tank":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < SCENE_4_TANK_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2){
						finalLine = "14 Iunius, MDXLVIII ... \"It took a while, but I was finally able to reinforce and expand the elemental tanks. It was very difficult to keep the elemental essences safe and contained while doing so, so Master assited me in the process, making the strain much easier on me. Hopefully these won't burst like the last set...\"";
					}else if(sceneCount == 3){
						knowledge++;
						finalLine = "After examining the tank further, you discover that it also connects into many other pipes, perhaps to distribute the material to other machines in the caves? As a result of this discovery, your alchemical knowledge has increased!";
					}
				}else {
					gameState = "Scene_5";
					curLine = "";
					finalLine = "As you leave the strange storage room behind, you once again begin to think about your situation here. Why is it that you are stuck in an alchemy lab? It must be somehow connected to your amnesia, and if so, The Alchemist must know something about you! You now realize that you must find them if you hope to uncover your past.";
					sceneCount = 1;
				}
			}
			break;
		case "Scene_5":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < SCENE_5_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2){
						finalLine = "The new room you find yourself in is a little more modern then the others. It looks to be somekind of workshop, littered with wood and metal parts. Weirdly, they are mixed together with the pipes and some of the unknown substances you've seen before in the lab.";
					}else if(sceneCount == 3){
						finalLine = "While you can guess what some of the mechanisms laying around can do, there are many which you have know idea about. However, you feel drawn towards one of the projects laying around, the humanoid robot sitting in the corner.";
					}else if(sceneCount == 4){
						finalLine = "It seems to be completely built, with a thick metal exterior and a variety of unknown mechanisms on the inside. These are all made up of wood, some metal, and strangely the same kind of pipes you saw in the storage room. And after you look closer, you notice the back of the robot is attached to more of these pipes, perhaps as a power source?";
					}else if(sceneCount == 5){
						finalLine = "Returning your attention back to the room, you notice another scroll laying on the table to the side of the robot. Maybe it will provide insight on what the robot is for, or teach you more about The Alchemist. Do you choose to read the scroll?";
					}
				}else {
					gameState = "Scene_5_Scroll_Choice";
					sceneCount = 1;
				}
			}
			break;
		case "Scene_5_Scroll_Choice":
			if(new Rectangle(165, 40, 300, 125).contains(mousePos)) {//read
				gameState = "Scene_5_Scroll";
				seenScroll = true;
				curLine = "";
				finalLine = "It looks like this note was written only a couple days before the first scroll I read. 21 Quintilis, MDXLVIII ... \"Once complete, this will be one of the largest discoveries in alchemical history! I have created a robot with an artificial human mind, with the capability to grow and learn. It will be called ALC4, once I find a suitable fuel to start it up.\"";
			}else if(new Rectangle(165, 200, 300, 125).contains(mousePos)) {//don't read
				gameState = "Scene_5_Robot";
				finalLine = "Moving your gaze back to the robot, you notice a valve on the side of its body. You wonder if it is where the fuel is poured into. Now if only you had something to use...";
				curLine = "";
			}
			break;
		case "Scene_5_Scroll":
			if(!updateLine() && window.contains(mousePos)) {
				gameState = "Scene_5_Robot";
				sceneCount = 1;
				curLine = "";
				finalLine = "Moving your gaze back to the robot, you notice a valve on the side of its body. That must be where the fuel is poured into! Now if only you had something to use...";
			}
			break;
		case "Scene_5_Robot":
			if(!updateLine() && window.contains(mousePos)) {
				sceneCount = 1;
				
				if(seenJars){
					//has liquid item
					gameState = "Scene_5_Robot_Choice";
				}else{//leave
					gameState = "Scene_6";
					curLine = "";
					finalLine = "Since you can do nothing else here, you decide to keep moving forward. After travelling through another long corridor, you find yourself infront of a large, ancient door which seems to be locked.";
				}
			}
			break;
		case "Scene_5_Robot_Choice":
			//correctly sizes the box
			int boxX2 = items.contains("Oil") ? 165 : items.contains("Water") ? 115 : 90;
			int boxWidth2 = items.contains("Oil") ? 300 : items.contains("Water") ? 400 : 450;
			
			if(new Rectangle(165, 40, 300, 125).contains(mousePos)) {//leave
				gameState = "Scene_6";
				curLine = "";
				finalLine = "You choose to do nothing else here, and decide to keep moving forward. After travelling through another long corridor, you find yourself infront of a large, ancient door which seems to be locked.";
			}else if(new Rectangle(boxX2, 200, boxWidth2, 125).contains(mousePos)) {//use item
				String item = items.contains("Oil") ? "oil" : items.contains("Water") ? "water" : "alcohol";
				curLine = "";
				
				if(items.contains("Oil")){
					gameState = "Robot_Oil";
					finalLine = "You decide that the oil you collected earlier would work well as a fuel, and carefully pour it into the robot. After a few seconds, one of the lights on the robot's body turns on. It worked!";
				}else{
					gameState = "Scene_5_Explosion";
					finalLine = "You don't know why " + item + " would work as a fuel, but you decide that you might as well try.  A few seconds after you pour it in, the robot begins to shake uncontrollably, right before it explodes and you are consumed by flames and buried under the collapsing cave.";
				}
			}
			break;
		case "Robot_Oil":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < ROBOT_OIL_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2){
						finalLine = "You can hear a faint humming from inside the robot as it begins to turn on. Suddenly, the plate on the front of the robot pops open, revealing a set of pipes meant to transfer some kind of fluid throughout the body. They seem to be rusted and broken.";
					}else if(sceneCount == 3){
						finalLine = "Looking around the room, you can see more of these pipes scattered across the table. Maybe you can repair the robot with these...";
					}
				}else {
					gameState = "Robot_Puzzle";
					sceneCount = 1;
				}
			}
			break;
		case "Scene_5_Explosion":
			if(!updateLine() && window.contains(mousePos)) {
				sceneCount = 1;
				curLine = "";
				gameState = "End";
				ending = "Death";
			}
			break;
		case "Robot_Puzzle":
			//checks if the previous tile is powered
			//if so, current tile is set to be powered
			int prevEnd = 60;
			boolean isPrevPowered = true;
			for(int i = 0; i < pipes.size(); i++) {
				if(isPrevPowered) {
					//gets the positions of the ends
					int startY = Integer.parseInt(pipes.get(i).arr.get(pipes.get(i).index).split("->")[0]);
					int endY = Integer.parseInt(pipes.get(i).arr.get(pipes.get(i).index).split("->")[1]);
					
					if(startY == prevEnd) {
						pipes.get(i).isPowered = true;
						isPrevPowered = true;
						prevEnd = endY;
					}else {
						pipes.get(i).isPowered = false;
						isPrevPowered = false;
					}
				}else {
					pipes.get(i).isPowered = false;
				}
			}
			
			if(isPrevPowered) {
				//wins minigame
				//delay allows paint to update first
				if(solveDelay) {
					try {
						TimeUnit.MILLISECONDS.sleep(2500);
					} catch (InterruptedException e1){}
					gameState = "Robot_Talk_1";
					curLine = "";
					finalLine = "A few seconds after you connect the last pipe, the panel snaps shut the robot slowly begins to move. You jump back in surprise, nearly falling backwards in the process. Then, the robot shifts it head towards you, and starts to speak...";
				}else {
					solveDelay = true;
				}
			}else {
				//clicking to change tile
				if(new Rectangle(125, 75, 80, 300).contains(mousePos)) {
					pipes.get(0).rotate();
				}
				if(new Rectangle(205, 75, 80, 300).contains(mousePos)) {
					pipes.get(1).rotate();
				}
				if(new Rectangle(285, 75, 80, 300).contains(mousePos)) {
					pipes.get(2).rotate();
				}
				if(new Rectangle(365, 75, 80, 300).contains(mousePos)) {
					pipes.get(3).rotate();
				}
				if(new Rectangle(445, 75, 80, 300).contains(mousePos)) {
					pipes.get(4).rotate();
				}
				
				if(new Rectangle(10, 10, 85, 25).contains(mousePos)){//exit button
					gameState = "Scene_6";
					curLine = "";
					finalLine = "After giving up on fixing the robot, you decide to keep moving forward. After travelling through another long corridor, you find yourself infront of a large, ancient door which seems to be locked.";
				}
			}
			break;
		case "Robot_Talk_1":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < ROBOT_TALK_1_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2){
						finalLine = "\"... Assessing Environment ... Lifeform Detected ... Analyzing ... ... Identity \'Creator\' ... Loading Language Library ... Installation Complete ... Communicate With Lifeform ... ... Hello human, it seems you have activated me, may I know your name?\"";
					}else if(sceneCount == 3){
						finalLine = "After you exclaim that you don't remember, the robot responds, \"I see, very strange, however it seems I do not have a name either, so how do you wish to refer to me?\"  What will you name the robot?";
					}
				}else {
					gameState = "Robot_Question_1";
					sceneCount = 1;
				}
			}
			break;
		case "Robot_Question_1":
			boolean bool = false;
			if(new Rectangle(15, 40, 300, 125).contains(mousePos)){//Wall-E
				bool = true;
				robotName = "Wall-E";
			}else if(new Rectangle(335, 40, 300, 125).contains(mousePos)){//ALC4
				bool = true;
				robotName = "ALC4";
			}else if(new Rectangle(15, 200, 300, 125).contains(mousePos)){//Scrap
				bool = true;
				robotName = "Scrap";
			}else if(new Rectangle(335, 200, 300, 125).contains(mousePos)){//Dio
				bool = true;
				robotName = "Dio";
			}
			
			if(bool){
				gameState = "Robot_Talk_2";
				curLine = "";
				finalLine = "You tell the robot your decision. \"Very well ... I will be known as " + robotName + " from now on.\" it says, \"But may I ask why you are here in this alchemy lab?\"";
			}
			break;
		case "Robot_Talk_2":
			if(!updateLine() && window.contains(mousePos)) {
				sceneCount = 1;
				gameState = "Robot_Question_2";
			}
			break;
		case "Robot_Question_2":
			if(new Rectangle(165, 40, 300, 125).contains(mousePos)) {//Tell truth
				gameState = "Robot_Talk_3";
				curLine = "";
				finalLine = "You tell " + robotName + " that you woke up in here a few hours ago, not knowing who or where you were. " + robotName + " seems to believe you.";
				robotCounter++;
			}else if(new Rectangle(165, 200, 300, 125).contains(mousePos)) {//Tell lie
				gameState = "Robot_Talk_3";
				curLine = "";
				finalLine = "You tell " + robotName + " that you live and work here, and that been here for years. " + robotName + " seems suspicious of your answer.";
				robotCounter--;
			}
			break;
		case "Robot_Talk_3":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < 2) {
					sceneCount++;
					curLine = "";
					finalLine = robotName + " moves onto another question, \"Do you know of what my purpose is? I cannot find any information about why I was created.\" How will you respond to " + robotName + "?";
				}else {
					sceneCount = 1;
					gameState = "Robot_Question_3";
				}
			}
			
			//updates 'remember' box
			if(boxAlpha >= 1){
				boxInc *= -1;
			}
			boxAlpha += boxInc;
			boxAlpha = boxAlpha < 0 ? 0 : boxAlpha > 1 ? 1 : boxAlpha;
			break;
		case "Robot_Question_3":
			if(new Rectangle(165, 40, 300, 125).contains(mousePos)) {//slave
				gameState = "Robot_Talk_4";
				curLine = "";
				finalLine = "You respond by stating " + robotName + " was created to be your slave, obeying your every command. " + robotName + " appears doubtful and seems to now distrust you.";
				robotCounter--;
				boxInc = 0.01;
				boxAlpha = 0;
			}else if(new Rectangle(165, 200, 300, 125).contains(mousePos)) {//helper
				gameState = "Robot_Talk_4";
				curLine = "";
				finalLine = "You respond by stating " + robotName + " was created to be a helper and assistant in the lab experiments. " + robotName + " appears happy and satisfied with your answer.";
				robotCounter++;
				boxInc = 0.01;
				boxAlpha = 0;
			}
			break;
		case "Robot_Talk_4":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < 2) {
					sceneCount++;
					curLine = "";
					finalLine = "It seems " + robotName + " has one final question for you. \"Do you wish to escape this place, or do you wish to stay and learn?\". What do you choose?";
				}else {
					sceneCount = 1;
					gameState = "Robot_Question_4";
				}
			}
			
			//updates 'remember' box
			if(boxAlpha >= 1){
				boxInc *= -1;
			}
			boxAlpha += boxInc;
			boxAlpha = boxAlpha < 0 ? 0 : boxAlpha > 1 ? 1 : boxAlpha;
			break;
		case "Robot_Question_4":
			if(new Rectangle(165, 40, 300, 125).contains(mousePos)) {//escape
				gameState = "Robot_Talk_5";
				curLine = "";
				finalLine = "After processing all of your answers, " + robotName + " stands up and begins to walk away towards the exit of the room.";
				robotCounter--;
				answer = "escape";
			}else if(new Rectangle(165, 200, 300, 125).contains(mousePos)) {//stay
				gameState = "Robot_Talk_5";
				curLine = "";
				finalLine = "After processing all of your answers, " + robotName + " stands up and begins to walk away towards the exit of the room.";
				robotCounter++;
				answer = "stay";
			}
			break;
		case "Robot_Talk_5":
			if(!updateLine() && window.contains(mousePos)) {
				sceneCount = 1;
				curLine = "";
				
				if(robotCounter < 0){//negative answers
					finalLine = "\"Disappointing...\", " + robotName + "says, \"I was hoping you would be better then this, Cre----\". The rest of " + robotName + "'s words are cut off by a loud voice booming through the caves.";
					gameState = "Robot_End";
				}else if(robotCounter > 0){//positive answers
					if(answer.equals("escape")){
						finalLine = "\"Follow me, I will show you the way out\", " + robotName + " states. You follow them through a long, winding corridor, until you finally reach a large, ancient door. " + robotName + " answers your confusion by telling you \"To open the door and escape, you must solve the riddle. But if you answer incorrect, you will die\".";
						gameState = "Scene_6";
						hasRobot = true;
					}else if(answer.equals("stay")){
						finalLine = robotName + " turns around to face you. \"If you wish to stay, then you should know about who you are\". Confused about " + robotName + "'s response, you ask them what they mean. \"While your true name has been lost long ago, you still retain your given title\", " + robotName + " answers.";
						gameState = "Stay_End";
					}
				}
			}
			break;
		case "Robot_End":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < ROBOT_END_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2){
						finalLine = "WARNING ... WARNING ... INTRUDER DETECTED ... ELIMINATING THREAT";
					}else if(sceneCount == 3){
						finalLine = "Soon after hearing this announcement, you can hear a deep roar rumbling through the cave. Suddenly, a giant wave of water blasts into the room, slamming you into the walls.";
					}else if(sceneCount == 4){
						finalLine = "In one of these violent waves, your head slams hard against a rock. Your vision begins to darken until everything is dark.";
					}
				}else {
					sceneCount = 1;
					gameState = "End";
					ending = "Death";
				}
			}
			break;
		case "Stay_End":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < STAY_END_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2){
						finalLine = "\"You are The Alchemist, and my Creator as well\", " + robotName + " exclaims. You almost fall over in shock, but as you begin to think about it, you start to believe it. How else would you have been in here in the first place, and how else could you have been to naturally skilled at alchemy?";
					}else if(sceneCount == 3){
						finalLine = "Over the next few weeks, you begin to regain some of your lost memories while also getting accustomed to your role as The Alchemist. It turns out the reason you lost your memory in the first place was because of a failed experiement trying to make the Elixir. Soon afterwards, you discover a safer method to create it, and succeed, therefore gaining eternal life.";
					}
				}else {
					sceneCount = 1;
					gameState = "End";
					ending = "Memories";
				}
			}
			break;
		case "Scene_6":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < SCENE_6_MAX) {
					sceneCount++;
					curLine = "";
					
					if(hasRobot) {
						finalLine = "When you begin to closely study the door, you notice writing in some strange language you have never seen before. It must be the riddle! And underneath the writing there is a carved image, maybe you have to press somewhere on it to answer the riddle? " + robotName + " translates the riddle for you.";
					}else {
						if(knowledge >= 2) {
							finalLine = "When you begin to examine the door, you suddenly find you are able to read the writing on it. You had seen it in some of The Alchemist's notes, and here it seems to describe a riddle. And underneath the writing there is a carved image, maybe you have to press somewhere on it to answer the riddle?";
						}else {
							finalLine = "When you begin to examine the door, you suddenly find you cannot understand any of the writing. If only you took the time to study all of the equipment and notes in the cave, maybe you could understand it.";
						}
					}
				}else {
					sceneCount = 1;
					if(knowledge >= 2 || hasRobot) {
						gameState = "Door_Puzzle";
					}else {
						gameState = "Robot_End";
						finalLine = "After a minute of examining the writing, you hear a loud voice boom through the cavern...";
						curLine = "";
						atDoor = true;
					}
				}
			}
			break;
		case "Door_Puzzle":
			//changes mouse cursor when over an image
			if(pos != null) {
				if(new Rectangle(405, 200, match.getWidth(this), match.getHeight(this)).contains(pos) || new Rectangle(200, 80, lamp.getWidth(this), lamp.getHeight(this)).contains(pos) || new Rectangle(185, 300, fireplace.getWidth(this), fireplace.getHeight(this)).contains(pos) || new Rectangle(25, 200, water.getWidth(this), water.getHeight(this)).contains(pos)) {
					this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				}else {
					this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
			
			//checking clicking for images
			if(new Rectangle(405, 200, match.getWidth(this), match.getHeight(this)).contains(mousePos)) {//match
				gameState = "Door_Escape";
				finalLine = "The match! That must be the answer! And as soon as you press the image of the match, you hear a loud click from inside the door, followed by it swinging open.";
				curLine = "";
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			if(new Rectangle(200, 80, lamp.getWidth(this), lamp.getHeight(this)).contains(mousePos)) {//lamp
				gameState = "Door_End";
				finalLine = "As soon as you push the lamp, you hear a muffled \'hiss\' and you see tiles on the wall slide open. Soon after, fire begins to shoot out of the spaces, burning you alive.";
				curLine = "";
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			if(new Rectangle(185, 300, fireplace.getWidth(this), fireplace.getHeight(this)).contains(mousePos)) {//fireplace
				gameState = "Door_End";
				finalLine = "As soon as you push the fireplace, you hear a muffled \'hiss\' and you see tiles on the wall slide open. Soon after, fire begins to shoot out of the spaces, burning you alive.";
				curLine = "";
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			if(new Rectangle(25, 200, water.getWidth(this), water.getHeight(this)).contains(mousePos)) {//water
				gameState = "Door_End";
				finalLine = "As soon as you push the pot of water, you hear a muffled \'hiss\' and you see tiles on the wall slide open. Soon after, fire begins to shoot out of the spaces, burning you alive.";
				curLine = "";
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			break;
		case "Door_End":
			if(!updateLine() && window.contains(mousePos)) {
				sceneCount = 1;
				curLine = "";
				gameState = "End";
				ending = "Death";
			}
			break;
		case "Door_Escape":
			if(!updateLine() && window.contains(mousePos)) {
				if(sceneCount < DOOR_ESCAPE_MAX) {
					sceneCount++;
					curLine = "";
					
					if(sceneCount == 2){
						finalLine = "You did it! You escaped! However, you still don't know anything about who you are. But now you are free to explore and learn, and rebuild a new life.";
					}else if(sceneCount == 3){
						if(hasRobot) {
							finalLine = "As you start walking away from the cave, you think that you hear " + robotName + " say something from behind you. \"Fairwell, Crea---\", but the rest is lost in the wind.";
						}else {
							gameState = "End";
							ending = "Escape";
						}
					}
				}else {
					sceneCount = 1;
					gameState = "End";
					ending = "Escape";
				}
			}
			break;
		case "End": //conclusion screen
			//stops the time elapsed
			if(!hasTimeStopped) {
				time = Math.round((System.currentTimeMillis() - startTime) / 1000);
				hasTimeStopped = true;
			}
			
			//updates the rune circle alpha level, had changes the image
			endAlpha += endInc;
			if(endAlpha >= 1) {
				endInc *= -1;
				endAlpha = 1;
			}
			if(endAlpha <= 0) {
				endInc *= -1;
				endAlpha = 0;
				
				runeCircle = Toolkit.getDefaultToolkit().createImage("Assets/Rune Circles/circle" + (int)((Math.random() * 13) + 1) + ".png");
			}
			break;
		}
		
		//resets the mouse position to offscreen
		mousePos = new Point(-10,-10);
		repaint();
	}
	
	public boolean updateLine(){
		//gets the previous character
		char last = curLine.length() > 0 ? curLine.charAt(curLine.length()-1) : '@';
		//checks if the curLine is equal to the final line
		if(curLine.length() != finalLine.length()) {
			if(window.contains(mousePos)) {//fill line
				curLine = finalLine;
			}else{
				if(sentenceDelay == 25 || last != '.'){
					//add a character
					curLine = finalLine.substring(0, curLine.length()+1);
					sentenceDelay = 0;
				}else{
					//delays on '.'
					sentenceDelay++;
				}
			}
			return true;
		}else{
			return false;
		}
	}

	//for double buffering
	@Override
	public void update(Graphics g) {
		paint(g);
	}
	
	@Override
	public void paint(Graphics g){
		//clear screen and enable anti-aliasing
		offg.clearRect(0, 0, 650, 450);
		offg.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
		
		/*
		 * Basic code for scenes:
		 * draws an image depending on the scene and part
		 * draw the curLine in a box
		 * or draws a choice scene
		 * some graphics are conditional based on items or story line followed
		 */
		
		switch(gameState){
		case "Menu":
			offg.setColor(Color.WHITE);
			offg.fill(window);
			
			//creates an outline effect
			offg.setFont(new Font("Exocet", Font.BOLD, 65));
			offg.setColor(Color.RED);
			offg.drawString("Alchemical", 18, 58);
			offg.drawString("Alchemical", 18, 62);
			offg.drawString("Alchemical", 22, 58);
			offg.drawString("Alchemical", 22, 62);
			offg.drawString("Memories", 18, 123);
			offg.drawString("Memories", 18, 127);
			offg.drawString("Memories", 22, 123);
			offg.drawString("Memories", 22, 127);
			offg.setColor(Color.BLACK);
			offg.drawString("Alchemical", 20, 60);
			offg.drawString("Memories", 20, 125);
			
			//checks if the text is hovered over
			int offsetP = overPlay ? 30 : 0;
			int offsetR = overRules ? 30 : 0;
			int offsetC = overCredits ? 30 : 0;
			
			//creates outline effect
			offg.setFont(new Font("Exocet", Font.BOLD, 40));
			offg.setColor(Color.RED.darker());
			offg.drawString("Play", 49 + offsetP, 199);
			offg.drawString("Play", 49 + offsetP, 201);
			offg.drawString("Play", 51 + offsetP, 199);
			offg.drawString("Play", 51 + offsetP, 201);
			offg.drawString("Rules", 49 + offsetR, 249);
			offg.drawString("Rules", 49 + offsetR, 251);
			offg.drawString("Rules", 51 + offsetR, 249);
			offg.drawString("Rules", 51 + offsetR, 251);
			offg.drawString("Credits", 49 + offsetC, 299);
			offg.drawString("Credits", 49 + offsetC, 301);
			offg.drawString("Credits", 51 + offsetC, 299);
			offg.drawString("Credits", 51 + offsetC, 301);
			offg.setColor(Color.WHITE);
			offg.drawString("Play", 50 + offsetP, 200);
			offg.drawString("Rules", 50 + offsetR, 250);
			offg.drawString("Credits", 50 + offsetC, 300);
			
			//draws when mouse is hovered
			if(overPlay) {
				//draws symbols on sides
				offg.setColor(Color.BLACK);
				offg.drawOval(55, 180, 20, 20);
				offg.drawOval(200, 180, 20, 20);
				offg.drawLine(51, 190, 79, 190);
				offg.drawLine(65, 176, 65, 204);
				offg.drawLine(196, 190, 224, 190);
				offg.drawLine(210, 176, 210, 204);
			}
			if(overRules) {
				//draws symbols on sides
				offg.setColor(Color.BLACK);
				offg.drawOval(55, 230, 20, 20);
				offg.drawOval(228, 230, 20, 20);
				offg.drawLine(51, 240, 79, 240);
				offg.drawLine(65, 226, 65, 254);
				offg.drawLine(224, 240, 252, 240);
				offg.drawLine(238, 226, 238, 254);
				
				//paints the rule list
				drawChoiceBox(new Rectangle(300, 175, 335, 140), offg);
				offg.setFont(new Font("Exocet", Font.BOLD, 15));
				offg.setColor(Color.WHITE);
				offg.drawString("1. Have Fun!", 310, 200);
				offg.drawString("2. Always read the text", 310, 220);
				offg.drawString("(It may have something", 310, 240);
				offg.drawString("important!)", 310, 260);
				offg.drawString("3. Try to find every ending!", 310, 280);
				offg.drawString("4. Don't Cheat!", 310, 300);
			}
			if(overCredits) {
				//draws symbols on sides
				offg.setColor(Color.BLACK);
				offg.drawOval(55, 280, 20, 20);
				offg.drawOval(283, 280, 20, 20);
				offg.drawLine(51, 290, 79, 290);
				offg.drawLine(65, 276, 65, 304);
				offg.drawLine(279, 290, 307, 290);
				offg.drawLine(293, 276, 293, 304);
				
				drawChoiceBox(new Rectangle(50, 320, 300, 70), offg);
				offg.setFont(new Font("Exocet", Font.BOLD, 30));
				offg.setColor(Color.WHITE);
				offg.drawString("Created by:", 55, 350);
				offg.setColor(new Color(192, 192, 192));
				offg.drawString("Quinlan Sokol", 55, 380);
			}
			
			//updates and fills the background rects
			int inc = 5;
			offg.setColor(new Color(255, 223, 0, 35));
			for(Rectangle r : menuRects) {
				offg.fill(r);
				
				//updates the position
				r.x += inc;
				if(inc < 0) {
					if(r.x + r.width <= 0) {
						r.x = 650;
					}
				}else {
					if(r.x >= 650) {
						r.x = -r.width;
					}
				}
				
				//alternates movement
				inc *= -1;
			}
			break;
		case "Scene_1"://waking up
			offg.setColor(Color.BLACK);
			offg.fill(window);
			drawText(curLine, offg);
			break;
		case "Scene_1_Choice"://wake up or sleep
			offg.setColor(Color.BLACK);
			offg.fill(window);
			
			drawText(curLine, offg);
			drawChoiceBox(new Rectangle(165, 40, 300, 125), offg);
			drawChoiceBox(new Rectangle(165, 200, 300, 125), offg);
			
			offg.setFont(new Font("Exocet", Font.BOLD, TITLE_MAX_SIZE));
			offg.setColor(Color.WHITE);
			
			offg.drawString("Sleep", 230, 115);
			offg.drawString("Wake up", 180, 275);
			break;
		case "Scene_2"://choose to wake up
			if(sceneCount == 1){
				offg.setColor(Color.BLACK);
				offg.fill(window);
			}else if(sceneCount == 2){
				offg.setColor(Color.WHITE);
				offg.fill(window);
			}else if(sceneCount == 3){
				offg.drawImage(scene_2_3, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 4){
				offg.drawImage(scene_2_4, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 5){
				offg.drawImage(scene_2_5, 0, 0, window.width, window.height, this);
			}else if(sceneCount >= 6 && sceneCount <= 7){
				offg.drawImage(scene_2_6, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 8){
				offg.drawImage(scene_2_5, 0, 0, window.width, window.height, this);
			}
			
			drawText(curLine, offg);
			break;
		case "Scene_2_Choice":
			offg.drawImage(scene_2_5, 0, 0, window.width, window.height, this);
			
			drawText(curLine, offg);
			drawChoiceBox(new Rectangle(165, 40, 300, 125), offg);
			if(!seenMachine){
				drawChoiceBox(new Rectangle(15, 200, 300, 125), offg);
			}
			if(!seenJars) {
				drawChoiceBox(new Rectangle(335, 200, 300, 125), offg);
			}
			
			offg.setFont(new Font("Exocet", Font.BOLD, TITLE_MAX_SIZE));
			offg.setColor(Color.WHITE);
			
			offg.drawString("Leave", 220, 115);
			if(!seenMachine){
				offg.drawString("Examine", 30, 255);
				offg.drawString("Machine", 30, 300);
			}
			if(!seenJars) {
				offg.drawString("Examine", 350, 255);
				offg.drawString("Jars", 420, 300);
			}
			break;
		case "Scene_2_Jars":
			offg.drawImage(scene_2_jars, 0, 0, window.width, window.height, this);
			drawText(curLine, offg);
			break;
		case "Scene_2_Jars_Choice":
			offg.drawImage(scene_2_jars, 0, 0, window.width, window.height, this);
			
			drawText(curLine, offg);
			drawChoiceBox(new Rectangle(165, 40, 300, 125), offg);
			drawChoiceBox(new Rectangle(15, 200, 300, 125), offg);
			drawChoiceBox(new Rectangle(335, 200, 300, 125), offg);
			
			offg.setFont(new Font("Exocet", Font.BOLD, TITLE_MAX_SIZE));
			offg.setColor(Color.WHITE);
			
			offg.drawString("Oil", 270, 115);
			offg.drawString("Water", 60, 275);
			offg.drawString("Alcohol", 350, 275);
			break;
		case "Scene_2_Machine":
			if(sceneCount == 1) {
				offg.drawImage(scene_2_machine, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 2) {
				offg.drawImage(scene_2_6, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 3) {
				offg.drawImage(scene_2_machine, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 4) {
				offg.drawImage(scene_2_machine, 0, 0, window.width, window.height, this);
				drawChoiceBox(new Rectangle(140, 100, 360, 125), offg);
				drawChoiceBox(new Rectangle(10, 10, 85, 25), offg);
				
				offg.setColor(Color.WHITE);
				offg.setFont(new Font("Exocet", Font.BOLD, 78));
				offg.drawString(input, 150, 185);
				
				offg.setFont(new Font("Exocet", Font.BOLD, 25));
				offg.drawString("Exit", 15, 30);
				
				//if incorrect should shown, displays it with alpha
				if(incorrectCounter > 0) {
					offg.setColor(Color.RED);
					offg.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
					offg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) incorrectAlpha));
					offg.drawLine(140, 100, 500, 225);
					offg.drawLine(140, 225, 500, 100);
					offg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
					offg.setStroke(new BasicStroke(1));
					
					//updates alpha level and direction
					if(incorrectAlpha >= 1) {
						incorrectInc *= -1f;
					}
					incorrectAlpha += incorrectInc;
					incorrectCounter--;
				}else {
					//reset alpha variables
					incorrectAlpha = 0;
					incorrectInc = 0.04;
				}
			}else if(sceneCount == 5) {
				offg.drawImage(scene_2_machine, 0, 0, window.width, window.height, this);
			}
			
			drawText(curLine, offg);
			break;
		case "Scene_3":
			if(sceneCount == 1 || sceneCount == 2) {
				offg.drawImage(scene_3_1, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 3) {
				offg.drawImage(scene_3_3, 0, 0, window.width, window.height, this);
			}
			
			drawText(curLine, offg);
			break;
		case "Scene_3_Choice":
			offg.drawImage(scene_3_3, 0, 0, window.width, window.height, this);
			
			drawText(curLine, offg);
			drawChoiceBox(new Rectangle(165, 40, 300, 125), offg);
			drawChoiceBox(new Rectangle(165, 200, 300, 125), offg);
			
			offg.setFont(new Font("Exocet", Font.BOLD, TITLE_MAX_SIZE));
			offg.setColor(Color.WHITE);
			
			offg.drawString("Left", 240, 115);
			offg.drawString("Right", 225, 275);
			break;
		case "Scene_3_Room":
			if(sceneCount == 1){
				offg.drawImage(scene_3_room, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 2 || sceneCount == 3){
				offg.drawImage(scene_2_6, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 4){
				offg.drawImage(scene_3_room, 0, 0, window.width, window.height, this);
			}
			
			drawText(curLine, offg);
			break;
		case "Scene_3_Room_Choice":
			offg.drawImage(scene_3_room, 0, 0, window.width, window.height, this);
			
			//aligns box and text dpeneding on item
			String item = items.contains("Oil") ? "oil" : items.contains("Water") ? "water" : "alcohol";
			int boxX = items.contains("Oil") ? 120 : items.contains("Water") ? 85 : 60;
			int boxWidth = items.contains("Oil") ? 390 : items.contains("Water") ? 465 : 520;
			int textX = items.contains("Oil") ? 140 : items.contains("Water") ? 90 : 65;
			
			drawText(curLine, offg);
			drawChoiceBox(new Rectangle(110, 40, 420, 125), offg);
			drawChoiceBox(new Rectangle(boxX, 200, boxWidth, 125), offg);
			
			offg.setFont(new Font("Exocet", Font.BOLD, TITLE_MAX_SIZE));
			offg.setColor(Color.WHITE);
			
			offg.drawString("Break", 225, 90);
			offg.drawString("the boulder", 115, 140);
			offg.drawString("Throw " + item, textX, 250);
			offg.drawString("on the fire", 125, 300);
			break;
		case "Scene_3_Room_End":
			if(sceneCount == 1){
				offg.drawImage(scene_3_room, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 2){
				offg.setColor(Color.BLACK);
				offg.fill(window);
			}
			
			drawText(curLine, offg);
			break;
		case "Scene_3_Room_Boulder":
			if(sceneCount == 1){
				offg.drawImage(scene_3_room, 0, 0, window.width, window.height, this);
			}
			
			drawText(curLine, offg);
			break;
		case "Scene_3_Room_Fire":
			offg.drawImage(scene_3_room_fire, 0, 0, window.width, window.height, this);
			drawText(curLine, offg);
			break;
		case "Scene_3_Escape":
			if(sceneCount == 1) {
				offg.drawImage(scene_3_room_fire, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 2 || sceneCount == 3) {
				if(items.contains("Lantern")) {
					offg.drawImage(scene_3_escape, 0, 0, window.width, window.height, this);
				}else {
					offg.setColor(Color.BLACK);
					offg.fill(window);
				}
			}else if(sceneCount == 4) {
				offg.drawImage(scene_3_escape_world, 0, 0, window.width, window.height, this);
			}
			
			drawText(curLine, offg);
			break;
		case "Scene_4":
			offg.drawImage(scene_3_1, 0, 0, window.width, window.height, this);
			
			drawText(curLine, offg);
			break;
		case "Scene_4_Choice":
			offg.drawImage(scene_3_1, 0, 0, window.width, window.height, this);
			
			drawText(curLine, offg);
			drawChoiceBox(new Rectangle(165, 40, 300, 125), offg);
			drawChoiceBox(new Rectangle(165, 200, 300, 125), offg);
			
			offg.setFont(new Font("Exocet", Font.BOLD, TITLE_MAX_SIZE));
			offg.setColor(Color.WHITE);
			
			offg.drawString("Examine", 180, 115);
			offg.drawString("Leave", 220, 275);
			break;
		case "Scene_4_Tank":
			if(sceneCount == 1){
				offg.drawImage(scene_4_tank, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 2){
				offg.drawImage(scene_2_6, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 3){
				offg.drawImage(scene_4_tank, 0, 0, window.width, window.height, this);
			}
			
			drawText(curLine, offg);
			break;
		case "Scene_5":
			if(sceneCount == 1){
				offg.drawImage(scene_5, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 2 || sceneCount == 3){
				offg.drawImage(scene_5_workshop, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 4){
				offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 5){
				offg.drawImage(scene_2_6, 0, 0, window.width, window.height, this);
			}
			
			drawText(curLine, offg);
			break;
		case "Scene_5_Scroll_Choice":
			offg.drawImage(scene_2_6, 0, 0, window.width, window.height, this);
			
			drawText(curLine, offg);
			drawChoiceBox(new Rectangle(165, 40, 300, 125), offg);
			drawChoiceBox(new Rectangle(165, 200, 300, 125), offg);
			
			offg.setFont(new Font("Exocet", Font.BOLD, TITLE_MAX_SIZE));
			offg.setColor(Color.WHITE);
			
			offg.drawString("Yes", 260, 115);
			offg.drawString("No", 275, 275);
			break;
		case "Scene_5_Scroll":
			offg.drawImage(scene_2_6, 0, 0, window.width, window.height, this);
			drawText(curLine, offg);
			break;
		case "Scene_5_Robot":
			offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			drawText(curLine, offg);
			break;
		case "Scene_5_Robot_Choice":
			offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			
			String item2 = items.contains("Oil") ? "oil" : items.contains("Water") ? "water" : "alcohol";
			int boxX2 = items.contains("Oil") ? 165 : items.contains("Water") ? 115 : 90;
			int boxWidth2 = items.contains("Oil") ? 300 : items.contains("Water") ? 400 : 450;
			int textX2 = items.contains("Oil") ? 195 : items.contains("Water") ? 140 : 110;
			
			drawText(curLine, offg);
			
			drawChoiceBox(new Rectangle(165, 40, 300, 125), offg);
			drawChoiceBox(new Rectangle(boxX2, 200, boxWidth2, 125), offg);
			
			offg.setFont(new Font("Exocet", Font.BOLD, TITLE_MAX_SIZE));
			offg.setColor(Color.WHITE);
			
			offg.drawString("Leave", 220, 115);
			offg.drawString("Use " + item2, textX2, 275);
			break;
		case "Robot_Oil":
			offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			drawText(curLine, offg);
			break;
		case "Scene_5_Explosion":
			offg.drawImage(scene_5_explosion, 0, 0, window.width, window.height, this);
			drawText(curLine, offg);
			break;
		case "Robot_Puzzle":
			offg.setColor(Color.GRAY);
			offg.fill(window);
			
			offg.setColor(Color.WHITE);
			offg.fillRect(75, 75, 500, 300);
			
			offg.setColor(new Color(30, 144, 255));
			offg.fillRect(75, 125, 50, 20);
			
			drawChoiceBox(new Rectangle(10, 10, 85, 25), offg);
			drawChoiceBox(new Rectangle(85, 390, 485, 50), offg);
			
			offg.setColor(Color.WHITE);
			offg.setFont(new Font("Exocet", Font.BOLD, 25));
			offg.drawString("Exit", 15, 30);
			
			//checks if the puzzle has been solved
			offg.setFont(new Font("Exocet", Font.BOLD, 20));
			if(solveDelay) {
				offg.drawString("You connected the pipes!", 150, 420);
			}else {
				offg.drawString("Click on the pipe tiles to change", 90, 410);
				offg.drawString("them until they all connect!", 110, 430);
			}
			
			//lightning symbol
			Polygon power = new Polygon();
			power.addPoint(59,  80);
			power.addPoint(10, 150);
			power.addPoint(30, 147);
			power.addPoint(14, 190);
			power.addPoint(50, 134);
			power.addPoint(30, 137);
			
			//draws the setup, permanent pipes and outlines
			if(!pipes.get(4).isPowered) {
				offg.setColor(Color.LIGHT_GRAY);
			}else{
				offg.setColor(new Color(30, 144, 255));
			}
			offg.fillRect(525, 305, 50, 20);
			
			offg.setColor(Color.BLACK);
			offg.drawLine(125, 75, 125, 375);
			offg.drawLine(205, 75, 205, 375);
			offg.drawLine(285, 75, 285, 375);
			offg.drawLine(365, 75, 365, 375);
			offg.drawLine(445, 75, 445, 375);
			offg.drawLine(525, 75, 525, 375);
			
			offg.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			offg.drawLine(75, 125, 125, 125);
			offg.drawLine(75, 145, 125, 145);
			offg.drawLine(525, 305, 575, 305);
			offg.drawLine(525, 325, 575, 325);
			
			for(Pipe p : pipes) {
				p.draw(offg);
			}
			
			offg.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			offg.setColor(Color.YELLOW);
			offg.fill(power);
			offg.setColor(Color.BLACK);
			offg.draw(power);
			
			offg.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			offg.setColor(new Color(57, 255, 20));
			offg.drawRect(75, 75, 500, 300);
			offg.setStroke(new BasicStroke(1));
			break;
		case "Robot_Talk_1":
			offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			drawText(curLine, offg);
			break;
		case "Robot_Question_1":
			offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			
			drawText(curLine, offg);
			drawChoiceBox(new Rectangle(15, 40, 300, 125), offg);
			drawChoiceBox(new Rectangle(335, 40, 300, 125), offg);
			drawChoiceBox(new Rectangle(15, 200, 300, 125), offg);
			drawChoiceBox(new Rectangle(335, 200, 300, 125), offg);
			
			offg.setFont(new Font("Exocet", Font.BOLD, TITLE_MAX_SIZE));
			offg.setColor(Color.WHITE);
			
			offg.drawString("Wall-E", 50, 115);
			offg.drawString("ALC4", 415, 115);
			offg.drawString("Scrap", 75, 275);
			offg.drawString("Dio", 435, 275);
			break;
		case "Robot_Talk_2":
			offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			drawText(curLine, offg);
			break;
		case "Robot_Question_2":
			offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			
			drawText(curLine, offg);
			drawChoiceBox(new Rectangle(165, 40, 300, 125), offg);
			drawChoiceBox(new Rectangle(165, 200, 300, 125), offg);
			
			offg.setFont(new Font("Exocet", Font.BOLD, TITLE_MAX_SIZE));
			offg.setColor(Color.WHITE);
			
			offg.drawString("Truth", 215, 115);
			offg.drawString("Lie", 270, 275);
			break;
		case "Robot_Talk_3":
			offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			drawText(curLine, offg);
			
			//draws 'remember' box
			offg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, boxAlpha));
			
			drawChoiceBox(new Rectangle(165, 5, 320, 30), offg);
			offg.setFont(new Font("Exocet", Font.BOLD, 17));
			offg.setColor(Color.WHITE);
			offg.drawString(robotName + " will remember this", 325 - (offg.getFontMetrics().stringWidth(robotName + " will remember this") / 2), 25);
			
			offg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			break;
		case "Robot_Question_3":
			offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			
			drawText(curLine, offg);
			drawChoiceBox(new Rectangle(165, 40, 300, 125), offg);
			drawChoiceBox(new Rectangle(165, 200, 300, 125), offg);
			
			offg.setFont(new Font("Exocet", Font.BOLD, TITLE_MAX_SIZE));
			offg.setColor(Color.WHITE);
			
			offg.drawString("Slave", 225, 115);
			offg.drawString("Helper", 215, 275);
			break;
		case "Robot_Talk_4":
			offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			drawText(curLine, offg);
			
			//draws 'remember' box
			offg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, boxAlpha));
			
			drawChoiceBox(new Rectangle(165, 5, 320, 30), offg);
			offg.setFont(new Font("Exocet", Font.BOLD, 17));
			offg.setColor(Color.WHITE);
			offg.drawString(robotName + " will remember this", 325 - (offg.getFontMetrics().stringWidth(robotName + " will remember this") / 2), 25);
			
			offg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			break;
		case "Robot_Question_4":
			offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			
			drawText(curLine, offg);
			drawChoiceBox(new Rectangle(165, 40, 300, 125), offg);
			drawChoiceBox(new Rectangle(165, 200, 300, 125), offg);
			
			offg.setFont(new Font("Exocet", Font.BOLD, TITLE_MAX_SIZE));
			offg.setColor(Color.WHITE);
			
			offg.drawString("Escape", 210, 115);
			offg.drawString("Stay", 235, 275);
			break;
		case "Robot_Talk_5":
			offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			drawText(curLine, offg);
			break;
		case "Robot_End":
			if(sceneCount == 1 || sceneCount == 2){
				if(!atDoor) {
					offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
				}else {
					offg.drawImage(scene_6, 0, 0, window.width, window.height, this);
				}
			}else if(sceneCount == 3){
				offg.drawImage(cave_flood, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 4){
				offg.setColor(Color.BLACK);
				offg.fill(window);
			}
			
			drawText(curLine, offg);
			break;
		case "Stay_End":
			if(sceneCount == 1 || sceneCount == 2){
				offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 3){
				offg.drawImage(scene_2_jars, 0, 0, window.width, window.height, this);
			}
			
			drawText(curLine, offg);
			break;
		case "Scene_6":
			if(sceneCount == 1 & didSleep) {
				offg.setColor(Color.BLACK);
				offg.fill(window);
			}else {
				offg.drawImage(scene_6, 0, 0, window.width, window.height, this);
			}
			drawText(curLine, offg);
			break;
		case "Door_Puzzle":
			offg.drawImage(door_writing, 0, 0, window.width, window.height, this);
			
			//draws images
			offg.drawImage(manImage, 400, 200, this);
			offg.drawImage(match, 405, 200, this);
			offg.drawImage(lamp, 200, 80, this);
			offg.drawImage(fireplace, 185, 300, this);
			offg.drawImage(water, 25, 200, this);
			
			drawChoiceBox(new Rectangle(10, 10, 630, 50), offg);
			
			//draws out the question
			offg.setFont(new Font("Exocet", Font.BOLD, 15));
			offg.setColor(Color.WHITE);
			offg.drawString("A man has only one match, but he must light an oil", 15, 25);
			offg.drawString("lamp, heat some water, and start a fire. In order to", 15, 40);
			offg.drawString("complete these tasks, what should he light first?", 15, 55);
			
			break;
		case "Door_End":
			offg.drawImage(scene_5_explosion, 0, 0, window.width, window.height, this);
			drawText(curLine, offg);
			break;
		case "Door_Escape":
			if(sceneCount == 1) {
				offg.drawImage(scene_6, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 2) {
				offg.drawImage(scene_3_escape_world, 0, 0, window.width, window.height, this);
			}else if(sceneCount == 3) {
				offg.drawImage(scene_5_robot, 0, 0, window.width, window.height, this);
			}
			
			drawText(curLine, offg);
			break;
		case "End":
			offg.setColor(Color.BLACK);
			offg.fill(window);
			
			//draws the rune circle with alpha level
			offg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, endAlpha));
			offg.drawImage(runeCircle, 350, 100, 250, 250, this);
			offg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			
			offg.setFont(new Font("Exocet", Font.BOLD, 80));
			offg.setColor(Color.RED);
			
			//creates outline effect
			offg.drawString("The End", 22, 80);
			offg.drawString("The End", 20, 78);
			offg.drawString("The End", 18, 80);
			offg.drawString("The End", 20, 82);
			
			offg.setColor(Color.WHITE);
			offg.drawString("The End", 20, 80);
			
			offg.setFont(new Font("Exocet", Font.BOLD, 30));
			offg.setColor(Color.WHITE);
			
			//draws the elapsed time
			offg.drawString("Time Played: ", 15, 150);
			offg.drawString(time + " seconds", 80, 180);
			offg.drawString("Ending: " + ending, 15, 250);
			
			offg.drawString("Thank You for playing!", 15, 400);
			break;
		}
		
		//draw symbols on click
		ArrayList<Symbol> toRemove = new ArrayList<Symbol>();
		for(Symbol s : symbols) {
			if(s.time <= 0) {
				toRemove.add(s);
			}else{
				//draws the symbol and decreases the alpha
				offg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, s.alpha));
				offg.drawImage(s.image, s.pos.x - (s.image.getWidth(this) / 2), s.pos.y - (s.image.getHeight(this) / 2), this);
				offg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				s.alpha -= 0.01f;
				s.time--;
			}
		}
		symbols.removeAll(toRemove);
		
		g.drawImage(offscreen, 0, 0, this);
	}
	
	public void drawChoiceBox(Rectangle bounds, Graphics2D offg){
		offg.setColor(new Color(57, 255, 20));
		offg.draw(bounds);
		offg.drawRect(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
		
		offg.setColor(new Color(0, 0, 0, 150));
		offg.fillRect(bounds.x + 2, bounds.y + 2, bounds.width - 3, bounds.height - 3);
	}
	
	public void drawText(String text, Graphics2D offg){
		offg.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
		//gets height of displayed text
		int height = offg.getFontMetrics().getHeight();
		//array for seperated lines
		ArrayList<String> lines = new ArrayList<String>();
		
		offg.setColor(new Color(57, 255, 20));//neon green
		offg.drawRect(0, 370, 649, 79);//border
		offg.setColor(new Color(0, 0, 0, 100));//alpha black
		offg.fillRect(1, 371, 648, 78);//inside box
		
		if(text.length() > 0) {
			//Splitting the text up into chunks
			int i = 0;
			while(true) {
				String str = text.substring(0, i+1);
				
				if(i == text.length()-1) {//if all that is left
					lines.add(str);
					break;
				}else if(offg.getFontMetrics().stringWidth(str) >= 600){//if the length is equal to the max
					lines.add(str.substring(0, str.lastIndexOf(" ")));
					i = -1;
					text = text.substring(str.lastIndexOf(" "), text.length());
				}
				
				i++;
			}
			
			//drawing the text
			offg.setColor(Color.WHITE);
			int y = 374 + height;
			
			for(String str : lines) {
				offg.drawString(str.trim(), 25, y);
				y += height + 2;
			}
		}
	}
	
	public Image getClickImage() {
		int num = (int) (Math.random() * 16) + 1;
		//loads random symbol
		return Toolkit.getDefaultToolkit().createImage("Assets/Symbols/symbol" + num + ".png");
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == 1){
			//sets mouse position
			mousePos = e.getPoint();
			if(!gameState.equals("Menu")) {
				//creates symbol on click
				symbols.add(new Symbol(getClickImage(), mousePos, 100));
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {
		//if in the machine puzzle
		if(gameState.equals("Scene_2_Machine") && sceneCount == 4 && incorrectCounter == 0){
			if(Character.isDigit(e.getKeyChar())){//if character is a number, add to answer
				if(input.length() < 5){//max of 4
					input += e.getKeyChar();
				}
			}else if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE){//delete functionality
				if(input.length() > 0){
					input = input.substring(0, input.length()-1);
				}
			}else if(e.getKeyChar() == KeyEvent.VK_ENTER){ //answer is 120
				if(input.equals("120")){//answers correctly
					input = "";
					sceneCount++;
					curLine = "";
					finalLine = "You answered correctly! As a result, your knowledge of alchemy has increased.";
					knowledge++;
				}else {
					incorrectCounter = 50;
				}
			}
		}
	}
}

class Pipe{
	//stores the 4 possible tiles
	ArrayList<String> arr;
	//variable for current tile
	int index = 0;
	int startX;
	int endX;
	boolean isPowered = false;
	public Pipe(int x1, int x2, ArrayList<String> a) { //each slot is increments of 30
		startX = x1;
		endX = x2;
		arr = a;
	}
	
	public void rotate() {//switches the pipe on click
		index = (index == 3) ? 0 : index + 1;
	}
	
	public void draw(Graphics2D offg) {
		String str = arr.get(index);
		//get start and end of pipe
		int startY = Integer.parseInt(str.split("->")[0]) + 75;
		int endY = Integer.parseInt(str.split("->")[1]) + 75;
		
		//if straight
		if(startY == endY) {
			//colors blue if powered
			if(isPowered) {
				offg.setColor(new Color(30, 144, 255));
			}else {
				offg.setColor(Color.LIGHT_GRAY);
			}
			offg.fillRect(startX, startY - 10, 80, 20);
			
			offg.setColor(Color.BLACK);
			offg.drawLine(startX, startY + 10, endX, startY + 10);
			offg.drawLine(startX, startY - 10, endX, startY - 10);
		}
		else {
			//colors blue if powered
			if(isPowered) {
				offg.setColor(new Color(30, 144, 255));
			}else {
				offg.setColor(Color.LIGHT_GRAY);
			}
			
			//conditional drawing of the pipe + outline
			//matters if the end is above or below the start
			offg.fillRect(startX, startY - 10, 30, 20);
			offg.fillRect(endX - 30, endY - 10, 30, 20);
			offg.fillRect(startX + 30, (startY > endY ? endY : startY) - 10, 20, Math.abs(startY - endY) + 20);
			
			offg.setColor(Color.BLACK);
			offg.drawLine(startX, startY - 10, startX + 30 + (startY < endY ? 20 : 0), startY - 10);
			offg.drawLine(startX, startY + 10, startX + 30 + (startY > endY ? 20 : 0), startY + 10);
			
			offg.drawLine(endX - 30 - (startY > endY ? 20 : 0), endY - 10, endX, endY - 10);
			offg.drawLine(endX - 30 - (startY < endY ? 20 : 0), endY + 10, endX, endY + 10);
			
			offg.drawLine(startX + 30, startY + (startY < endY ? 10 : -10), startX + 30, endY + (startY < endY ? 10 : -10));
			offg.drawLine(endX - 30, startY + (startY > endY ? 10 : -10), endX - 30, endY + (startY > endY ? 10 : -10));
		}
	}
}

//class for click and trail images
//acts as a data structure stores needed info easier
class Symbol{
	Image image;
	int time;
	Point pos;
	float alpha;
	public Symbol(Image i, Point p, int t) {
		image = i;
		pos = p;
		time = t;
		alpha = 1f;
	}
}