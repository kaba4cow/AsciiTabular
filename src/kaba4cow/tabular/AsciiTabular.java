package kaba4cow.tabular;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Consumer;

import kaba4cow.ascii.MainProgram;
import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.drawers.BoxDrawer;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.drawing.gui.GUIButton;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.input.Mouse;
import kaba4cow.ascii.toolbox.Colors;
import kaba4cow.ascii.toolbox.tools.Table;
import kaba4cow.ascii.toolbox.tools.TableSorter;

public class AsciiTabular implements MainProgram {

	private static final char BACKSPACE = 0x0008;
	private static final char DELETE = 0x007F;

	private ArrayList<String> history;
	private int index;

	private StringBuilder builder;

	private String output;
	private String text;

	private int scroll;
	private int maxScroll;

	private int color = 0x000FFF;

	private int scrollX;
	private int scrollY;
	private int maxScrollX;
	private int maxScrollY;

	private int tableColumn;
	private int tableRow;
	private int newTableColumn;
	private int newTableRow;
	private boolean guiMenu;

	private GUIFrame menuFrame;

	public AsciiTabular() {

	}

	@Override
	public void init() {
		scroll = 0;
		maxScroll = 0;

		scrollX = 0;
		scrollY = 0;
		guiMenu = false;

		text = "";

		history = new ArrayList<>();
		index = 0;

		builder = new StringBuilder();
		Command.processCommand("");
		output = "TABULAR by kaba4cow" + Command.getOutput();

		menuFrame = new GUIFrame(color, false, false);
		menuFrame.setTitle("Menu");
		new GUIButton(menuFrame, -1, "Add column", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				Command.getTable().addColumn("");
				guiMenu = false;
			}
		});
		new GUIButton(menuFrame, -1, "Insert column", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				Command.getTable().insertColumn(tableColumn, "");
				guiMenu = false;
			}
		});
		new GUIButton(menuFrame, -1, "Delete column", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				Command.getTable().removeColumn(tableColumn);
				guiMenu = false;
			}
		});
		new GUIButton(menuFrame, -1, "Add row", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				Command.getTable().addItem("");
				guiMenu = false;
			}
		});
		new GUIButton(menuFrame, -1, "Insert row", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				Command.getTable().insertItem(tableRow, "");
				guiMenu = false;
			}
		});
		new GUIButton(menuFrame, -1, "Delete row", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				Command.getTable().removeItem(tableRow);
				guiMenu = false;
			}
		});
		new GUIButton(menuFrame, -1, "Clear cell", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				if (tableRow == -1)
					Command.getTable().setColumn(tableColumn, "");
				else
					Command.getTable().setItemString(tableRow, tableColumn, "");
				guiMenu = false;
			}
		});
		new GUIButton(menuFrame, -1, "Sort table " + Glyphs.RIGHTWARDS_ARROW, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				TableSorter.sort(Command.getTable(), tableColumn, true);
				guiMenu = false;
			}
		});
		new GUIButton(menuFrame, -1, "Sort table " + Glyphs.LEFTWARDS_ARROW, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				TableSorter.sort(Command.getTable(), tableColumn, false);
				guiMenu = false;
			}
		});
		new GUIButton(menuFrame, -1, "Save project", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				Command.processCommand("proj-save");
				guiMenu = false;
			}
		});
	}

	@Override
	public void update(float dt) {
		if (Command.getTable() == null)
			updateConsole();
		else
			updateGUI();
	}

	public void updateConsole() {
		scroll -= 2 * Mouse.getScroll();
		if (scroll < 0)
			scroll = 0;
		if (scroll > maxScroll)
			scroll = maxScroll;

		if (Keyboard.isKeyDown(Keyboard.KEY_ENTER)) {
			scroll = Integer.MAX_VALUE;
			maxScroll = Integer.MAX_VALUE;
			if (Command.processCommand(text))
				Engine.requestClose();
			else
				builder = new StringBuilder();
			String cmd = Command.getOutput();
			output += text + "\n" + cmd;
			if (history.isEmpty() || !history.get(history.size() - 1).equalsIgnoreCase(text))
				history.add(text);
			index = history.size();
		} else if (!history.isEmpty() && Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			index--;
			if (index < 0)
				index = history.size() - 1;
			builder = new StringBuilder(history.get(index));
		} else if (!history.isEmpty() && Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			index++;
			if (index >= history.size())
				index = 0;
			builder = new StringBuilder(history.get(index));
		} else if (Keyboard.isKey(Keyboard.KEY_CONTROL_LEFT) && Keyboard.isKeyDown(Keyboard.KEY_V)) {
			try {
				String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
						.getData(DataFlavor.stringFlavor);
				data = data.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
				builder.append(data);
			} catch (Exception e) {
			}
		} else if (Keyboard.getLastTyped() != null) {
			char c = Keyboard.getLastTyped().getKeyChar();
			if (c == BACKSPACE && builder.length() > 0)
				builder.deleteCharAt(builder.length() - 1);
			else if (c >= 32 && c < DELETE)
				builder.append(c);
			Keyboard.resetLastTyped();
		}

		text = builder.toString();
	}

	public void updateGUI() {
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			if (guiMenu)
				guiMenu = false;
			else {
				tableColumn = -1;
				tableRow = -1;
				Command.closeTable();
			}
			return;
		}

		if (Mouse.isKeyDown(Mouse.RIGHT))
			guiMenu = !guiMenu;

		if (guiMenu) {
			menuFrame.update();
			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
				guiMenu = false;
		} else if (Mouse.isKeyDown(Mouse.LEFT)) {
			tableColumn = newTableColumn;
			tableRow = newTableRow;
		}

		if (tableColumn != -1) {
			String string;
			if (tableRow == -1)
				string = Command.getTable().getColumn(tableColumn);
			else
				string = Command.getTable().getItemString(tableRow, tableColumn);
			if (Keyboard.isKeyDown(Keyboard.KEY_DELETE))
				string = "";
			else if (Keyboard.isKey(Keyboard.KEY_CONTROL_LEFT) && Keyboard.isKeyDown(Keyboard.KEY_V)) {
				try {
					String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
							.getData(DataFlavor.stringFlavor);
					data = data.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
					string += data;
				} catch (Exception e) {
				}
			} else if (Keyboard.getLastTyped() != null) {
				char c = Keyboard.getLastTyped().getKeyChar();
				if (c == BACKSPACE && string.length() > 0)
					string = string.substring(0, string.length() - 1);
				else if (c >= 32 && c < DELETE)
					string += c;
				Keyboard.resetLastTyped();
			}
			if (tableRow == -1)
				Command.getTable().setColumn(tableColumn, string);
			else
				Command.getTable().setItemString(tableRow, tableColumn, string);

			if (Keyboard.isKeyDown(Keyboard.KEY_UP))
				tableRow--;
			else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
				tableRow++;
			else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
				tableColumn--;
			else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
				tableColumn++;

			if (tableRow < -1)
				tableRow = -1;
			if (tableColumn < 0)
				tableColumn = 0;
			else if (tableColumn >= Command.getTable().getColumns().size())
				tableColumn = Command.getTable().getColumns().size() - 1;
		}

		if (Keyboard.isKey(Keyboard.KEY_SHIFT_LEFT))
			scrollX -= 6 * Mouse.getScroll();
		else
			scrollY -= 3 * Mouse.getScroll();

		if (scrollX < 0)
			scrollX = 0;
		else if (scrollX > maxScrollX)
			scrollX = maxScrollX;
		if (scrollY < 0)
			scrollY = 0;
		else if (scrollY > maxScrollY)
			scrollY = maxScrollY;
	}

	@Override
	public void render() {
		color = Colors.combine(Command.getBackgroundColor(), Command.getForegroundColor());
		Display.setBackground(' ', color);
		if (Command.getTable() == null)
			renderConsole();
		else
			renderGUI();
	}

	public void renderConsole() {
		Display.setDrawCursor(false);

		int x = 0;
		int y = -scroll;
		for (int i = 0; i < output.length(); i++) {
			char c = output.charAt(i);
			if (c == '\n') {
				x = 0;
				y++;
			} else if (c == '\t')
				x += 4;
			else
				Drawer.drawChar(x++, y, c, color);

			if (x >= Display.getWidth()) {
				x = 0;
				y++;
			}
		}

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			Drawer.drawChar(x++, y, c, color);

			if (x >= Display.getWidth()) {
				x = 0;
				y++;
			}
		}

		y += scroll;
		if (y < Display.getHeight())
			maxScroll = 0;
		else
			maxScroll = y + 5 - Display.getHeight();
	}

	public void renderGUI() {
		Display.setDrawCursor(true);

		Table table = Command.getTable();
		LinkedList<String> columns = table.getColumns();
		LinkedList<LinkedList<String>> items = table.getItems();

		int mX = Mouse.getTileX();
		int mY = Mouse.getTileY();

		newTableColumn = -1;
		newTableRow = -1;

		int x = 0;
		int y = 3 - scrollY;
		for (int j = 0; j < items.size(); j++) {
			LinkedList<String> item = items.get(j);
			x = -scrollX;
			y += 2;
			for (int i = 0; i < columns.size(); i++) {
				int width = table.getColumnWidth(i) + 1;
				if (i >= item.size())
					item.add("");
				int textColor = (i == tableColumn && j == tableRow) ? Colors.swap(color) : color;
				BoxDrawer.drawBox(x, y, width, 2, true, color);
				Drawer.drawLine(x + 1, y + 1, x + width - 1, y + 1, ' ', textColor);
				Drawer.drawString(x + 1, y + 1, false, item.get(i), textColor);

				if (mY > 4 && mY == y + 1 && mX > x && mX < x + width) {
					newTableColumn = i;
					newTableRow = j;
				}

				x += width;
			}
		}

		Drawer.fillRect(0, 0, Display.getWidth(), 4, false, ' ', color);
		Drawer.drawLine(0, 4, Display.getWidth(), 4, Glyphs.BOX_DRAWINGS_DOUBLE_HORIZONTAL, color);
		Drawer.drawString(0, 0, false, "TABLE [" + table.getName() + "] (ESCAPE to close)", color);

		x = -scrollX;
		for (int i = 0; i < columns.size(); i++) {
			int width = table.getColumnWidth(i) + 1;
			int textColor = (tableRow == -1 && i == tableColumn) ? Colors.swap(color) : color;
			BoxDrawer.drawBox(x, 1, width, 2, false, color);
			Drawer.drawLine(x + 1, 2, x + width - 1, 2, ' ', textColor);
			Drawer.drawString(x + 1, 2, false, table.getColumn(i), textColor);

			if (mY == 2 && mX > x && mX < x + width) {
				newTableRow = -1;
				newTableColumn = i;
			}

			x += width;
		}

		x += scrollX;
		if (x < Display.getWidth())
			maxScrollX = 0;
		else
			maxScrollX = x + 2 - Display.getWidth();

		y += scrollY;
		if (y < Display.getHeight())
			maxScrollY = 0;
		else
			maxScrollY = y + 5 - Display.getHeight();

		if (guiMenu) {
			BoxDrawer.disableCollision();
			menuFrame.setColor(color);
			menuFrame.render(Display.getWidth() / 2, Display.getHeight() / 2, Display.getWidth() / 3,
					2 * Display.getHeight() / 3, true);
			BoxDrawer.enableCollision();
		}
	}

	public static void main(String[] args) {
		Engine.init("Tabular", 60);
		Display.createFullscreen();
		Engine.start(new AsciiTabular());
	}

}
