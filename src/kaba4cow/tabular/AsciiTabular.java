package kaba4cow.tabular;

import java.util.LinkedList;

import kaba4cow.ascii.MainProgram;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.core.Input;
import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.drawing.drawers.BoxDrawer;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.toolbox.Colors;
import kaba4cow.ascii.toolbox.files.TableFile;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.tools.Table;
import kaba4cow.console.Console;
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

	private static float saveTime = 1f;

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
			updateGUI(dt);
	}

	public void updateGUI(float dt) {
		saveTime += dt;

		if (Input.isKeyDown(Input.KEY_ESCAPE)) {
			if (menu)
				menu = false;
			else {
				tableColumn = -1;
				tableRow = -1;
				table = null;
				saveTime = 1f;
			}
			return;
		}

		if (Input.isButtonDown(Input.RIGHT))
			menu = !menu;

		if (Input.isKeyDown(Input.KEY_S) && Input.isKey(Input.KEY_CONTROL_LEFT)) {
			saveProject();
			saveTime = 0f;
		}

		if (menu) {
			menuFrame.update();
			if (Input.isKeyDown(Input.KEY_ESCAPE))
				menu = false;
		} else if (Input.isButtonDown(Input.LEFT)) {
			tableColumn = newTableColumn;
			tableRow = newTableRow;
		}

		if (tableColumn != -1) {
			String string;
			if (tableRow == -1)
				string = table.getColumn(tableColumn);
			else
				string = table.getCell(tableColumn, tableRow);
			string = Input.typeString(string);
			if (tableRow == -1)
				table.setColumn(tableColumn, string);
			else
				table.setCell(tableColumn, tableRow, string);

			if (Input.isKeyDown(Input.KEY_UP))
				tableRow--;
			else if (Input.isKeyDown(Input.KEY_DOWN))
				tableRow++;
			else if (Input.isKeyDown(Input.KEY_LEFT))
				tableColumn--;
			else if (Input.isKeyDown(Input.KEY_RIGHT))
				tableColumn++;

			if (tableRow < -1)
				tableRow = -1;
			if (tableColumn < 0)
				tableColumn = 0;
			else if (tableColumn >= table.getColumns().size())
				tableColumn = table.getColumns().size() - 1;
		}

		if (!menu) {
			if (Input.isKey(Input.KEY_SHIFT_LEFT))
				scrollX -= 6 * Input.getScroll();
			else
				scrollY -= 3 * Input.getScroll();

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
		Window.setDrawCursor(true);

		LinkedList<String> columns = table.getColumns();
		LinkedList<Table.Row> rows = table.getRows();

		int mX = Input.getTileX();
		int mY = Input.getTileY();

		newTableColumn = -1;
		newTableRow = -1;

		int x = 0;
		int y = 4 - scrollY;
		for (int j = 0; j < rows.size(); j++) {
			Table.Row item = rows.get(j);
			x = -scrollX;
			y += 2;
			for (int i = 0; i < columns.size(); i++) {
				int width = 4 + Maths.max(table.getColumnWidth(i), table.getColumn(i).length() / 2);
				if (i >= item.size())
					item.add("");
				int textColor = (i == tableColumn && j == tableRow) ? Colors.swap(consoleColor) : consoleColor;
				BoxDrawer.drawBox(x, y, width, 2, true, consoleColor);
				Drawer.drawLine(x + 1, y + 1, x + width - 1, y + 1, ' ', textColor);
				Drawer.drawString(x + 1, y + 1, false, "<" + item.get(i) + ">", textColor);

				if (mY > 4 && mY == y + 1 && mX > x && mX < x + width) {
					newTableColumn = i;
					newTableRow = j;
				}

				x += width;
			}
		}

		Drawer.fillRect(0, 0, Window.getWidth(), 5, false, Glyphs.SPACE, consoleColor);
		Drawer.drawLine(0, 5, Window.getWidth(), 5, Glyphs.BOX_DRAWINGS_DOUBLE_HORIZONTAL, consoleColor);
		if (saveTime < 1f)
			Drawer.drawString(0, 0, false, "PROJECT SAVED", consoleColor);
		else
			Drawer.drawString(0, 0, false,
					"TABLE [" + table.getName() + "] : " + table.columns() + " COLUMNS " + table.rows() + " ROWS",
					consoleColor);

		x = -scrollX;
		for (int i = 0; i < columns.size(); i++) {
			int width = 4 + Maths.max(table.getColumnWidth(i), table.getColumn(i).length() / 2);
			int textColor = (tableRow == -1 && i == tableColumn) ? Colors.swap(consoleColor) : consoleColor;
			BoxDrawer.drawBox(x, 1, width, 3, false, consoleColor);
			Drawer.fillRect(x + 1, 2, width - 1, 2, false, Glyphs.SPACE, textColor);
			Drawer.drawString(x + 1, 2, false, width - 1, "<" + table.getColumn(i) + ">", textColor);

			if (mY >= 2 && mY <= 3 && mX > x && mX < x + width) {
				newTableRow = -1;
				newTableColumn = i;
			}

			x += width;
		}

		x += scrollX;
		if (x < Window.getWidth())
			maxScrollX = 0;
		else
			maxScrollX = x + 2 - Window.getWidth();

		y += scrollY;
		if (y < Window.getHeight())
			maxScrollY = 0;
		else
			maxScrollY = y + 6 - Window.getHeight();

		if (menu) {
			BoxDrawer.disableCollision();
			menuFrame.setColor(consoleColor);
			menuFrame.render(Window.getWidth() / 2, Window.getHeight() / 2, Window.getWidth() / 3,
					2 * Window.getHeight() / 3, true);
			BoxDrawer.enableCollision();
		}
	}

	public static void closeMenu() {
		menu = false;
	}

	public static boolean saveProject() {
		return TableFile.write(project, Console.getDirectory().getAbsolutePath() + "/" + projectName);
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
