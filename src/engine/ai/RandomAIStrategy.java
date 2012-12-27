package engine.ai;

import engine.FaceDirection;
import engine.Game;
import engine.entity.AbstractLivingEntity;

import java.util.Random;

public class RandomAIStrategy implements IAIStrategy {
	
	private Game game;
	
	private AbstractLivingEntity entity;

	private long lastMoved = System.currentTimeMillis();
	
	private long moveDelay = 1000;
	
	private boolean moving = false;
	
	private int distMoved = 0;
	
	private int moveSpeed = 2;
	
	private int movingDir = -1;
	
	Random r = new Random();
	
	public RandomAIStrategy(Game game, AbstractLivingEntity entity, int moveDelay) {
		this.game = game;
		this.entity = entity;
		this.moveDelay = moveDelay;
	}
	
	@Override
	public void handle() {
		if(moving) {
			switch(movingDir) {
				case 0:	// North
					entity.locate(entity.x(), entity.y() - moveSpeed);
					break;
				case 1:	// East
					entity.locate(entity.x() + moveSpeed, entity.y());
					break;
				case 2:	// South
					entity.locate(entity.x(), entity.y() + moveSpeed);
					break;
				case 3:	// West
					entity.locate(entity.x() - moveSpeed, entity.y());
					break;
			}
			distMoved += moveSpeed;
			if(distMoved >= 16) { // moved 1 square
				moving = false;
			}
		} else if(System.currentTimeMillis() - lastMoved > moveDelay) { // move
			lastMoved = System.currentTimeMillis();
			movingDir = r.nextInt(4);
			if(movingDir < 4) {
				distMoved = 0;
				moving = true;
				int x; int y;
				switch(movingDir) {
					case 0:	// North
						x = (int)entity.x() / game.map().tileWidth();
						y = (int)(entity.y() - game.map().tileHeight()) / game.map().tileHeight();
						if(!game.map().isWalkable(x, y)) {
							moving = false;
						} else {
							entity.face(FaceDirection.NORTH);
						}
						break;
					case 1:	// East
						x = (int)(entity.x() + game.map().tileWidth()) / game.map().tileWidth();
						y = (int)entity.y() / game.map().tileHeight();
						if(!game.map().isWalkable(x, y)) {
							moving = false;
						} else {
							entity.face(FaceDirection.EAST);
						}
						break;
					case 2:	// South
						x = (int)entity.x() / game.map().tileWidth();
						y = (int)(entity.y() + game.map().tileHeight()) / game.map().tileHeight();
						if(!game.map().isWalkable(x, y)) {
							moving = false;
						} else {
							entity.face(FaceDirection.SOUTH);
						}
						break;
					case 3:	// West
						x = (int)(entity.x() - game.map().tileWidth()) / game.map().tileWidth();
						y = (int)entity.y() / game.map().tileHeight();
						if(!game.map().isWalkable(x, y)) {
							moving = false;
						} else {
							entity.face(FaceDirection.WEST);
						}
						break;
				}
			} else {
				// do nothing
			}
		}
		
	}

}
