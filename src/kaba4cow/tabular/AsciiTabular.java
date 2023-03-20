package kaba4cow.tabular;

import java.util.LinkedList;

import kaba4cow.ascii.MainProgram;
import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.drawers.BoxDrawer;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.input.Input;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.input.Mouse;
import kaba4cow.ascii.toolbox.Colors;
import kaba4cow.ascii.toolbox.files.TableFile;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.tools.Table;
import kaba4cow.console.ConsoleProgram;

public class AsciiTabular extends ConsoleProgram implements MainProgram {

	public static String projectName = null;
	public static TableFile project = null;
	public static Table table = null;

	private int scrollX;
	private int scrollY;
	private int maxScrollX;
	private int maxScrollY;

	private static int tableColumn = -1;
	private static int tableRow = -1;
	private static int newTableColumn = -1;
	private static int newTableRow = -1;
	private static boolean menu = false;

	private GUIFrame menuFrame;

	public AsciiTabular() {
		super(AsciiTabular.class, "TABULAR by kaba4cow");
		Commands.init();
	}

	@Override
	public void init() {
		scrollX = 0;
		scrollY = 0;
		menu = false;

		menuFrame = new MenuFrame();
	}

	@Override
	public void update(float dt) {
		if (table == null)
			updateConsole(projectName);
		else
			updateGUI();
	}

	public void updateGUI() {
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			if (menu)
				menu = false;
			else {
				tableColumn = -1;
				tableRow = -1;
				table = null;
			}
			return;
		}

		if (Mouse.isKeyDown(Mouse.RIGHT))
			menu = !menu;

		if (menu) {
			menuFrame.update();
			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
				menu = false;
		} else if (Mouse.isKeyDown(Mouse.LEFT)) {
			tableColumn = newTableColumn;
			tableRow = newTableRow;
		}

		if (tableColumn != -1) {
			String string;
			if (tableRow == -1)
				string = table.getColumn(tableColumn);
			else
				string = table.getCell(tableColumn, tableRow);
			if (Keyboard.isKeyDown(Keyboard.KEY_DELETE))
				string = "";
			else
				string = Input.typeString(string);
			if (tableRow == -1)
				table.setColumn(tableColumn, string);
			else
				table.setCell(tableColumn, tableRow, string);

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
			else if (tableColumn >= table.getColumns().size())
				tableColumn = table.getColumns().size() - 1;
		}

		if (!menu) {
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
	}

	@Override
	public void render() {
		updateWindow();
		if (table == null)
			renderConsole();
		else
			renderGUI();
	}

	public void renderGUI() {
		Display.setDrawCursor(true);

		LinkedList<String> columns = table.getColumns();
		LinkedList<Table.Row> rows = table.getRows();

		int mX = Mouse.getTileX();
		int mY = Mouse.getTileY();

		newTableColumn = -1;
		newTableRow = -1;

		int x = 0;
		int y = 4 - scrollY;
		for (int j = 0; j < rows.size(); j++) {
			Table.Row item = rows.get(j);
			x = -scrollX;
			y += 2;
			for (int i = 0; i < columns.size(); i++) {
				int width = 2 + Maths.max(table.getColumnWidth(i), table.getColumn(i).length() / 2);
				if (i >= item.size())
					item.add("");
				int textColor = (i == tableColumn && j == tableRow) ? Colors.swap(consoleColor) : consoleColor;
				BoxDrawer.drawBox(x, y, width, 2, true, consoleColor);
				Drawer.drawLine(x + 1, y + 1, x + width - 1, y + 1, ' ', textColor);
				Drawer.drawString(x + 1, y + 1, false, item.get(i), textColor);

				if (mY > 4 && mY == y + 1 && mX > x && mX < x + width) {
					newTableColumn = i;
					newTableRow = j;
				}

				x += width;
			}
		}

		Drawer.fillRect(0, 0, Display.getWidth(), 5, false, Glyphs.SPACE, consoleColor);
		Drawer.drawLine(0, 5, Display.getWidth(), 5, Glyphs.BOX_DRAWINGS_DOUBLE_HORIZONTAL, consoleColor);
		Drawer.drawString(0, 0, false,
				"TABLE [" + table.getName() + "] : " + table.columns() + " COLUMNS " + table.rows() + " ROWS",
				consoleColor);

		x = -scrollX;
		for (int i = 0; i < columns.size(); i++) {
			int width = 2 + Maths.max(table.getColumnWidth(i), table.getColumn(i).length() / 2);
			int textColor = (tableRow == -1 && i == tableColumn) ? Colors.swap(consoleColor) : consoleColor;
			BoxDrawer.drawBox(x, 1, width, 3, false, consoleColor);
			Drawer.fillRect(x + 1, 2, width - 1, 2, false, Glyphs.SPACE, textColor);
			Drawer.drawString(x + 1, 2, false, width - 1, table.getColumn(i), textColor);

			if (mY >= 2 && mY <= 3 && mX > x && mX < x + width) {
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
			maxScrollY = y + 6 - Display.getHeight();

		if (menu) {
			BoxDrawer.disableCollision();
			menuFrame.setColor(consoleColor);
			menuFrame.render(Display.getWidth() / 2, Display.getHeight() / 2, Display.getWidth() / 3,
					2 * Display.getHeight() / 3, true);
			BoxDrawer.enableCollision();
		}
	}

	public static void closeMenu() {
		menu = false;
	}

	public static int getTableColumn() {
		return tableColumn;
	}

	public static int getTableRow() {
		return tableRow;
	}

	public static void main(String[] args) {
		Engine.init("Tabular", 60);
		Engine.start(new AsciiTabular());
	}

}
