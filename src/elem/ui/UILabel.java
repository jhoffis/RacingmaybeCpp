package elem.ui;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

public class UILabel implements IUIObject {

	protected String text;
	protected int options;

	public UILabel(String text) {
		this.text = text;
		this.options = Nuklear.NK_TEXT_ALIGN_LEFT | Nuklear.NK_TEXT_ALIGN_MIDDLE;
	}
	
	public UILabel(String text, int options) {
		this.text = text;
		this.options = options;
	}

	public UILabel() {
		this("");
	}

	public void setOptions(int options) {
		this.options = options;
	}

	@Override
	public void layout(NkContext ctx, MemoryStack stack) {

		String[] coloredText = text.split("#");
		if (coloredText.length <= 1 || coloredText[1].isBlank()) {
			Nuklear.nk_label(ctx, coloredText[0], options);
		} else {

			NkColor color = null;
			switch (coloredText[1].toUpperCase()) {
			case "G":
				color = NkColor.mallocStack(stack).set((byte) 0, (byte) 255, (byte) 0, (byte) 255);
				break;
			case "R":
				color = NkColor.mallocStack(stack).set((byte) 255, (byte) 0, (byte) 0, (byte) 255);
				break;
			case "WON":
				color = NkColor.mallocStack(stack).set((byte) 14, (byte) 171, (byte) 129, (byte) 255);
				break;
			case "AI":
				color = NkColor.mallocStack(stack).set((byte) 25, (byte) 29, (byte) 144, (byte) 255);
				break;
			case "DNF":
				color = NkColor.mallocStack(stack).set((byte) 200, (byte) 21, (byte) 21, (byte) 255);
				break;
			case "NF":
				color = NkColor.mallocStack(stack).set((byte) 64, (byte) 64, (byte) 64, (byte) 255);
				break;
			case "BLACK":
				color = NkColor.mallocStack(stack).set((byte) 0, (byte) 0, (byte) 0, (byte) 255);
				break;
			case "LBEIGE":
				color = NkColor.mallocStack(stack).set((byte) 224, (byte) 209, (byte) 195, (byte) 255);
				break;
			case "GRAY":
				color = NkColor.mallocStack(stack).set((byte) 150, (byte) 150, (byte) 150, (byte) 255);
				break;
			case "TUR":
				color = NkColor.mallocStack(stack).set((byte) 0, (byte) 128, (byte) 255, (byte) 255);
				break;
			case "BUR":
				color = NkColor.mallocStack(stack).set((byte) 255, (byte) 0, (byte) 191, (byte) 255);
				break;
			case "WEAKGOLD":
				color = NkColor.mallocStack(stack).set((byte) 237, (byte) 217, (byte) 157, (byte) 255);
				break;
			}

			if (color != null) {
				Nuklear.nk_label_colored(ctx, coloredText[0], options, color);
			}
			else {
				try {
					colorError(coloredText[1], text);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
	}
	
	private void colorError(String coloredText, String text) throws Exception {
		System.out.println("no color with " + coloredText);
		System.out.println(text);
		throw new Exception();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		if (text == null)
			text = "";
		this.text = text;
	}

	public static UILabel[] split(String str, String splitter) {
		String[] texts = str.split(splitter);
		return create(texts);
	}

	public static UILabel[] create(String[] texts) {
		UILabel[] labels = new UILabel[texts.length];

		for (int i = 0; i < labels.length; i++) {
			labels[i] = new UILabel(texts[i]);
		}
		return labels;
	}

}
