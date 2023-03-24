package kaba4cow.tabular;

import java.io.File;
import java.util.HashMap;

import kaba4cow.ascii.toolbox.files.TableFile;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.tools.Table;
import kaba4cow.console.Command;
import kaba4cow.console.Console;

public final class Commands {

	private Commands() {

	}

	public static void init() {
		new Command("proj-create", "[name]", "Creates new project with specified name") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 1, output))
					return;

				AsciiTabular.project = new TableFile();
				AsciiTabular.projectName = parameters[0];
				AsciiTabular.table = null;
				output.append("Project created\n");
			}
		};

		new Command("proj-open", "[name]", "Opens a project with specified name") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 1, output))
					return;

				File file = new File(Console.getDirectory().getAbsolutePath() + "/" + parameters[0]);
				if (!file.exists())
					output.append("File not found\n");
				else {
					AsciiTabular.project = TableFile.read(file);
					AsciiTabular.projectName = file.getName();
					AsciiTabular.table = null;
					output.append("Project opened\n");
				}
			}
		};

		new Command("proj-save", "", "Saves current project") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (AsciiTabular.project == null)
					output.append("No project selected\n");
				else if (AsciiTabular.saveProject())
					output.append("Project saved\n");
				else
					output.append("Could not save the project\n");
			}
		};

		new Command("proj-rename", "[name]", "Renames current project") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 1, output))
					return;

				if (AsciiTabular.project == null)
					output.append("No project selected\n");
				else {
					AsciiTabular.projectName = parameters[0];
					output.append("Project renamed\n");
				}
			}
		};

		new Command("proj-info", "", "Prints information about current project") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (AsciiTabular.project == null) {
					output.append("No project selected\n");
					return;
				}

				HashMap<String, Table> tables = AsciiTabular.project.getTables();
				output.append("Name: " + AsciiTabular.projectName + "\n");
				if (tables.isEmpty())
					output.append("Tables: none\n");
				else {
					output.append("Tables: " + tables.size() + "\n\n");
					int maxLength = 0;
					for (String key : tables.keySet())
						maxLength = Maths.max(maxLength, tables.get(key).getName().length());

					output.append(String.format("%" + maxLength + "s | %9s | %6s", "Name", "Columns", "Items"));
					output.append('\n');
					for (int i = 0; i < maxLength + 22; i++)
						output.append('-');
					output.append('\n');
					for (String key : tables.keySet()) {
						Table table = tables.get(key);
						output.append(String.format("%" + maxLength + "s | %9s | %6s", key, table.getColumns().size(),
								table.rows()));
						output.append('\n');
						output.append(String.format("%" + maxLength + "s | %9s | %6s", "", "", ""));
						output.append('\n');
					}
					output.append('\n');
				}

			}
		};

		new Command("table-open", "[name]", "Opens a table with specified name") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 1, output))
					return;

				if (AsciiTabular.project == null)
					output.append("No project selected\n");
				else if (AsciiTabular.project.containsTable(parameters[0]))
					AsciiTabular.table = AsciiTabular.project.getTable(parameters[0]);
				else
					output.append("Table not found\n");
			}
		};

		new Command("table-add", "[name]", "Adds new table") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 1, output))
					return;

				if (AsciiTabular.project == null)
					output.append("No project selected\n");
				else if (AsciiTabular.project.containsTable(parameters[0]))
					output.append("Table already exists\n");
				else {
					AsciiTabular.project.addTable(parameters[0]);
					output.append("Table added\n");
				}
			}
		};

		new Command("table-rename", "[old name] [new name]", "Renames table with specified name") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 2, output))
					return;

				if (AsciiTabular.project == null)
					output.append("No project selected\n");
				else if (!AsciiTabular.project.containsTable(parameters[0]))
					output.append("Table not found\n");
				else if (AsciiTabular.project.containsTable(parameters[1]))
					output.append("Table with this name already exists\n");
				else {
					AsciiTabular.project.renameTable(parameters[0], parameters[1]);
					output.append("Table renamed\n");
				}
			}
		};

		new Command("table-delete", "[name]", "Deletes table with specified name") {
			@Override
			public void execute(String[] parameters, int numParameters, StringBuilder output) {
				if (invalidParameters(numParameters, 1, output))
					return;

				if (AsciiTabular.project == null)
					output.append("No project selected\n");
				else if (!AsciiTabular.project.containsTable(parameters[0]))
					output.append("Table not found\n");
				else {
					AsciiTabular.project.getTables().remove(parameters[0]);
					output.append("Table deleted\n");
				}
			}
		};
	}

}