import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class SpaceAttack extends PApplet {

int white  = color(255, 255, 255);
int black  = color(0, 0, 0);
int green  = color(0, 200, 0);
int yellow = color(200, 200, 0);
int red    = color(200, 0, 0);

int screenC = black;
int playerC = white;
int bulletC = white;
int targetC = white; //04
int textC = green;

int playerSize = 20;

int sizeX; // screen X size (see setup() for actual values)
int sizeY; // screen Y size (see setup() for actual values)

int playerX;
int playerY;

int maxBullet = 5;        // max number of bullets
int bulletX[];
int bulletY[];
int bulletActive[];       // 0 stop, 1 active
int bulletSpeed = 7;      // max speed of all bullets

int maxTarget = 5;        // max number of targets
int targetX[];
int targetY[];
int targetActive[];       // 0 stop, 1 active
int targetDelayCnt[];     // delay counter 
int targetDelayMin =  50; // min delay 
int targetDelayMax = 200; // max delay 
int targetSpeed = 3;      // max speed of all targets

int score = 0;
int life = 3;

int playerDirection = 0; // direction: -1 left, 0 stop, 1 right

int gameMode = 1; // 0 = game over, 1 = intro, 2 = init game, 3 = in game, 4 = pause, 5 = end game, 6 = enter highscore, 7 = finalize, 8-9 = not used 

public void setup()
{
  
  sizeX = width;
  sizeY = height;
  textSize(36);
  initPlayer();
  //initialize target... arrays
  targetX = new int[maxTarget];
  targetY = new int[maxTarget];
  targetActive = new int[maxTarget];
  targetDelayCnt = new int[maxTarget];
  //initialize bullet... arrays
  bulletX = new int[maxBullet];
  bulletY = new int[maxBullet];
  bulletActive = new int[maxBullet];
}

public void draw()
{
  background(screenC); // erase screen


  switch (gameMode)
  {
    case 1: // intro
      // put the cannon back to start
      initPlayer();
      
      // stop the bullets // 08
      for (int i=0; i<maxBullet; i++) // 08
      { // 08
        bulletActive[i] = 0; // 08
      } // 08
      
      // reset all targets
      for (int i=0; i<maxTarget; i++)
      {
        targetActive[i] = 0;
      }
      
      // show intro-text
      fill(red);
      textSize(50);
      text("Space Attack", 100, 200);
      fill(green);
      textSize(20);
      text("V1.7 (c) AcB of BITS 07/2017", 110, 240);  // 04
      fill(yellow);
      textSize(36);
      text("Game", 200, 300);
      text("Over", 210, 350);
      fill(green);  // 04
      textSize(20);  // 04
      text("Press G to start", 180, 390);  // 04
      textSize(15);  // 04
      text("A / D = left/right, X = fire", 155, 420);  // 04
    break;  

    case 2: //init
      score = 0;
      life = 3;
      // set initial delay of targets
      targetDelayCnt[1] = 10; // first one with fixed delay  // 07
      for (int i=1; i<maxTarget; i++)  // all others with random
      {
        targetDelayCnt[i] = PApplet.parseInt(random(targetDelayMin, targetDelayMax));  // generate delay
      }
      gameMode = 3;  // switch to inGame mode
    break;  

    case 3: // inGame - we do all active gaming stuff here
      // handle player movements
      if (playerDirection == -1) // left
      {
        if (playerX > (0 + playerSize) )
        {
          playerX = playerX - 5;
        }
      }
    
      if (playerDirection == 1) // right
      {
        if (playerX + playerSize < sizeY)
        {
          playerX = playerX + 5;
        }
      }
      
      // handle new target creation
      // for each target, we have to check if it is already active. If so, do nothing here (moving the target is done at another place). 
      // if inactive, see if the individual delay per target is up. If not, decrease it. 
      // if so, set this target to active, define start position and new delay 
      for (int i=0; i<maxTarget; i++)  // all others with random
      {
        if (targetActive[i] == 0) // target is not active - see if ne need to launch it  // 07
        { // 04
          if (targetDelayCnt[i] <= 0) // target delay time up - launch it  // 07
          {
            targetActive[i] = 1;
            targetDelayCnt[i] = PApplet.parseInt(random(targetDelayMin, targetDelayMax));  // generate delay for next time
            targetX[i] = PApplet.parseInt(random(sizeX)); // define random x position 
            targetY[i] = 0;  // set it to the top of the screen
          }
          else
          {
            targetDelayCnt[i]--;  // 07
          }
        } // 04    
      
      }
    break;  

    case 4: //pause
      text("P A U S E", sizeX/2 - 80, sizeY/2 - 18);
    break;  

  }

  // show the objects on screen
  stroke(playerC);
  fill(playerC);

  // show the player
  line(playerX, playerY, playerX + playerSize/2, playerY + playerSize); 
  line(playerX, playerY, playerX - playerSize/2, playerY + playerSize); 
  line(playerX - playerSize/2, playerY + playerSize, playerX + playerSize/2, playerY + playerSize); 

  // show the targets by checking each  // 07
  for (int i=0; i<maxTarget; i++) // 07
  {  // 07
    if (targetActive[i] == 1) // 07
    { // 04
      fill(black);
      stroke(targetC); // 04
      ellipse(targetX[i], targetY[i], playerSize, playerSize); // 07
      // no - move target, but only if in game mode
      if (gameMode == 3) // 04
      { // 04
        targetY[i] = targetY[i] + targetSpeed; // move target down  // 07
        // check for collision with player  //05
        if ( ( (targetX[i] >= playerX - playerSize+2) && (targetX[i] <= playerX + playerSize-2 ) ) && (targetY[i] >= playerY) )  //07
        {  //05
          // we got hit by the target!  //05
          // delay(100);
          life--;  // reduce life  //05
          initPlayer();  // set player back to start  //05
          targetActive[i] = 0;  // switch off target  //07
        }  //05
      } // 04
  
      // see if we have reached the bottom
      if (targetY[i] >= sizeY) // 07
      { // 04
          // stop target
          targetActive[i] = 0; // 07
      } // 04
    } // 04
  }
  
  // show the bullets by checking each // 08
  for (int i=0; i<maxBullet; i++) // 08
  {  // 08
    if (bulletActive[i] == 1)
    {
      stroke(bulletC);
      line(bulletX[i], bulletY[i], bulletX[i], bulletY[i] - playerSize/2);  // display the bullet (a straight line) // 08
      if (gameMode == 3)
      {
        bulletY[i] = bulletY[i] - bulletSpeed;  // move it up each cycle // 08
        
        // see if we hit the target - check each  // 07
        for (int j=0; j<maxTarget; j++)  // all others with random  // 07
        {  // 07
          if ( ( (targetX[j] >= bulletX[i] - playerSize+2) && (targetX[j] <= bulletX[i] + playerSize-2 ) ) && (targetY[j] >= bulletY[i]) && (targetActive[j] == 1) )  // 08
          {  // 06
            // we hit the target!  // 06
            // delay(100); // 06
            score++;  // add score  // 06
            targetActive[j] = 0;  // switch off target  // 06
            bulletActive[i] = 0;  // switch off bullet  // 08
          }  //06
        }  // 07
      }
      
      // see if we reached the top
      if (bulletY[i] <= 0) // 08
      {
        bulletActive[i] = 0; // 08
      }
    }
  }
 
  // display the score
  fill(textC);
  textSize(25);
  text("Score:", 20, 50); 
  text(score, 150, 50); 
  textSize(36);

  // display the lives
  fill(textC);
  textSize(25);
  text("Life:", sizeX - 120, 50); 
  text(life, sizeX - 40, 50); 
  textSize(36);

  // display debug text 1
  fill(textC);
  textSize(25);
  // text("Life:", sizeX - 120, 50); 
//  text(targetDelayCnt, sizeX - 300, 50); 
//  text(playerX, sizeX - 300, 50); 
  textSize(36);
  
  // display debug text 2
  fill(textC);
  textSize(25);
  // text("Life:", sizeX - 120, 50); 
//  text(targetDelayCnt, sizeX - 300, 50); 
//  text(targetX, sizeX - 220, 50); 
  textSize(36);
  
  // see if player ran out of lives : game over  // 05
  if (life == 0)  // 05
  {
    gameMode = 1; // might need to change to another gameMode later  // 05
  }  // 05
}


// check if a key has been pressed
public void keyPressed(KeyEvent e)
{
  println(keyCode);
  switch (keyCode)
  {
    case 37: // left
      playerDirection = -1;
    break;
    
    case 39: // right
      playerDirection = 1;
    break;
    
    case 81:
      if (gameMode == 3)
      {
        gameMode = 1;
      }
    break;

    case 32:  // fire 1
      // we handle firing of multiple bullets here - we have a limited number of bullets, which can be moving simultaneously.
      // for each fire request, we look for a free bullet, and launch it if available. If there is no free one, we can' launch any.
      for (int i = 0; i<maxBullet; i++) // go thru all available bullets we have  // 08
      {  // 08
        if (bulletActive[i] == 0)  // is this current bullet available (i.e. not noving) ?
        {
          bulletActive[i] = 1;     // yes - launch it
          bulletX[i] = playerX;    // get current player position as start
          bulletY[i] = playerY;
          i = maxBullet;           // and quit searching for next bullet, since we're done for now.
        }
      }
    break;

    case 'e':  // launch target 1 (test only!)
      if (targetActive[1] == 0)
      {
        targetActive[1] = 1;
        targetX[1] = PApplet.parseInt(random(10,490));
//        targetX = 350;
        targetY[1] = 350;
      }
    break;

    case '-':  // reduce life (test only!)
      if (gameMode == 3)
      {
        life--;
      }
    break;

    case '+':  // reduce life (test only!)
      if (gameMode == 3)
      {
        life++;
      }
    break;

    case 71: // go = start game
      if (gameMode == 1)
      {
        gameMode = 2;
      }
    break;

    case 80: // pause
      if (gameMode == 3)
      {
        gameMode = 4;
      }
      else if (gameMode == 4)
      {
        gameMode = 3;
      }
    break;
  }
}


// check if a key has been released
public void keyReleased(KeyEvent e)
{
  switch (keyCode)
  { 
    case 37:
      playerDirection = 0;
    break;
    
    case 39:
      playerDirection = 0;
    break;

    case 83:  // fire
      bulletActive[1] = 0;
    break;
  }
}

public void initPlayer()
{
  playerX = sizeX / 2;
  playerY = sizeY - 10 - playerSize;
}
  public void settings() {  size(500, 500); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "SpaceAttack" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
