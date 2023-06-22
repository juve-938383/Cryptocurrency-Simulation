package com.mygdx.cryptocurrencysim;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.LinkedList;

public class Main extends ApplicationAdapter {

	//region Assets
	SpriteBatch batch;
	BitmapFont extraSmallFont;
	BitmapFont smallFont;
	BitmapFont largeFont;
	ShapeRenderer shape;
	Texture queueImage;
	Texture blockImage;
	Texture blockNoArrow;
	//endregion

	//Creates assets and initializes the Network
	@Override
	public void create () {

		Network.Start(15, 3);

		//region Asset initialization
		batch = new SpriteBatch();
		shape = new ShapeRenderer();
		queueImage = new Texture("Queue.png");
		blockImage = new Texture("Block.png");
		blockNoArrow = new Texture("BlockNoArrow.png");

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.local("cmu.serif-roman.ttf"));

		FreeTypeFontGenerator.FreeTypeFontParameter paramsExtraSmall = new FreeTypeFontGenerator.FreeTypeFontParameter();

		paramsExtraSmall.borderColor = Color.WHITE;
		paramsExtraSmall.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
		paramsExtraSmall.genMipMaps = true;
		paramsExtraSmall.size = 15;
		paramsExtraSmall.borderWidth = 0.2f;

		extraSmallFont = generator.generateFont(paramsExtraSmall);


		FreeTypeFontGenerator.FreeTypeFontParameter paramsSmall = new FreeTypeFontGenerator.FreeTypeFontParameter();

		paramsSmall.borderColor = Color.WHITE;
		paramsSmall.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
		paramsSmall.genMipMaps = true;
		paramsSmall.size = 30;
		paramsSmall.borderWidth = 0.5f;

		smallFont =	generator.generateFont(paramsSmall);

		FreeTypeFontGenerator.FreeTypeFontParameter paramsLarge = new FreeTypeFontGenerator.FreeTypeFontParameter();

		paramsLarge.borderColor = Color.WHITE;
		paramsLarge.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
		paramsLarge.genMipMaps = true;
		paramsLarge.size = 45;
		paramsLarge.borderWidth = 1f;

		largeFont =	generator.generateFont(paramsLarge);
		//endregion

	}

	//Renders the content and updates the Network
	@Override
	public void render () {
		//region Close app on ESC pressed
		if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)){
			Gdx.app.exit();
			System.exit(0);
		}
		//endregion

		Network.Update();

		//region Draw rectangles
		ScreenUtils.clear(0, 0, 0, 1);

		shape.begin(ShapeRenderer.ShapeType.Filled);
		for(int i = 0; i < Network.Users.length; i++) {
			shape.setColor(Color.RED);
			shape.rect(115 + i * 115, 130, 80, Network.Users[i].Balance * 4);
			if(i < Network.Miners.length) {
				if(Network.Miners[i].JustMined){
					shape.setColor(Color.GREEN);
				}else if(Network.Miners[i].State == Miner.RunningState.Idle){
					shape.setColor(Color.GRAY);
				} else if (Network.Miners[i].State == Miner.RunningState.Mining) {
					shape.setColor(Color.ORANGE);
				}
				shape.rect(115 + i * 115, 20, 80, 40);
			}
		}
		shape.end();

		shape.begin(ShapeRenderer.ShapeType.Line);
		shape.setColor(Color.GREEN);
		shape.rect(545, 15, 250, 50);
		shape.end();
		//endregion

		//region Balance text
		batch.begin();

		for(int i = 0; i < Network.Users.length; i++) {
			int _currentUserBalance = Network.Users[i].Balance;
			if (_currentUserBalance < 10) {
				smallFont.draw(batch, _currentUserBalance + "", 147 + i * 115, 160);
			} else if (_currentUserBalance > 99) {
				smallFont.draw(batch, _currentUserBalance + "", 132 + i * 115, 160);
			} else {
				smallFont.draw(batch, _currentUserBalance + "", 140 + i * 115, 160);
			}
		}
		smallFont.draw(batch, "Transactions in queue: " + Network.Miners[0].TransactionQueue.size(), 1420, 570);
		smallFont.draw(batch, "Press ESC to exit", 550, 50);
		//endregion

		//region Chart labels
		for(int i = 0; i < Network.Users.length; i++) {
				largeFont.draw(batch, Network.Users[i].Username, 115 + i * 115, 115);

		}
		//endregion

		//region Transaction queue
		LinkedList<Transaction> _transactions = Network.Miners[0].TransactionQueue;
		for(int i = 0; i < _transactions.size(); i++){
				if(i == 5){
					largeFont.draw(batch, ". . .", 1560, 700 + i * 70);
					break;
				}
				Transaction _currentTransaction = _transactions.get(i);
				int _transactionQuantity = _currentTransaction.Quantity;
				int y = 700 + i * 70;

				largeFont.draw(batch, _currentTransaction.Sender.Username, 1400, y);
				largeFont.draw(batch, "->", 1520, y);
				largeFont.draw(batch, _currentTransaction.Receiver.Username, 1600, y);

				if(_transactionQuantity < 10) {
					largeFont.draw(batch, _currentTransaction.Quantity + "", 1755, y);
				}else{
					largeFont.draw(batch, _currentTransaction.Quantity + "", 1730, y);
				}

		}
		//endregion

		//region Blockchain text
		LinkedList<Block> _blockchain = Network.Users[0].BlockChain;
		int _chainLength = _blockchain.size();
		for(int i = _chainLength-5; i < _chainLength; i++){
			if(i < 0){
				continue;
			}
			Block _currentBlock = _blockchain.get(i);
			int x;
			if(_chainLength < 5){
				x = 107 + i * 250;
			}else {
				x = 107 + ((i - (_chainLength - 5)) * 250);
			}
			extraSmallFont.draw(batch, _currentBlock.PreviousBlockHash, x, 1030, 185.0f, 10, true);

			smallFont.draw(batch, _currentBlock.TransactionList[0].toString(), x, 950);
			smallFont.draw(batch, _currentBlock.TransactionList[1].toString(), x, 900);
			smallFont.draw(batch, _currentBlock.TransactionList[2].toString(), x, 850);

			extraSmallFont.draw(batch, _currentBlock.TransactionsHash, x + 5, 780, 185.0f, 10, true);
			extraSmallFont.draw(batch, _currentBlock.Nonce, x, 720, 185.0f, 10, true);
			extraSmallFont.draw(batch, _currentBlock.CurrentHash, x, 650, 185.0f, 10, true);

		}

		batch.end();
		//endregion

		//region Render images
		batch.begin();
		batch.draw(queueImage, 1340, 600);
		for (int i = 0; i < _chainLength; i++){
			if(i == 5){
				break;
			}else if(i == 0){
				if (_chainLength < 6) {
					batch.draw(blockNoArrow, 100, 600);
				}
				else{
					batch.draw(blockImage, 50, 600);
				}
			}else{
				batch.draw(blockImage, 50 + i * 250, 600);
			}
		}
		batch.end();
		//endregion
	}

	//Disposes of the assets
	@Override
	public void dispose () {
		batch.dispose();
		extraSmallFont.dispose();
		smallFont.dispose();
		largeFont.dispose();
		shape.dispose();
		blockImage.dispose();
		blockNoArrow.dispose();
		queueImage.dispose();
	}
}
