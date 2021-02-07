package elem.upgrades;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import adt.ICloneStringable;
import audio.AudioRemote;
import audio.SfxTypes;
import engine.math.Vec2;
import game_modes.GameMode;
import main.Features;
import main.Game;
import main.Texts;
import player_local.Player;
import player_local.Car.Rep;
import scenes.SceneHandler;

//Dyrere for de som leder: 1st (+10%), 2nd (5%), 3rd (2%) osv.
//cancel bigger turbo farm gold
//tb too stronk weight too weak lighter pistons too weak

public class Upgrades implements ICloneStringable {

	public static final byte
			powerID = 0,
			boostID = 1,	
			supplementaryID = 2,	
			clutchID = 3, 
			gearID = 4, 
			fuelID = 5, 
			nosID = 6,
			tbID = 7, 
			weightID = 8, 
			pistonsID = 9, 
			turboID = 10, 
			blockID = 11,
			moneyID = 12;

	public static final String[] UPGRADE_NAMES = {
			"Power",
			"Boost",
			"Supplementary",
			"Clutch", 
			"Gears", 
			"Fuel",
			"N O S", 
			"Tireboost", 
			"Weight", 
			"Lighter Pistons", 
			"Turbo",
			"Beefy Block", 
			"Money Pit",
			};
	
	public boolean[] unlockedIds = new boolean[UPGRADE_NAMES.length];

	public static final String[] info = new String[UPGRADE_NAMES.length];
	public static final String[][] bonusesTextsGold = new String[UPGRADE_NAMES.length][], bonusesTexts = new String[UPGRADE_NAMES.length][];

	public static int[] UPGRADE_HEIGHTS = new int[UPGRADE_NAMES.length];
	private static Queue<Byte> lastFocusedUpgrade;
	private static double[][] originalUpgradeValues;

	private static final String[] fuelNames = new String[]{
		"98 octane", "Ethanol", "Methanol"
	};
	private double fuelCollectedKW;
	private int fuelAmount;
	private final Upgrade[] upgradeReferences;
	private AudioRemote audio;

	public Upgrades() {
		lastFocusedUpgrade = new LinkedList<>();

		upgradeReferences = new Upgrade[UPGRADE_NAMES.length];
		originalUpgradeValues = new double[UPGRADE_NAMES.length][];
		
		byte powerMaxLVL = 1;
		byte[] powerLVLs = {};
		double[] powerRegUpgrades = {0, 0, 0, 30};
		double[] powerNeighbour = {0};
		String[] powerGold = {};
		String[] powerNormal = {};
		UpgradeAction[] powerBonus = {};
		createUpgrade(powerID, powerMaxLVL, powerLVLs, powerRegUpgrades,
						"  Strong in high rpm.\n\n"
						+ "    Unlocks:#LBEIGE\n"
						+ "- " + UPGRADE_NAMES[weightID] + "#LBEIGE\n"
						+ "- " + UPGRADE_NAMES[turboID] + "#LBEIGE\n"
						+ "- " + UPGRADE_NAMES[blockID] + "#LBEIGE"
								,
						(regularValues, player, rep, gold, test) -> {
							if (regularValues == null)
								regularValues = upgradeReferences[powerID].getRegVals();
							regularValues.upgrade(rep);
							if (!test && !unlockedIds[weightID]) {
								audio.get(SfxTypes.UNLOCKED).play();
								unlockedIds[weightID] = true;
								unlockedIds[turboID] = true;
								unlockedIds[blockID] = true;
							}
						},
						powerGold, powerNormal, powerBonus,
						powerNeighbour);
		
		
		byte boostMaxLVL = 1;
		byte[] boostLVLs = {1};
		double[] boostRegUpgrades = {0};
		double[] boostNeighbour = {0};
		String[] boostGold = {"+1 nos bottle"};
		String[] boostNormal = {"+0.2 tb"};
		UpgradeAction[] boostBonus = {(regularValues, player, rep, gold, test) -> {
			if (gold) {
				rep.add(Rep.nosSize, 1);
				upgradeReferences[nosID].getRegVals().values[Rep.nos] -= 0.1;
			}
			else
				rep.add(Rep.tb, 0.2);
		}};
		createUpgrade(boostID, boostMaxLVL, boostLVLs, boostRegUpgrades,
						"  Sprint type.\n"
						+ "  Strong in low rpm.\n\n"
						+ "    Unlocks:#LBEIGE\n"
						+ "- " + UPGRADE_NAMES[tbID] + "#LBEIGE\n"
						+ "- " + UPGRADE_NAMES[nosID] + "#LBEIGE"
								,
						(regularValues, player, rep, gold, test) -> {
							if (regularValues == null)
								regularValues = upgradeReferences[boostID].getRegVals();
							regularValues.upgrade(rep);
							if (!test && !unlockedIds[tbID]) {
								audio.get(SfxTypes.UNLOCKED).play();
								unlockedIds[tbID] = true;
								unlockedIds[nosID] = true;
							}
						},
						boostGold, boostNormal, boostBonus,
						boostNeighbour);
		
		
		byte supplementaryMaxLVL = 1;
		byte[] supplementaryLVLs = {};
		double[] supplementaryRegUpgrades = {0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 10};
		double[] supplementaryNeighbour = {0};
		String[] supplementaryGold = {};
		String[] supplementaryNormal = {};
		UpgradeAction[] supplementaryBonus = {};
		createUpgrade(supplementaryID, supplementaryMaxLVL, supplementaryLVLs, supplementaryRegUpgrades,
						"  Eco and speed upgrade.\n\n"
						+ "    Unlocks:#LBEIGE\n"
						+ "- " + UPGRADE_NAMES[clutchID] + "#LBEIGE\n"
						+ "- " + UPGRADE_NAMES[moneyID] + "#LBEIGE"
								,
						(regularValues, player, rep, gold, test) -> {
							if (regularValues == null)
								regularValues = upgradeReferences[supplementaryID].getRegVals();
							regularValues.upgrade(rep);
							if (!test && !unlockedIds[clutchID]) {
								audio.get(SfxTypes.UNLOCKED).play();
								unlockedIds[clutchID] = true;
								unlockedIds[moneyID] = true;
							}
						},
						supplementaryGold, supplementaryNormal, supplementaryBonus,
						supplementaryNeighbour);
		
		
		
		
		byte clutchMaxLVL = 14;
		byte[] clutchLVLs = {3, 5, 7, 14};
		double[] clutchRegUpgrades = {0, 0, 0, 0, 0, 1.05};
		double[] clutchNeighbour = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, RegVals.decimals + 0.1};
		String[] clutchGold = {
				"Unlock ability to shift and \n"
				+ " hold down throttle, and -8% kg",
				"Guaranteed Tireboost unlocked, \n"
				+ " and +2 tb",
				"Unlock Sticky-Clutch \n"
				+ " (same kW over entire powerband)", "STRENGTH: +50% nos and tb"};
		String[] clutchNormal = {
				"Unlock ability to shift and \n"
				+ " hold down throttle",
				"Guaranteed Tireboost unlocked\n"
				+ " (no tb loss from reaction-time)",
				"Earn 30% of current tb as " + Texts.nos, "TIME: +50% nos and tb"};
		UpgradeAction[] clutchBonus = {(UpgradeAction) (regularValues, player, rep, gold, test) -> {
			if (gold)
				rep.mul(Rep.kg, 0.92);

			rep.setBool(Rep.throttleShift, true);

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			rep.guarenteeTireboostArea();

			if (gold)
				rep.add(Rep.tb, 2);

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			if (gold)
				rep.setBool(Rep.stickyclutch, true);
			else
				rep.add(Rep.nos, rep.get(Rep.tb) * 0.3);
		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			double amount = 1.5;
			if (gold) {
				rep.mul(Rep.tb, amount);
				rep.mul(Rep.nos, amount);
			} else {
				rep.mul(Rep.tbMs, amount);
				rep.mul(Rep.nosMs, amount);
			}
		}};
		createUpgrade(clutchID, clutchMaxLVL, clutchLVLs, clutchRegUpgrades,
						"  Increases km/h by % so weak early,\n"
						+ "  but has strong bonuses!\n\n"
						+ "    Unlocks:#LBEIGE\n"
						+ "- " + UPGRADE_NAMES[gearID] + "#LBEIGE",
						(regularValues, player, rep, gold, test) -> {
							if (regularValues == null)
								regularValues = upgradeReferences[clutchID].getRegVals();
							regularValues.upgrade(rep);
							if (!test && !unlockedIds[gearID]) {
								audio.get(SfxTypes.UNLOCKED).play();
								unlockedIds[gearID] = true;
							}
						},
						clutchGold, clutchNormal, clutchBonus,
						clutchNeighbour);
		
		byte gearMaxLVL = -1;
		byte[] gearLVLs = {3, 5, 7};
		double[] gearRegUpgrades = {0, 0, 0, 0, 0, 72};
		double[] gearNeighbour = {0, 0, 0, 0, 0, RegVals.specialPercent + 1.02};
		String[] gearGold = {"+0.2 " + Texts.nos + " from " + UPGRADE_NAMES[pistonsID],
				"Sequential shift unlocked\n"
				+ " and +10% current nos", "+420 km/h"};
		String[] gearNormal = {"Free " + UPGRADE_NAMES[fuelID] + " upgrade",
				"Sequential shift unlocked\n"
				+ " and -10% current nos",
				"+120 km/h"};
		UpgradeAction[] gearBonus = {(UpgradeAction) (regularValues, player, rep, gold, test) -> {

			if (gold)
				upgradeReferences[pistonsID].getRegVals().values[Rep.nos] =  RegVals.decimals + 0.2;
			else {
				lastFocusedUpgrade.offer(fuelID);
				upgradeReferences[fuelID].upgrade(player, null, false);
			}

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			rep.setBool(Rep.sequential, true);

			if (gold)
				rep.mul(Rep.nos, 1.1);
			else 
				rep.mul(Rep.nos, 0.9);
				

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			rep.add(Rep.spdTop, gold ? 420 : 120);
		}};
		createUpgrade(gearID, gearMaxLVL, gearLVLs, gearRegUpgrades,
				"  Main km/h source.\n",
				 gearGold, gearNormal, gearBonus,
					gearNeighbour);
		
		
		fuelAmount = 30;
		fuelCollectedKW = 0;
		byte fuelMaxLVL = (byte) fuelNames.length;
		byte[] fuelLVLs = {2, 3};
		double[] fuelRegUpgrades = {0, 0, 0, fuelAmount};
		double[] fuelNeighbour = {0, 0, 0, 30};
		String[] fuelGold = {"+20% current victory$",
				"+10% km/h from " + UPGRADE_NAMES[gearID] + " instead\n"
						+ " and +3% km/h from " + UPGRADE_NAMES[pistonsID]};
		String[] fuelNormal = {"+500 tb ms and +300 kg",
				"+10% km/h from " + UPGRADE_NAMES[gearID] + " instead"};
		UpgradeAction[] fuelBonus = 
		{(UpgradeAction) (regularValues, player, rep, gold, test) -> {
			if (gold) {
				rep.mul(Rep.vmoney, 1.2);
			} else {
				rep.add(Rep.tbMs, 500);
				rep.add(Rep.kg, 300);
			}

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			if (gold)
				upgradeReferences[pistonsID].getRegVals().values[Rep.spdTop] = 1.03;
			upgradeReferences[gearID].getRegVals().values[Rep.spdTop] = 1.10;
		}};
		String fuelInfo = "  Has essential bonus for high km/h!\n"
				+ "  Stats sequence:\n ";
		double tempCoilsAmount = fuelRegUpgrades[3];
		for (int i = 0; i < fuelMaxLVL; i++) {
			fuelInfo += (int) tempCoilsAmount
					+ (i != fuelMaxLVL - 1 ? " -> " : " kW");
			tempCoilsAmount = tempCoilsAmount + fuelAmount;
		}
		createUpgrade(fuelID, fuelMaxLVL, fuelLVLs, fuelRegUpgrades, fuelInfo,
				(UpgradeAction) (regularValues, player, rep, gold, test) -> {
					if (regularValues == null)
						regularValues = upgradeReferences[fuelID].getRegVals();
					
					long beforeKW = rep.getInt(Rep.kW);
					
					regularValues.upgrade(rep);
					
					// Øk kraften til fuel til neste steg.
					if (!test)
						regularValues.values[Rep.kW] += fuelAmount;

					if (!test) {
						long afterKW = rep.getInt(Rep.kW); // TODO Test fuel i unit testing
						fuelCollectedKW += afterKW - beforeKW;
					}
				}, fuelGold, fuelNormal, fuelBonus,
				fuelNeighbour);
		
		
		byte nosMaxLVL = -1;
		byte[] nosLVLs = {3, 5, 8, 10};
		double[] nosRegUpgrades = {1, 0, RegVals.decimals + 0.3};
		double[] nosNeighbour = {0, 0, RegVals.decimals + 0.1};
		String[] nosGold = {"+2 tb, but -0.1 tb from Tireboost",
				"-70% off Tireboost", "Set off first NOS automatically",
				"+15% " + Texts.nos + " from " + UPGRADE_NAMES[nosID] + " instead"};
		String[] nosNormal = {"+0.1 tb from Tireboost",
				"+1 nos bottle", "+2 " + Texts.nos,
				"+10% " + Texts.nos + " from " + UPGRADE_NAMES[nosID] + " instead"};
		UpgradeAction[] nosBonus = {(UpgradeAction) (regularValues, player, rep, gold, test) -> {
			if (gold) {
				rep.add(Rep.tb, 2);
				upgradeReferences[tbID].getRegVals().values[Rep.tb] -= 0.1;
			} else {
				upgradeReferences[tbID].getRegVals().values[Rep.tb] += 0.1;
			}

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			if (gold) {
				player.getBank().addSale(0.3, tbID, nosID);
			} else {
				rep.add(Rep.nosSize, 1);
			}

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			if (gold) {
				rep.setBool(Rep.nosAuto, true);
			} else {
				rep.add(Rep.nos, 2);
			}

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			double val = 0;
			
			if (gold)
				val = 1.15;
			else
				val = 1.1;
			
			regularValues.values[Rep.nos] = val;
			upgradeReferences[nosID].getRegVals().values[Rep.nos] = val;
		}};
		createUpgrade(nosID, nosMaxLVL, nosLVLs, nosRegUpgrades,
					"  Ignores rpm and gear resistance.\n"
						+ "  Stackable (50% weaker for each)\n"
						+ "  Press E to use!",
				(UpgradeAction) (regularValues, player, rep, gold, test) -> {

					if (regularValues == null)
						regularValues = upgradeReferences[nosID].getRegVals();
					
					regularValues.upgrade(rep);
					if (!test)
						regularValues.values[Rep.nosSize] = 0;
					
					if (!test)
						upgradeReferences[nosID].getRegVals().values[Rep.nosSize] = 0;

				}, nosGold, nosNormal, nosBonus,
				nosNeighbour);
		
		byte tbMaxLVL = -1;
		byte[] tbLVLs = {3, 5, 7, 10};
		double[] tbRegUpgrades = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, RegVals.decimals + 0.3};
		double[] tbNeighbour = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, RegVals.decimals + 0.1};
		String[] tbGold = {"Turbo spools 400% faster",
				"+0.3 tb from " + UPGRADE_NAMES[pistonsID],
				"+30% km/h and +100% current tb",
				"+15% tb from " + UPGRADE_NAMES[tbID] + " instead"};
		String[] tbNormal = {"+20% current " + Texts.nos + "", "+2 tb", "+30% km/h",
				"+10% tb from " + UPGRADE_NAMES[tbID] + " instead"};
		UpgradeAction[] tbBonus = {(UpgradeAction) (regularValues, player, rep, gold, test) -> {
			
			if (gold)
				rep.mul(Rep.spool, 4);
			else
				rep.mul(Rep.nos, 1.2);

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {

			if (gold)
				upgradeReferences[pistonsID].getRegVals().values[Rep.tb] = RegVals.decimals + 0.3;
			else
				rep.add(Rep.nos, 2);

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {

			rep.mul(Rep.spdTop, 1.30);

			if (gold)
				rep.mul(Rep.tb, 2);

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			double val = 0;
			
			if (gold)
				val = 1.15;
			else
				val = 1.1;
			
			regularValues.values[Rep.tb] = val;
			upgradeReferences[tbID].getRegVals().values[Rep.tb] = val;
		}};
		createUpgrade(tbID, tbMaxLVL, tbLVLs, tbRegUpgrades,
						"  Ignores rpm and gear resistance.\n"
						+ "  Based on reactiontime.\n"
						+ "  100% tb at 0ms\n"
						+ "   0% tb at 1000ms",
						tbGold, tbNormal, tbBonus,
						tbNeighbour);

		double weightIncPerUpgrade = 0.01;
		byte weightMaxLVL = 12;
		byte[] weightLVLs = {2, 4, 6, 12};
		double[] weightRegUpgrades = {0, 0, 0, 0, 0.96};
		double[] weightNeighbour = {0, 0, 0, 0, RegVals.specialPercent + 0.99};
		String[] weightGold = {UPGRADE_NAMES[turboID] + " -2% kg instead",
				UPGRADE_NAMES[blockID] + " +2% kg instead",
				UPGRADE_NAMES[clutchID] + " gets -5% kg and +120 km/h", 
						"+(-2% kg) for every upgrade"
						};
		String[] weightNormal = {
				UPGRADE_NAMES[turboID] + " no longer increases kg",
				UPGRADE_NAMES[blockID] + " +6% kg instead",
				"Get -3% kg from " + UPGRADE_NAMES[pistonsID], "-15% kg"};
		UpgradeAction[] weightBonus = {

				(UpgradeAction) (regularValues, player, rep, gold, test) -> {

					double value;
					if (gold) {
						value = 0.98f;
					} else {
						value = 0;
					}
					upgradeReferences[turboID].getRegVals().values[Rep.kg] = value;

				}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
					double value;
					if (gold) {
						value = 1.02f;
					} else {
						value = 1.06f;
					}
					upgradeReferences[blockID].getRegVals().values[Rep.kg] = value;

				}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
					
					if (gold) {
						upgradeReferences[clutchID].getRegVals().values[Rep.kg] = 0.95;
						upgradeReferences[clutchID].getRegVals().values[Rep.tbArea] = 0;
						rep.add(Rep.spdTop, 120);
					} else {
						upgradeReferences[pistonsID].getRegVals().values[Rep.kg] = 0.97;
					}

				}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {

					if (gold) {
						// mink alt med -x%kg
						RegVals regVals;
						double ogKgVal;
						for (int i = 0; i < UPGRADE_NAMES.length; i++) {
							regVals = upgradeReferences[i].getRegVals();
							ogKgVal = regVals.values[Rep.kg];

							// modifiser eller sett minking ettersom noe er allerede satt.
							if(ogKgVal != 0) {
								if (regVals.isValuePercent(i))
									regVals.values[Rep.kg] = ogKgVal - 0.02;
							} else {
								regVals.values[Rep.kg] = 0.98;
							}
						}
					} else {
						rep.mul(Rep.kg, 0.85);
					}

				}};
		createUpgrade(weightID, weightMaxLVL, weightLVLs, weightRegUpgrades,
						  "  Same as kW,\n"
						+ "  think power to weight ratio!\n"
						+ "  -" + String.format("%.1f", weightIncPerUpgrade * 100f) + "% kg per tile.\n\n"
						+ "    Unlocks:#LBEIGE\n"
						+ "- " + UPGRADE_NAMES[fuelID] + "#LBEIGE\n"
						+ "- " + UPGRADE_NAMES[pistonsID] + "#LBEIGE"
						,
				(UpgradeAction) (regularValues, player, rep, gold, test) -> {
					if (regularValues == null)
						regularValues = upgradeReferences[weightID].getRegVals();
			
					regularValues.upgrade(player.getCarRep());
					regularValues = upgradeReferences[weightID].getRegVals();
					if (!test) {
						regularValues.values[Rep.kg] -= weightIncPerUpgrade;
						if (!unlockedIds[fuelID]) {
							audio.get(SfxTypes.UNLOCKED).play();
							unlockedIds[fuelID] = true;
							unlockedIds[pistonsID] = true;
						}
					}

				}, weightGold, weightNormal, weightBonus,
				weightNeighbour);

		double increase = 2;
		String increaseFuelVals = "+" + (int) (-(1f - increase) * 100f) + "% " + UPGRADE_NAMES[fuelID] + " kW";
		byte pistonsMaxLVL = -1;
		byte[] pistonsLVLs = {2, 5};
		double[] pistonsRegUpgrades = {0, 0, 0, 1.08, 0, 0, 0, 0};
		double[] pistonsNeighbour = {0, 0, 0, 1.02, 0, 0, 0, 0};
		String[] pistonsGold = {
				increaseFuelVals + " even if upgraded!",
				"+100% " + UPGRADE_NAMES[pistonsID] + " values"
						};
		String[] pistonsNormal = {
				increaseFuelVals,
				"+75% " + UPGRADE_NAMES[pistonsID] + " values"};
		UpgradeAction[] pistonsBonus = {(UpgradeAction) (regularValues, player, rep, gold, test) -> {
			if (gold) {
				rep.add(Rep.kW, fuelCollectedKW);
			}
			
			fuelAmount *= increase;
			upgradeReferences[fuelID].getRegVals().multiplyAllValues(increase);

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {

			if (gold) {
				upgradeReferences[pistonsID].getRegVals()
						.multiplyAllValues(2f);
			} else {
				upgradeReferences[pistonsID].getRegVals()
						.multiplyAllValues(1.75f);
			}

		}};
		createUpgrade(pistonsID, pistonsMaxLVL, pistonsLVLs, pistonsRegUpgrades,
				"  Weak early but can\n"
						+ "  become strong by collecting\n"
						+ "  improvements from other bonuses.",
						pistonsGold, pistonsNormal, pistonsBonus,
						pistonsNeighbour);

		byte turboMaxLVL = -1;
		byte[] turboLVLs = {3, 5, 10};
		double[] turboRegUpgrades = {0, 0, 0, 0, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, RegVals.decimals + 0.4};
		double[] turboNeighbour = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, RegVals.decimals + 0.1};
		String[] turboGold = {"+1 nos bottle",
				"-10% sale on everything",
				"-50% sale " + UPGRADE_NAMES[turboID] + ",\n"
				+ "-50% sale " + UPGRADE_NAMES[pistonsID]};
		String[] turboNormal = {"Free " + UPGRADE_NAMES[nosID] + " upgrade", "Free " + UPGRADE_NAMES[nosID] + " upgrade",
				"+60% nos ms"};
		UpgradeAction[] turboBonus = {(UpgradeAction) (regularValues, player, rep, gold, test) -> {
			if (gold) {
				rep.add(Rep.nosSize, 1);
			} else {
				lastFocusedUpgrade.offer(nosID);
				upgradeReferences[nosID].upgrade(player, null, false);
			}
		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			if (gold) {
				for (int i = 0; i < Upgrades.UPGRADE_NAMES.length; i++) {
					player.getBank().addSale(0.9f, i, turboID);
				}
			} else {
				lastFocusedUpgrade.offer(nosID);
				upgradeReferences[nosID].upgrade(player, null, false);
			}

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			
			if (gold) {
				player.getBank().addSale(0.5f, turboID, turboID);
				player.getBank().addSale(0.5f, pistonsID, turboID);
			} else {
				rep.mul(Rep.nosMs, 1.6f);
			}

		}};
		createUpgrade(turboID, turboMaxLVL, turboLVLs, turboRegUpgrades,
				"  Gives turbo kW (%kW of base kW)\n"
						+ "  Spooling (bar) up the turbo\n"
						+ "  gives you your turbo kWs!\n"
						+ "    ========\n"
						+ "  Hold Q: multiply turbo kW by 5!",
						turboGold, turboNormal, turboBonus,
						turboNeighbour);



		byte blockMaxLVL = -1;
		byte[] blockLVLs = {3, 4, 8, 9};
		double[] blockRegUpgrades = {0, 0, 0, 100, 1.14f};
		double[] blockNeighbour = {0, 0, 0, 1.02};
		String[] blockGold = {"+20% tb time", "-25% sale on everything",
				"-20% sale on " + UPGRADE_NAMES[gearID], "+18% kW instead from " + UPGRADE_NAMES[blockID] };
		String[] blockNormal = {"+5% TB time",
				"+130% more tb from " + UPGRADE_NAMES[tbID],
				"-1 ton if you weight >2 ton", "+750 kW instead from " + UPGRADE_NAMES[blockID]};
		UpgradeAction[] blockBonus = {(UpgradeAction) (regularValues, player, rep, gold, test) -> {

			double value;

			if (gold)
				value = 1.2f;
			else
				value = 1.05f;

			rep.mul(Rep.tbMs, value);

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {

			if (gold) {
				for (int i = 0; i < Upgrades.UPGRADE_NAMES.length; i++) {
					player.getBank().addSale(0.75f, i, blockID);
				}
			} else {
				upgradeReferences[tbID].getRegVals().multiplyAllValues(2.3f);
			}

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {

			if (gold) {
				player.getBank().addSale(0.6, gearID, blockID);
			} else {
				if (rep.get(Rep.kg) >= 2000)
					rep.add(Rep.kg, -1000);
			}

		},
		(UpgradeAction) (regularValues, player, rep, gold, test) -> {

			double value = 0;
			
			if (gold) {
				value = 1.18;
			} else {
				value = 750;
			}

			upgradeReferences[blockID].getRegVals().values[Rep.kW] = value;
			
		}};
		createUpgrade(blockID, blockMaxLVL, blockLVLs, blockRegUpgrades,
						  "  This tile's \"+x kW\" is based on\n"
						+ "  your kg at the time of purchase.",
				(UpgradeAction) (regularValues, player, rep, gold, test) -> {
					regularValues.upgrade(rep);
					if (!test && upgradeReferences[blockID].getLVL() < blockLVLs[3])
						resetBlock(rep);

				}, blockGold, blockNormal, blockBonus,
				blockNeighbour);

		int vmoneyinc = 10;
		byte moneyMaxLVL = 5;
		byte[] moneyLVLs = {3, 5};
		double[] moneyRegUpgrades = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12};
		double[] moneyNeighbour = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5};
		String[] moneyGold = {"3 random free upgrades\n"
				+ "and 1 victory gold!", "+100 v$"};
		String[] moneyNormal = {"Earn 1 victory gold!\n"
				+ "Meaning you get 1 gold per race", "+30 v$"};
		UpgradeAction[] moneyBonus = {(UpgradeAction) (regularValues, player, rep, gold, test) -> {
			if (gold) {
				int amount = 3;
				Stack<Byte> gotThese = new Stack<>();

				if (Game.DEBUG) {
					upgradeReferences[0].upgrade(player, null, false);
					lastFocusedUpgrade.offer((byte) 0);
					upgradeReferences[2].upgrade(player, null, false);
					lastFocusedUpgrade.offer((byte) 2);
				} else {
					while (amount > 0) {
						byte free = (byte) Features.ran.nextInt(upgradeReferences.length);
						if (free == moneyID) continue;
						
						if (upgradeReferences[free].upgrade(player, null, false)) {
							lastFocusedUpgrade.offer(free);
							gotThese.push(free);
							amount--;
						}
					}
				}
				String message = "you got:\n";
				while (!gotThese.isEmpty()) {
					message += "\"" + UPGRADE_NAMES[gotThese.pop()] + "\""
							+ (gotThese.size() > 1
									? ", "
									: (gotThese.size() != 0 ? " and " : ""));
				}

				SceneHandler.showMessage(message);
			}
			
			rep.add(Rep.vgold, 1);

		}, (UpgradeAction) (regularValues, player, rep, gold, test) -> {
			int val = 0;
			if (gold) 
				val = 100;
			else
				val = 30;
			rep.add(Rep.vmoney, val);
		}};
		createUpgrade(moneyID, moneyMaxLVL, moneyLVLs, moneyRegUpgrades,
						 "  Eco upgrade.\n"
						+ "  v$ and vgold give money and gold\n"
						+ "  after each race!\n"
						+ "  Next tile gives +" + vmoneyinc + " v$ more",
				(UpgradeAction) (regularValues, player, rep, gold, test) -> {
					if (regularValues == null)
						regularValues = upgradeReferences[moneyID].getRegVals();
					
					regularValues.upgrade(rep);
					if(!test)
						upgradeReferences[moneyID].getRegVals().values[Rep.vmoney] += vmoneyinc;

				}, moneyGold, moneyNormal, moneyBonus, moneyNeighbour);
	}

//	private void addByPercent(RegVals reg, int type, double amount) {
//		if (reg.isTypeNormal(type)) {
//			if (reg.getValue(type) != 0)
//				reg.addValue(type, (int) (reg.getValue(type) * amount));
//			else
//				reg.addValue(type, 1.0 + amount);
//		} else if (reg.isTypePercent(type)) {
//			reg.addValue(type, amount);
//		}		
//	}
//	
//	private void addByDecimal(RegVals reg, int type, double amount) {
//		if (reg.getValue(type) == 0) {
//			reg.addValue(type, 20.0 + amount);
//		} else if (reg.isTypeDecimal(type)) {
//			reg.addValue(type, amount);
//		}
//	}

	public void resetTowardsCar(Player player, GameMode gm) {
		fuelCollectedKW = 2;
		for(int i = 0; i < upgradeReferences.length; i++) {
			upgradeReferences[i].reset(originalUpgradeValues[i], gm);
		}
		
		int start = supplementaryID + 1;
		for (int i = 0; i < start; i++) {
			unlockedIds[i] = true;
		}
		for (int i = start; i < unlockedIds.length; i++) {
			unlockedIds[i] = false;
		}
		
		player.getBank().resetSales();
		resetBlock(player.getCarRep());
	}
	
	private void resetBlock(Rep rep) {
		// thoroughbred får 100 % mer
		upgradeReferences[blockID].getRegVals().values[Rep.kW] = (int) (rep.get(Rep.kg) / 16f * (rep.getNameID() == 3 ? 2f : 1));
	}

	private void createUpgrade(byte id, byte maxLVL, byte[] bonusLVLs,
			double[] regUpgrades, String info,
			String[] bonusTextGold, String[] bonusTextNormal,
			UpgradeAction[] bonus, double[] neighbourModifier) {
		createUpgrade(id, maxLVL, bonusLVLs, regUpgrades, info,
		(regularValues, player, rep, gold, test) -> {
			if (regularValues == null)
				upgradeReferences[id].getRegVals().upgrade(rep);
			else
				regularValues.upgrade(rep);
		}
		, bonusTextGold, bonusTextNormal, bonus, 
		neighbourModifier);
	}
	
	private void createUpgrade(byte id, byte maxLVL, byte[] bonusLVLs,
			double[] regUpgrades, String info, UpgradeAction upgrade,
			String[] bonusTextGold, String[] bonusTextNormal,
			UpgradeAction[] bonus,
			double[] neighbourModifier) {
		RegVals regularValues = new RegVals(regUpgrades);
		RegVals neighbourValues = new RegVals(neighbourModifier);
		
		upgradeReferences[id] = new Upgrade(id, maxLVL, bonusLVLs, regularValues, neighbourValues);

		originalUpgradeValues[id] = regUpgrades;
		
		upgradeReferences[id].setUpgrade(upgrade);

		UPGRADE_HEIGHTS[id] = bonusLVLs.length;
		upgradeReferences[id].setBonuses(bonus);
		
		Upgrades.info[id] = info;
		Upgrades.bonusesTextsGold[id] = bonusTextGold;
		Upgrades.bonusesTexts[id] = bonusTextNormal;
	}

	public Upgrade instantiateTile(int possibleId) {
		if (unlockedIds[possibleId] && !upgradeReferences[possibleId].isFullyUpgraded())
			return upgradeReferences[possibleId];
		return null;
	}
	
	public Player upgradeClone(Player player, Upgrade upgrade, Vec2 pos, boolean test) {
		var clone = player.getClone();
		upgrade.upgrade(clone, pos, test);
		return clone;
	}

	public Upgrade getUpgrade(int i) {
		return upgradeReferences[i];
	}

	public Upgrade[] getUpgrades() {
		return upgradeReferences;
	}

	public void setLastFocusedUpgrade(byte upgrade) {
//		lastFocusedUpgrade.clear();
		lastFocusedUpgrade.offer(upgrade);
	}

	public int pollLastFocusedUpgrade() {
		return lastFocusedUpgrade.isEmpty() ? -1 : lastFocusedUpgrade.poll();
	}

	public void setAudio(AudioRemote audio) {
		this.audio = audio;
	}
	
	@Override
	public void getCloneString(StringBuilder outString, int lvlDeep, String splitter, boolean test, boolean all) {
		if (lvlDeep > 0)
			outString.append(splitter);
		lvlDeep++;
		outString.append(fuelCollectedKW);
		outString.append(splitter + fuelAmount);
		for (int i = 0; i < upgradeReferences.length; i++) {
			outString.append(splitter + (unlockedIds[i] ? 1 : 0));
			upgradeReferences[i].getCloneString(outString, lvlDeep, splitter, test, all);
		}
	}

	@Override
	public void setCloneString(String[] cloneString, AtomicInteger fromIndex) {
		fuelCollectedKW = Double.valueOf(cloneString[fromIndex.getAndIncrement()]);
		fuelAmount = Integer.valueOf(cloneString[fromIndex.getAndIncrement()]);

		for (int i = 0; i < upgradeReferences.length; i++) {
			unlockedIds[i] = Integer.valueOf(cloneString[fromIndex.getAndIncrement()]) != 0;
			upgradeReferences[i].setCloneString(cloneString, fromIndex);
		}
	}

	public static String GetRealUpgradeName(byte nameID, int lvl) {
		if (nameID == fuelID) {
			return fuelNames[lvl];
		}
		
		return UPGRADE_NAMES[nameID];
	}

}
