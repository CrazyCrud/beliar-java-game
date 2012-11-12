/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Units;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import Pathfinding.Pathfinder;
import Pathfinding.Node;
import Map.MapController;
import com.jme3.math.Vector3f;
import java.util.List;
/**
 *
 * @author andministrator
 */
public class WalkControl extends AbstractControl{

    private Pathfinder pathFinder;
    private List<Node> list_path;
    private Node node_target, node_start, node_current;
    private float float_timer;
    private int int_direction;
    private boolean bool_isMoving;
    
    public WalkControl(){
        pathFinder = new Pathfinder();
        bool_isMoving = false;
        float_timer = 0.0f;
        int_direction = GameObjectValues.NO_DIRECTION_CHANGE;
    }
    
    public void setSpeed(float speed){
        spatial.setUserData(GameObjectValues.SPEED_KEY, speed);
    }
    
    public float getSpeed(){
        return spatial.getUserData(GameObjectValues.SPEED_KEY);
    }
    
    protected void findPath(int xPos, int zPos){
        node_start = MapController.getNode(spatial.getControl(GameObjectControl.class).getPosX(), 
                spatial.getControl(GameObjectControl.class).getPosY());
        node_current = node_start;
        node_target = MapController.getNode(xPos, zPos);
        list_path = (List)pathFinder.search(node_start, node_target);
        if(list_path != null){
            setMoving(true);
        }else{
            setMoving(false);
        }
    }
    
    protected boolean isMoving(){
        return bool_isMoving;
    }
    
    protected void setMoving(boolean isMoving){
        bool_isMoving = isMoving;
    }
    
    protected void setDirection(int direction){
        int_direction = direction;
        spatial.setUserData(GameObjectValues.ORIENTATION_KEY, int_direction);
    }
    
    protected int getDirection(){
        return int_direction;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(spatial == null){
            return;
        }
        if(isEnabled()){
            if(isMoving()){
                setOrientation();
                if(isTimeToMove()){
                    setPosition();
                    resetTimer();
                }else{
                    updateTimer(tpf);
                }
            } 
        }
    }
    
    private boolean isTimeToMove(){
        return float_timer > GameObjectValues.MOVEMENT_PERIOD ? true : false;
    }
    
    private void updateTimer(float timeToUpdate){
        float_timer += (timeToUpdate + getSpeed()); 
    }
    
    private void resetTimer(){
        float_timer = 0.0f;
    }    
    
    private void setOrientation(){
        if(list_path.isEmpty()){
            return;
        }
        Node nextNode = list_path.get(0);
        int newXPos = nextNode.getXPos();
        int newZPos = nextNode.getYPos();
        spatial.lookAt(new Vector3f((float)newXPos, (float)GameObjectValues.Y_POSITION, (float)newZPos), 
                GameObjectValues.UP_VECTOR);
    }
    
    private void setPosition(){
        if(list_path.isEmpty()){
            setMoving(false);
            clearMovement();
            return;
        }
        
        Node nextNode = list_path.get(0);
        int newXPos = nextNode.getXPos();
        int newZPos = nextNode.getYPos();
        node_current = nextNode;
        spatial.getControl(GameObjectControl.class).setPosX(newXPos);
        spatial.getControl(GameObjectControl.class).setPosY(newZPos);
        spatial.getControl(GameObjectControl.class).setLocation(new Vector3f(newXPos, 
                GameObjectValues.Y_POSITION, newZPos));
        list_path.remove(nextNode);
    }
    
    private void computeDirection(int xPos, int zPos, int newXPos, int newZPos){
        if(newXPos < xPos){
            if(newZPos < zPos){
                setDirection(GameObjectValues.NORTH_WEST);
            }else if(newZPos > zPos){
                setDirection(GameObjectValues.SOUTH_WEST);
            }else{
                setDirection(GameObjectValues.WEST);
            }
        }else if(newXPos > xPos){
            if(newZPos < zPos){
                setDirection(GameObjectValues.NORTH_EAST);
            }else if(newZPos > zPos){
                setDirection(GameObjectValues.SOUTH_EAST);
            }else{
                setDirection(GameObjectValues.EAST);
            }
        }else{
            if(newZPos < zPos){
                setDirection(GameObjectValues.NORTH);
            }else if(newZPos > zPos){
                setDirection(GameObjectValues.SOUTH);
            }else{
                setDirection(GameObjectValues.NO_DIRECTION_CHANGE);
            }
        }
    }
    
    private void clearMovement(){
        clearPath();
        clearNodes();
    }
    
    private void clearPath(){
        list_path.clear();
    }
    
    private void clearNodes(){
        node_start = node_target = null;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        
    }

    public Control cloneForSpatial(Spatial spatial) {
        final WalkControl clone = new WalkControl();
        clone.setSpatial(spatial);
        return clone;
    }    
}
