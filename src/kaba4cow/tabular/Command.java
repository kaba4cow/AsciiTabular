package kaba4cow.tabular;

import java.io.File;
import java.util.HashMap;
import java.util.prefs.Preferences;

import kaba4cow.ascii.toolbox.files.TableFile;
import kaba4cow.ascii.toolbox.files.TableFile.Table;
import kaba4cow.ascii.toolbox.maths.Maths;

public enum Command {

	EXIT("exit", "", "Closes the program") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			savePreferences();
			exit = true;
		}
	},
	HELP("help", "", "Prints all available commands") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			for (Command command : values()) {
				output.append("-> " + command.name.toUpperCase() + " " + command.parameters + "\n");
				output.append(command.description + "\n\n");
			}
		}
	},
	ECHO("echo", "[message]", "Prints a message") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			for (int i = 0; i < numParameters; i++)
				output.append(parameters[i] + " ");
			output.append('\n');
		}
	},
	COLOR_B("color-b", "[color]", "Sets background color (000-FFF)") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			try {
				backgroundColor = Integer.parseInt(parameters[0], 16);
			} catch (NumberFormatException e) {
				invalidParameters();
			}
		}
	},
	COLOR_F("color-f", "[color]", "Sets foreground color (000-FFF)") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			try {
				foregroundColor = Integer.parseInt(parameters[0], 16);
			} catch (NumberFormatException e) {
				invalidParameters();
			}
		}
	},
	CD("cd", "[path]", "Changes current directory") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			String path = parameters[0];
			File file;
			if (path.equals("..")) {
				if (directory.getParentFile() != null)
					directory = directory.getParentFile();
			} else {
				file = new File(path);
				if (file.isDirectory())
					directory = file;
				else {
					path = directory.getAbsolutePath() + "\\" + path;
					file = new File(path);
					if (file.isDirectory())
						directory = file;
					else
						output.append(path + " is not a directory\n");
				}
			}
		}
	},
	MD("md", "[name]", "Creates new directory") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			String name = parameters[0];
			File file = new File(directory.getAbsolutePath() + "/" + name);
			if (!file.mkdirs())
				output.append("Could not create directory\n");
		}
	},
	DIR("dir", "", "Prints all files in current directory") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			File[] files = directory.listFiles();
			for (File file : files)
				output.append("-> " + file.getName() + "\n");
		}
	},
	PROJ_OPEN("proj-open", "[name]", "Opens a project with specified name") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			File file = new File(directory.getAbsolutePath() + "/" + parameters[0]);
			if (!file.exists())
				output.append("File not found\n");
			else {
				project = TableFile.read(file);
				projectName = file.getName();
				table = null;
			}
		}
	},
	PROJ_SAVE("proj-save", "", "Saves current project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (project == null)
				output.append("No project selected\n");
			else if (TableFile.write(project, directory + "/" + projectName))
				output.append("Project saved\n");
			else
				output.append("Could not save the project\n");
		}
	},
	PROJ_RENAME("proj-rename", "[name]", "Renames current project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			if (project == null)
				output.append("No project selected\n");
			else
				projectName = parameters[0];
		}
	},
	PROJ_INFO("proj-info", "", "Prints information about current project") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (project == null) {
				output.append("No project selected\n");
				return;
			}

			HashMap<String, Table> tables = project.getTables();
			output.append("Name: " + projectName + "\n");
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
							table.getItems().size()));
					output.append('\n');
				}
				output.append('\n');
			}

		}
	},
	TABLE_OPEN("table-open", "[name]", "Opens a table with specified name") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			if (project == null)
				output.append("No project selected\n");
			else if (project.containsTable(parameters[0]))
				table = project.getTable(parameters[0]);
			else
				output.append("Table not found\n");
		}
	},
	TABLE_ADD("table-add", "[name]", "Adds new table") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			if (project == null)
				output.append("No project selected\n");
			else if (project.containsTable(parameters[0]))
				output.append("Table already exists\n");
			else
				project.addTable(parameters[0]);
		}
	},
	TABLE_DELETE("table-delete", "[name]", "Deletes table with specified name") {
		@Override
		public void execute(String[] parameters, int numParameters) {
			if (invalidParameters(numParameters, 1))
				return;

			if (project == null)
				output.append("No project selected\n");
			else if (!project.containsTable(parameters[0]))
				output.append("Table not found\n");
			else
				project.getTables().remove(parameters[0]);
		}
	};

	private final String name;
	private final String parameters;
	private final String description;

	private static String[] parameterArray = new String[32];
	private static boolean exit = false;
	private static String projectName = null;
	private static TableFile project = null;
	private static Table table = null;

	private static Preferences preferences = Preferences.userNodeForPackage(Command.class);
	private static int backgroundColor = preferences.getInt("color-b", 0x000);
	private static int foregroundColor = preferences.getInt("color-f", 0xFFF);
	private static File directory = new File(preferences.get("home", System.getProperty("user.dir")));

	private static StringBuilder output = new StringBuilder();

	private Command(String name, String parameters, String description) {
		this.name = name;
		this.parameters = parameters;
		this.description = description;
	}

	public abstract void execute(String[] parameters, int numParameters);

	public static boolean processCommand(String line) {
		output.append('\n');
		String name = getCommandName(line);
		int numParameters = getCommandParameters(name, line);

		Command command = Command.search(name);

		if (line.isEmpty())
			output.append('\n');
		else if (command == null)
			output.append("Unknown command: " + line + "\n");
		else
			command.execute(parameterArray, numParameters);
		output.append('\n');

		if (exit)
			return true;

		if (project == null)
			output.append(directory.getAbsolutePath() + ": ");
		else
			output.append(directory.getAbsolutePath() + " -> " + projectName + ": ");

		return false;
	}

	private static String getCommandName(String string) {
		String name = "";
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char c = string.charAt(i);
			if (c == ' ')
				break;
			else
				name += c;
		}
		return name;
	}

	private static int getCommandParameters(String name, String string) {
		if (name.length() == string.length())
			return 0;

		string = string.substring(name.length()) + " ";
		final int length = string.length();

		int index = 0;
		boolean backslash = false;
		boolean space = false;
		String token = "";

		for (int i = 1; i < length; i++) {
			char c = string.charAt(i);
			if (!space && !backslash && c == ' ') {
				parameterArray[index++] = token;
				token = "";
				space = true;
			} else if (c == '\\') {
				backslash = true;
				space = false;
			} else {
				token += c;
				space = false;
				backslash = false;
			}

			if (index >= parameterArray.length)
				break;
		}

		for (int i = index; i < parameterArray.length; i++)
			parameterArray[i] = null;

		return index;
	}

	public static String getOutput() {
		String string = output.toString();
		output = new StringBuilder();
		return string;
	}

	public static Table getTable() {
		return table;
	}

	public static void closeTable() {
		table = null;
	}

	public static Command search(String name) {
		for (Command command : values())
			if (command.name.equalsIgnoreCase(name))
				return command;
		return null;
	}

	private static boolean invalidParameters(int numParameters1, int numParameters2) {
		if (numParameters1 == numParameters2)
			return false;
		invalidParameters();
		return true;
	}

	private static void invalidParameters() {
		output.append("Invalid parameters\n");
	}

	public static int getBackgroundColor() {
		return backgroundColor;
	}

	public static int getForegroundColor() {
		return foregroundColor;
	}

	private static void savePreferences() {
		preferences.put("color-b", Integer.toString(backgroundColor));
		preferences.put("color-f", Integer.toString(foregroundColor));
		preferences.put("home", directory.getAbsolutePath());
	}

}
