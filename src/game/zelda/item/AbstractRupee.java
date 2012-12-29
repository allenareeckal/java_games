package game.zelda.item;

import engine.Game;
import engine.entity.item.AbstractItem;
import engine.sprite.AnimatedSprite;
import engine.sprite.SpriteBank;
import engine.sprite.SpriteSheet;

public abstract class AbstractRupee extends AbstractItem {
	
	protected int value;
	
	protected AbstractRupee(Game game, int value, int spriteNumber) {
		super(game);
		this.value = value;
		SpriteSheet sheet = (SpriteSheet) SpriteBank.getInstance().get("entities");
		sprite = new AnimatedSprite(sheet.getRange(spriteNumber, spriteNumber), 0);
		collisionOffset = 6;
	}
	
	@Override
	public void consume() {
		game.link().rupees(game.link().rupees() + value);
		consumed = true;
	}

}