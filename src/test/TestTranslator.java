package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;

import communication.Translator;
import elem.upgrades.RegVals;
import elem.upgrades.Store;
import elem.upgrades.UpgradePrice;
import elem.upgrades.Upgrades;
import engine.math.Vec2;
import game_modes.GameMode;
import game_modes.GolfMode;
import player_local.BankType;
import player_local.Player;
import player_local.Car.Car;
import player_local.Car.Rep;

public class TestTranslator {

	@Test
	void playerCloneString() {
		Player player = new Player("test", (byte) 0, Player.HOST);
		player.getBank().add(20000, BankType.MONEY);
		Store store = new Store();
		
		GameMode gm = new GolfMode();
		gm.init(new ConcurrentHashMap<>(), new Random());
		gm.createPrices();
		store.setPrices(player, gm.getPrices());
		store.resetAllTowardsPlayer(player, gm);
		
		var tile = store.getStoreTiles().get(0);
		store.attemptBuyTile(player, tile, new Vec2(1));
		long startTime = System.currentTimeMillis();
		String toSend = Translator.getCloneString(player, false, true);
		
		Player clonedPlayer = new Player();
		Translator.setCloneString(clonedPlayer, toSend);

		String recieved = Translator.getCloneString(clonedPlayer, false, true);
		long endTime = System.currentTimeMillis();
		System.out.println("Time: " + (endTime - startTime) + "ms");
		System.out.println(toSend);
		System.out.println(recieved);
		assertEquals(toSend, recieved);
	}
	
	@Test
	void upgradeCloneString() {
		Player player = new Player("test", (byte) 0, Player.HOST);
		player.getBank().add(20000, BankType.MONEY);
		Store store = new Store();
		
		GameMode gm = new GolfMode();
		gm.init(new ConcurrentHashMap<>(), new Random());
		gm.createPrices();
		store.setPrices(player, gm.getPrices());
		store.resetAllTowardsPlayer(player, gm);
		
		var tile = store.getStoreTiles().get(0);
		store.attemptBuyTile(player, tile, new Vec2(1));
		String toSend = Translator.getCloneString(player.upgrades, false, true);

		Upgrades upgrades2 = new Upgrades();
		Translator.setCloneString(upgrades2, toSend);
		String recieved = Translator.getCloneString(upgrades2, false, true);
		assertEquals(toSend, recieved);
	}
	
	@Test
	void upgrade() {
		Car car = new Car();
		car.switchTo(2);
		String ogStr = car.getRep().toString();
		String expected = "2, 1.0, 1000.0, 0.6, 80.0, 740.0, 204.0, 800.0, 5500.0, 6.0, 1200.0, 0.0, 25.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0784313725490196, 0.0";
		System.out.println(ogStr);
		System.out.println(expected);
		assertEquals(expected, ogStr);
		
		var og = new RegVals(new double[]{ 0, 100, 1.1, RegVals.decimals + 0.1, 0, RegVals.specialPercent + 1.02});
		og.upgrade(car.getRep());
		String modStr = car.getRep().toString();
		expected = "2, 1.0, 1100.0, 0.66, 80.1, 740.0, 208.08, 800.0, 5500.0, 6.0, 1200.0, 0.0, 25.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0784313725490196, 0.0";
		System.out.println(modStr);
		System.out.println(expected);
		assertEquals(expected, modStr);
		
		og.multiplyAllValues(10);
		assertEquals("+1000 nos ms, +100% nos, +1 kW, +20% km/h", og.getUpgradeRepString());
		
		og.upgrade(car.getRep());
		modStr = car.getRep().toString();
		expected = "2, 1.0, 2100.0, 1.32, 81.1, 740.0, 249.696, 800.0, 5500.0, 6.0, 1200.0, 0.0, 25.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0784313725490196, 0.0";
		System.out.println(modStr);
		System.out.println(expected);
		assertEquals(expected, modStr);
		
	}
	
	@Test
	void neighbourMod() {
		var og = new RegVals(new double[]{ 0, 100, 1.1, RegVals.decimals + 0.1});
		String str = og.getUpgradeRepString();
		assertEquals("+100 nos ms, +10% nos, +0.1 kW", str);
		
		var mod = new RegVals(new double[]{ 0, RegVals.decimals + 0.1 , 100, 1.1});
		og.combine(mod);
		str = og.getUpgradeRepString();
		assertEquals("+100.1 nos ms, +10% nos, +0.11 kW", str);

		mod = new RegVals(new double[]{ 0, 1.1, RegVals.decimals + 0.1 , 100});
		og.combine(mod);
		str = og.getUpgradeRepString();
		assertEquals("+110.11 nos ms, +10% nos, +100.11 kW", str);

		mod = new RegVals(new double[]{ 0, 0, 0, 0, 0, 1.2, 0.5});
		mod.values[Rep.tbArea] = RegVals.specialPercent + 1.69;
		og.combine(mod);
		str = og.getUpgradeRepString();
		assertEquals("+110.11 nos ms, +10% nos, +100.11 kW, +20% km/h, -50% idle-rpm, +69% tb area", str);

		mod = new RegVals(new double[]{ 0, 0, 0, 0, 0, 0.5, 1.2});
		og.combine(mod);
		str = og.getUpgradeRepString();
		assertEquals("+110.11 nos ms, +10% nos, +100.11 kW, +10% km/h, -60% idle-rpm, +69% tb area", str);

		
		og = new RegVals(new double[]{0, 0, 0, 0, 0, 1.05});
		mod = new RegVals(new double[]{0, 0, 0, 0, 0, RegVals.specialPercent + 1.02});
		og.combine(mod);
		str = og.getUpgradeRepString();
		assertEquals("+7% km/h", str);
	}
	
}
