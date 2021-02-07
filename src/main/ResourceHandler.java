package main;

import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import elem.objects.Sprite;
import engine.math.Vec2;

public class ResourceHandler {
    private static Stack<Sprite> preparedSpriteStack = new Stack<>();
    private static ArrayList<Vec2> topleftPointList = new ArrayList<>();
    private static ArrayList<Float> heightSizeList = new ArrayList<>();
    private static ArrayList<String> spriteNameList = new ArrayList<>(), shaderNameList = new ArrayList<>();
    private static ArrayList<Consumer<Sprite>> afterLoadedList = new ArrayList<>(44);
    private static ArrayList<Boolean> doneList = new ArrayList<>(44);
    private static AtomicInteger availableIndexToPrepare = new AtomicInteger(), finishedThreads = new AtomicInteger();
    private static int amount;
    private static boolean running;
    private final int amountThreads = 3; 
    
    public ResourceHandler() {
    	new Thread(() -> {
    		running = true;
	    	for(int i = 0; i < amountThreads; i++) {
	    		new Thread(() -> {
	    			int checkIndex = 0;
	    			while (running) {
	    				try {
	    					if (afterLoadedList.size() <= (checkIndex = availableIndexToPrepare.get()))
	    						continue;
	    				} catch (NullPointerException e) {
	    					continue;
	    				}
	    				int index = 0;
	    				synchronized (availableIndexToPrepare) {
	    					index = availableIndexToPrepare.getAndIncrement();
		    				if (index != checkIndex) {
			    				availableIndexToPrepare.decrementAndGet();
			    				continue;
			    			}
		    			}
		    			
	    				// IndexoutOfBoundsException was here
		    			Vec2 topleftPoint = topleftPointList.get(index);
		    			float heightSize = heightSizeList.get(index);
		    			String spriteName = spriteNameList.get(index);
		    			String shaderName = shaderNameList.get(index);
		    			Consumer<Sprite> afterLoaded = afterLoadedList.get(index);
		    			
//		    			System.out.println("Loading: " +spriteName);
		    			
		    			if (topleftPoint != null) {
		    				PushSprite(new Sprite(topleftPoint, heightSize, spriteName, shaderName), afterLoaded);
		    			} else if (heightSize != 0) {
		    				PushSprite(new Sprite(heightSize, spriteName, shaderName), afterLoaded);
		    			} else {
		    				PushSprite(new Sprite(spriteName, shaderName), afterLoaded);
		    			}
		    	    	doneList.set(index, true);
	    			}
	    			finishedThreads.incrementAndGet();
	    		}).start();
	    	}
    	}).start();
    }

    public static void LoadSprite(Vec2 topleftPoint, float heightSize, String spriteName, String shaderName, Consumer<Sprite> afterLoaded) {
    	amount++;
    	topleftPointList.add(topleftPoint);
    	heightSizeList.add(heightSize);
    	spriteNameList.add(spriteName);
    	shaderNameList.add(shaderName);
    	afterLoadedList.add(afterLoaded);
    	doneList.add(false);
    }

    public static void LoadSprite(float heightSize, String spriteName, String shaderName, Consumer<Sprite> afterLoaded) {
    	amount++;
    	topleftPointList.add(null);
    	heightSizeList.add(heightSize);
    	spriteNameList.add(spriteName);
    	shaderNameList.add(shaderName);
    	afterLoadedList.add(afterLoaded);
    	doneList.add(false);
    }

    public static void LoadSprite(String spriteName, String shaderName, Consumer<Sprite> afterLoaded) {
    	amount++;
    	topleftPointList.add(null);
    	heightSizeList.add(0f);
    	spriteNameList.add(spriteName);
    	shaderNameList.add(shaderName);
    	afterLoadedList.add(afterLoaded);
    	doneList.add(false);
    }

    private static void PushSprite(Sprite sprite, Consumer<Sprite> afterLoaded) {
    	preparedSpriteStack.push(sprite);
        afterLoaded.accept(sprite);
    }

    public boolean isNotDone() {
        return amount > 0;
    }
    
    public int getAmount() {
    	return amount;
    }

    public void createNext() {
        if (!preparedSpriteStack.isEmpty()) {
            amount--;
            preparedSpriteStack.pop().create();
        }
    }
    
    public void destroy() {
    	running = false;
    	while (finishedThreads.get() != amountThreads);
    	preparedSpriteStack = null;
    	topleftPointList = null;
    	heightSizeList = null;
    	spriteNameList = null;
    	shaderNameList = null;
    	afterLoadedList = null;
    	availableIndexToPrepare = null;
    }

}
