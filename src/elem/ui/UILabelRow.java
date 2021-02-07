package elem.ui;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

public class UILabelRow implements IUIObject  {
	
	private final UILabel[] texts;
	private final float height;
	private final int columns;
	
	public UILabelRow(String text, String splitter, float height, int options) {
		this.texts = UILabel.split(text, splitter);
		for (var label : texts) {
			label.setOptions(options);
		}
		this.height = height;
		this.columns = texts.length;
	}
	
	public UILabelRow(String text, String splitter, String color, float height, int options) {
		this(text, splitter, height, options);
		for (var label : texts) {
			label.setText(label.getText() + color);
		}
	}

	@Override
	public void layout(NkContext ctx, MemoryStack stack) {
		Nuklear.nk_layout_row_dynamic(ctx, height, columns);
		for (UILabel str : texts) {
			str.layout(ctx, stack);
		}
	}
	
}
