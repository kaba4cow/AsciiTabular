package kaba4cow.tabular;

import java.util.function.Consumer;

import kaba4cow.ascii.drawing.Glyphs;
import kaba4cow.ascii.gui.GUIButton;
import kaba4cow.ascii.gui.GUIFrame;
import kaba4cow.ascii.toolbox.files.TableFile;
import kaba4cow.ascii.toolbox.tools.TableSorter;
import kaba4cow.console.Console;

public class MenuFrame extends GUIFrame {

	public MenuFrame() {
		super(0, false, false);
		setTitle("Menu");

		new GUIButton(this, 0, "Add column", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				AsciiTabular.table.addColumn("");
				AsciiTabular.closeMenu();
			}
		});
		new GUIButton(this, 0, "Insert column", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				AsciiTabular.table.insertColumn(AsciiTabular.getTableColumn(), "");
				AsciiTabular.closeMenu();
			}
		});
		new GUIButton(this, 0, "Delete column", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				AsciiTabular.table.removeColumn(AsciiTabular.getTableColumn());
				AsciiTabular.closeMenu();
			}
		});
		new GUIButton(this, 0, "Add row", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				AsciiTabular.table.addRow();
				AsciiTabular.closeMenu();
			}
		});
		new GUIButton(this, 0, "Insert row", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				AsciiTabular.table.insertRow(AsciiTabular.getTableRow());
				AsciiTabular.closeMenu();
			}
		});
		new GUIButton(this, 0, "Delete row", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				AsciiTabular.table.removeRow(AsciiTabular.getTableRow());
				AsciiTabular.closeMenu();
			}
		});
		new GUIButton(this, 0, "Clear cell", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				if (AsciiTabular.getTableRow() == 0)
					AsciiTabular.table.setColumn(AsciiTabular.getTableColumn(), "");
				else
					AsciiTabular.table.setCell(AsciiTabular.getTableColumn(), AsciiTabular.getTableRow(), "");
				AsciiTabular.closeMenu();
			}
		});
		new GUIButton(this, 0, "Sort table " + Glyphs.RIGHTWARDS_ARROW, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				TableSorter.sort(AsciiTabular.table, AsciiTabular.getTableColumn(), true);
				AsciiTabular.closeMenu();
			}
		});
		new GUIButton(this, 0, "Sort table " + Glyphs.LEFTWARDS_ARROW, new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				TableSorter.sort(AsciiTabular.table, AsciiTabular.getTableColumn(), false);
				AsciiTabular.closeMenu();
			}
		});
		new GUIButton(this, 0, "Save project", new Consumer<Object>() {
			@Override
			public void accept(Object t) {
				TableFile.write(AsciiTabular.project,
						Console.getDirectory().getAbsolutePath() + "/" + AsciiTabular.projectName);
				AsciiTabular.closeMenu();
			}
		});
	}

}
