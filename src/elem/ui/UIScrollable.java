package elem.ui;

import static org.lwjgl.nuklear.Nuklear.nk_end;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.Nuklear;
import org.lwjgl.system.MemoryStack;

import engine.io.Window;

public class UIScrollable implements IUIObject {

	protected UIFont font;
	private boolean dynamicRowHeight;
	
	private int rowsAmount, rowsFrom, rowsMax;
	protected float rowHeight;
	private float paddingX = 0, paddingY = 0;
	private final UIWindowInfo window;
	private boolean bottomHeavy, scrollable = true;
	private final ArrayList<IUIObject> rows;
	private Consumer<Integer> scrollingAction;
	
	public UIScrollable(int sceneIndex, float x, float y, float width, float height) {
		window = UISceneInfo.createWindowInfo(sceneIndex,
				x, 
				y, width, height);
		rows = new ArrayList<>();
	}

	public UIScrollable(UIFont uiFont, int sceneIndex, float x, float y, float width, float height) {
		this(sceneIndex, x, y, width, height);
		if (uiFont == null) {
			System.out.println("why are you giving scrollable label a null font?");
			return;
		}
		font = uiFont;
		if (dynamicRowHeight = uiFont.getHeight() <= 0)
			uiFont.resizeFont(height / 24f); 
	}

	public void setPadding(float x, float y) {
		paddingX = x;
		paddingY = y;
	}

	@Override
	public void layout(NkContext ctx, MemoryStack stack) {

		boolean changeFont = font != null;
		
		if(changeFont) 
			Nuklear.nk_style_push_font(ctx, font.getFont());
		
		if (window.begin(ctx, stack, paddingX, paddingY, 0, 0)) {

			if(dynamicRowHeight)
				rowHeight = font.getHeight() * 1.1f;
			else
				rowHeight = Window.HEIGHT / 32f;
			
			rowsAmount = (int) (window.height / rowHeight) - 1;
			
			if(rows != null) {
				rowsMax = rows.size();
				for (int i = rowsFrom; i <  rowsFrom + rowsAmount; i++) { // FIXME maybe split labels when text is being added and set instead
					
					if(i >= rowsMax)
						break;
					
					if (!rows.get(i).getClass().equals(UILabelRow.class)) 
						nk_layout_row_dynamic(ctx, rowHeight, 1); // nested row
					rows.get(i).layout(ctx, stack);
				}
			}

		}
		nk_end(ctx);
		
		if(changeFont) 
			Nuklear.nk_style_pop_font(ctx);
	}
	
	public void scroll(float y) {
		if (scrollable && window.focus) {
			int direction = y > 0 ? 1 : -1;
			int newRowsFrom = rowsFrom - direction;
			
			if (newRowsFrom >= 0 && newRowsFrom < rowsMax - rowsAmount + 5) // padding
				rowsFrom = newRowsFrom;
			
			if (scrollingAction != null)
				scrollingAction.accept(rowsFrom);
		}
	}

	public void addObject(IUIObject obj) {
		rows.add(obj);
		updateRowsFrom();
	}

	public void addText(String text) {
		addText(text, Nuklear.NK_TEXT_ALIGN_LEFT | Nuklear.NK_TEXT_ALIGN_MIDDLE);
	}
	
	public void addText(String text, int options) {
		for(UILabel label : UILabel.split(text, "\n")) {
			rows.add(label);
			label.setOptions(options);
		}
		updateRowsFrom();
	}
	
	public void addText(UILabel[] labels) {
		Collections.addAll(this.rows, labels);
		updateRowsFrom();
	}
	
	public void addText(ArrayList<UILabel> labels) {
		this.rows.addAll(labels);
		updateRowsFrom();
	}

	public String getText() {
		if(rows == null || rows.size() == 0)
			return "";
		StringBuilder text = new StringBuilder();
		for(int i = 0; i < rows.size(); i++) {
			if (rows.get(i).getClass().equals(UILabel.class))
				text.append(((UILabel) rows.get(i)).getText()).append("\n");
		}
		text.substring(0, text.length() - 2);
		
		return text.toString();
	}

	public void setText(String text) {
		rows.clear();
		Collections.addAll(rows, UILabel.split(text, "\n"));
		updateRowsFrom();
	}
	
	public void setText(IUIObject[] labels) {
		this.rows.clear();
		Collections.addAll(this.rows, labels);
		updateRowsFrom();
	}
	
	public void clear() {
		this.rows.clear();
		updateRowsFrom();
	}
	
	private void updateRowsFrom() {
		if (!bottomHeavy) {
			rowsFrom = 0;
		} else {
			rowsFrom = rowsMax - rowsAmount + 2;
			if (rowsFrom < 0)
				rowsFrom = 0;
		}
	}

	public UIWindowInfo getWindow() {
		return window;
	}

	public void setBottomHeavy(boolean b) {
		this.bottomHeavy = b;
	}

	public void setScrollable(boolean b) {
		this.scrollable = b;
	}

	public float getRowHeight() {
		return rowHeight;
	}

	public int getScrollIndex() {
		return rowsFrom;
	}

	public void setScrollIndex(int scrollIndex) {
		rowsFrom = scrollIndex;
	}
	
	public void addScrollingAction(Consumer<Integer> action) {
		scrollingAction = action;
	}

	public IUIObject[] getList() {
		return rows.toArray(new IUIObject[0]);
	}

}
