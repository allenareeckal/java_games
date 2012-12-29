package game.zelda.player;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import engine.FaceDirection;
import engine.Game;
import engine.GameState;
import engine.entity.AbstractLivingEntity;
import engine.entity.enemy.AbstractEnemy;
import engine.entity.weapon.AbstractWeapon;
import engine.entity.weapon.WeaponBank;
import engine.keyboard.KeyBoard;
import engine.math.Vector2D;
import engine.sound.ISound;
import engine.sound.SoundBank;
import engine.sprite.AnimatedSprite;
import engine.sprite.SimpleSprite;
import engine.sprite.SpriteBank;
import engine.sprite.SpriteSheet;
import engine.sprite.SpriteUtils;

public class Link extends AbstractLivingEntity {

	private Vector2D mapPosition; // position in the map grid

	protected Vector2D acceleration = new Vector2D(0, 0);

	private int accelerationRate;

	private int rupees = 208;

	private AnimatedSprite linkCurrent;
	private AnimatedSprite linkN;
	private AnimatedSprite linkE;
	private AnimatedSprite linkS;
	private AnimatedSprite linkW;

	private AbstractWeapon weaponA;

	private AbstractWeapon weaponB;

	private ISound lowHeartsSound;

	public Link(Game game) {
		super(game);

		SpriteSheet sheet = (SpriteSheet) SpriteBank.getInstance().get("entities");

		linkE = new AnimatedSprite(sheet.getRange(2, 3), 200);
		linkW = SpriteUtils.flipHorizontal(linkE);
		linkN = new AnimatedSprite(sheet.getRange(4, 5), 200);
		linkS = new AnimatedSprite(sheet.getRange(0, 1), 200);
		
		weaponA = WeaponBank.getInstance().get("sword3");
		weaponB = WeaponBank.getInstance().get("boomerang");
		
		locate(6 * game.map().tileWidth(), 12 * game.map().tileHeight());

		mapPosition = new Vector2D();
		acceleration = new Vector2D();
		accelerationRate = 4;
		face = FaceDirection.EAST;
		linkCurrent = linkE;
		invincibleTime = 500;
		life = 5.5;
		maxLife = 20;
		collisionOffset(5);
		deadSound = SoundBank.getInstance().get("link_die");
		hitSound = SoundBank.getInstance().get("link_hurt");
		lowHeartsSound = SoundBank.getInstance().get("link_low_life");
	}

	@Override
	public void handle() {
		if (dead()) {
			game.gameState(GameState.DEAD);
			lowHeartsSound.stop();
		}
		weaponA.handle();
		weaponB.handle();

		// handle invincibility (after getting hit)
		if (invincible) {
			if (System.currentTimeMillis() - lastTimeHit > invincibleTime) {
				invincible = false;
				flickerCount = 0;
			}
		}

		// handle enemy collisions
		Iterator<AbstractEnemy> iter = game.enemies().iterator();
		while (iter.hasNext()) {
			if(life() <= 0){ lowHeartsSound.stop(); }
			AbstractEnemy entity = iter.next();
			if (rectangleCollide(entity)) {
				hit(entity.damage());
				if (life() <= 3 && maxLife() > 6) {
					if (!lowHeartsSound.playing()) {
						lowHeartsSound.play();
					}
				}
			}
			if (weaponA.using()) {
				if (weaponA.rectangleCollide(entity)) {
					entity.hit(weaponA.damage());
				}
			}
			if (weaponB.using()) {
				if (weaponB.rectangleCollide(entity)) {
					entity.hit(weaponB.damage());
				}
			}
		}

	}

	@Override
	public void draw(Graphics2D g) {
		weaponA.draw(g);
		weaponB.draw(g);
		if (!invincible) {
			linkCurrent.draw(g, renderX(), renderY());
		} else {
			if (flicker) {
				// @TODO find better way to do this without creating a new
				// sprite each time
				SimpleSprite neg = SpriteUtils.negative(linkCurrent
						.currentSprite());
				neg.draw(g, renderX(), renderY());
				neg = null;
				flicker = false;
				flickerCount++;
			} else {
				linkCurrent.draw(g, renderX(), renderY());
				if (flickerCount < maxFlickerCount) {
					flicker = true;
				}
			}
		}
	}

	/*
	 * used for keyboard handling
	 */
	private int offsetX() {
		return (x + collisionOffset()) / game.map().tileWidth();
	}

	private int offsetY() {
		return (y + collisionOffset()) / game.map().tileHeight(); 
	}

	public void keyBoard(KeyBoard kb) {
		acceleration.set(0, 0);
		boolean move = false;

		int offX = 0;
		int offY = 0;
		if (kb.isKeyPressed(KeyEvent.VK_LEFT)
				|| kb.isKeyPressed(KeyEvent.VK_RIGHT)
				|| kb.isKeyPressed(KeyEvent.VK_UP)
				|| kb.isKeyPressed(KeyEvent.VK_DOWN)) {
			offX = offsetX();
			offY = offsetY();

		}
		
		//@TODO do not check offx + m, offy + n such that it is out outside the map
		// handle angles first
		if (kb.isKeyPressed(KeyEvent.VK_RIGHT)
				&& kb.isKeyPressed(KeyEvent.VK_UP)) {
			if(!game.map().collide(this, offX + 1, offY, accelerationRate, -accelerationRate) &&
					!game.map().collide(this, offX + 1, offY - 1, accelerationRate, -accelerationRate) &&
					!game.map().collide(this, offX , offY - 1, accelerationRate, -accelerationRate)
					) {
				acceleration.add(accelerationRate, -accelerationRate); // normalize later
				move = true;
			}
		} else if (kb.isKeyPressed(KeyEvent.VK_RIGHT)
				&& kb.isKeyPressed(KeyEvent.VK_DOWN)) {
			if(!game.map().collide(this, offX + 1, offY, accelerationRate, accelerationRate) &&
					!game.map().collide(this, offX + 1, offY + 1, accelerationRate, accelerationRate) &&
					!game.map().collide(this, offX , offY + 1, accelerationRate, accelerationRate)
					) {
				acceleration.add(accelerationRate, accelerationRate); // normalize later
				move = true;
			}
		} else if (kb.isKeyPressed(KeyEvent.VK_LEFT)
				&& kb.isKeyPressed(KeyEvent.VK_UP)) {
			if(!game.map().collide(this, offX - 1, offY, -accelerationRate, -accelerationRate) &&
					!game.map().collide(this, offX - 1, offY - 1, -accelerationRate, -accelerationRate) &&
					!game.map().collide(this, offX , offY - 1, -accelerationRate, -accelerationRate)
					) {
				acceleration.add(-accelerationRate, -accelerationRate); // normalize later
				move = true;
			}
		} else if (kb.isKeyPressed(KeyEvent.VK_LEFT)
				&& kb.isKeyPressed(KeyEvent.VK_DOWN)) {
			if(!game.map().collide(this, offX - 1, offY, -accelerationRate, accelerationRate) &&
					!game.map().collide(this, offX - 1, offY + 1, -accelerationRate, accelerationRate) &&
					!game.map().collide(this, offX , offY + 1, -accelerationRate, accelerationRate)
					) {
				acceleration.add(-accelerationRate, accelerationRate); // normalize later
				move = true;
			}
		} else if (kb.isKeyPressed(KeyEvent.VK_RIGHT)) {
			if(!game.map().collide(this, offX + 1, offY - 1, accelerationRate, 0) &&
					!game.map().collide(this, offX + 1, offY, accelerationRate, 0) &&
					!game.map().collide(this, offX + 1, offY + 1, accelerationRate, 0)
					) {
				acceleration.add(accelerationRate, 0);
				move = true;
			}
		} else if (kb.isKeyPressed(KeyEvent.VK_LEFT)) {
			if(!game.map().collide(this, offX - 1, offY - 1, -accelerationRate, 0) &&
					!game.map().collide(this, offX - 1, offY, -accelerationRate, 0) &&
					!game.map().collide(this, offX - 1, offY + 1, -accelerationRate, 0)
					) {
				acceleration.add(-accelerationRate, 0);
				move = true;
			}
		} else if (kb.isKeyPressed(KeyEvent.VK_UP)) {
			if(!game.map().collide(this, offX - 1, offY - 1, 0, -accelerationRate) &&
					!game.map().collide(this, offX, offY - 1, 0, -accelerationRate) &&
					!game.map().collide(this, offX + 1, offY - 1, 0, -accelerationRate)
					) {
				acceleration.add(0, -accelerationRate);
				move = true;
			}
			
		} else if (kb.isKeyPressed(KeyEvent.VK_DOWN)) {
			if(!game.map().collide(this, offX - 1, offY + 1, 0, accelerationRate) &&
					!game.map().collide(this, offX, offY + 1, 0, accelerationRate) &&
					!game.map().collide(this, offX + 1, offY + 1, 0, accelerationRate)
					) { 
				acceleration.add(0, accelerationRate);
				move = true;
			}
		}
		
		// facing
		if (kb.isKeyPressed(KeyEvent.VK_RIGHT)) {
			face = FaceDirection.EAST;
			linkCurrent = linkE;
		} else if (kb.isKeyPressed(KeyEvent.VK_LEFT)) {
			face = FaceDirection.WEST;
			linkCurrent = linkW;
		} else if (kb.isKeyPressed(KeyEvent.VK_UP)) {
			face = FaceDirection.NORTH;
			linkCurrent = linkN;
		} else if (kb.isKeyPressed(KeyEvent.VK_DOWN)) {
			face = FaceDirection.SOUTH;
			linkCurrent = linkS;
		}

		// update position on grid
		if (move) {
			// offX = (int) (-(game.map().offset().x() + -acceleration.x()) /
			// game.map().tileWidth() + game.map().entityOffset());
			// offY = (int) (-(game.map().offset().y() + -acceleration.y()) /
			// game.map().tileHeight() + game.map().entityOffset());
			game.map().offset().subtract(acceleration);
			mapPosition.set(offsetX(), offsetX());
			x += (int) acceleration.x();
			y += (int) acceleration.y();
		}

		if (kb.isKeyPressed(KeyEvent.VK_A)) {
			if (!weaponA.using() && !weaponB.using()) {
				weaponB.use();
			}
		}
		if (kb.isKeyPressed(KeyEvent.VK_S)) {
			if (!weaponA.using() && !weaponB.using()) {
				weaponA.use();
			}
		}

		if (kb.isKeyPressed(KeyEvent.VK_ESCAPE)) {
			game.gameState(GameState.END);
		}

	}

	public AbstractWeapon weaponA() {
		return weaponA;
	}

	public AbstractWeapon weaponB() {
		return weaponB;
	}

	public int rupees() {
		return rupees;
	}

	public void rupees(int rupees) {
		this.rupees = rupees;
	}

	@Override
	public int renderX() {
		return game.map().tileWidth() * game.map().entityOffset();
	}

	@Override
	public int renderY() {
		return game.map().tileHeight() * game.map().entityOffset();
	}

	@Override
	public int width() {
		return linkCurrent.width();
	}

	@Override
	public int height() {
		return linkCurrent.height();
	}

	public void face(FaceDirection face) {
		this.face = face;
	}

}
