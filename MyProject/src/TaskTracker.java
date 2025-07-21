
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.Comparator;
import java.nio.file.*;
import java.io.*;

class Task {
	private String name, dueDate, category, priority;

	public Task(String name, String dueDate, String category, String priority) {
		this.name = name;
		this.dueDate = dueDate;
		this.category = category;
		this.priority = priority;
	}

	public String getDueDate() {
		return dueDate;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return String.format("📌 %s | Due: %s | %s | %s", name, dueDate, category, priority);
	}

	public String toFileFormat() {
		return String.join(";", name, dueDate, category, priority);
	}

	public static Task fromFileFormat(String line) {
		String[] parts = line.split(";");
		return new Task(parts[0], parts[1], parts[2], parts[3]);
	}
}

public class TaskTracker extends JFrame implements ActionListener {
	private Container cp;
	private JPanel formPanel, listPanel, buttonPanel;
	private JLabel taskNameLabel, dueDateLabel, categoryLabel, priorityLabel;
	private JTextField taskNameField;
	private JFormattedTextField dueDateField;
	private JComboBox<String> categoryBox, priorityBox;
	private JButton addButton, clearButton, deleteButton, sortButton;
	private JList<String> taskJList;
	private DefaultListModel<String> listModel;

	private List<Task> tasks;
	private final String FILE_PATH = "tasks.txt";

	public TaskTracker() {
		setTitle("Student Task Tracker");
		setSize(550, 550);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		cp = getContentPane();
		cp.setLayout(new BorderLayout());

		tasks = new ArrayList<>();
		loadTasks();

		formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
		formPanel.setBorder(BorderFactory.createTitledBorder("Add New Task"));
		formPanel.setBackground(new Color(245, 245, 245));

		taskNameLabel = new JLabel("Task Name:");
		taskNameField = new JTextField();

		dueDateLabel = new JLabel("Due Date (YYYY-MM-DD):");
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		format.setLenient(false);
		dueDateField = new JFormattedTextField(format);
		dueDateField.setText("2025-01-01");

		categoryLabel = new JLabel("Category:");
		categoryBox = new JComboBox<>(new String[] { "Homework", "Project", "Exam", "Other" });

		priorityLabel = new JLabel("Priority:");
		priorityBox = new JComboBox<>(new String[] { "High", "Medium", "Low" });

		addButton = new JButton("Add Task");
		addButton.setBackground(new Color(39, 174, 96));
		addButton.setForeground(Color.WHITE);
		addButton.addActionListener(this);

		clearButton = new JButton("Clear Tasks");
		clearButton.setBackground(new Color(192, 57, 43));
		clearButton.setForeground(Color.WHITE);
		clearButton.addActionListener(this);

		deleteButton = new JButton("Delete Selected");
		deleteButton.setBackground(new Color(52, 73, 94));
		deleteButton.setForeground(Color.WHITE);
		deleteButton.addActionListener(this);

		sortButton = new JButton("Sort by Date");
		sortButton.setBackground(new Color(41, 128, 185));
		sortButton.setForeground(Color.WHITE);
		sortButton.addActionListener(this);

		formPanel.add(taskNameLabel);
		formPanel.add(taskNameField);
		formPanel.add(dueDateLabel);
		formPanel.add(dueDateField);
		formPanel.add(categoryLabel);
		formPanel.add(categoryBox);
		formPanel.add(priorityLabel);
		formPanel.add(priorityBox);
		formPanel.add(addButton);
		formPanel.add(clearButton);

		listModel = new DefaultListModel<>();
		taskJList = new JList<>(listModel);
		taskJList.setFont(new Font("SansSerif", Font.PLAIN, 14));
		taskJList.setBorder(BorderFactory.createTitledBorder("Task List"));

		listPanel = new JPanel(new BorderLayout());
		listPanel.add(new JScrollPane(taskJList), BorderLayout.CENTER);

		buttonPanel = new JPanel();
		buttonPanel.add(deleteButton);
		buttonPanel.add(sortButton);

		cp.add(formPanel, BorderLayout.NORTH);
		cp.add(listPanel, BorderLayout.CENTER);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		updateTaskList();
	}

	private void loadTasks() {
		try {
			if (Files.exists(Paths.get(FILE_PATH))) {
				List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));
				for (String line : lines) {
					tasks.add(Task.fromFileFormat(line));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveTasks() {
		try {
			List<String> lines = new ArrayList<>();
			for (Task t : tasks) {
				lines.add(t.toFileFormat());
			}
			Files.write(Paths.get(FILE_PATH), lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updateTaskList() {
		listModel.clear();
		for (Task task : tasks) {
			listModel.addElement(task.toString());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addButton) {
			String name = taskNameField.getText().trim();
			String date = dueDateField.getText().trim();
			String category = (String) categoryBox.getSelectedItem();
			String priority = (String) priorityBox.getSelectedItem();

			if (!name.isEmpty() && !date.isEmpty()) {
				try {
					new SimpleDateFormat("yyyy-MM-dd").parse(date);
					Task task = new Task(name, date, category, priority);
					tasks.add(task);
					saveTasks();
					updateTaskList();
					taskNameField.setText("");
					dueDateField.setText("2025-01-01");
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this, "Please fill all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
			}
		} else if (e.getSource() == clearButton) {
			tasks.clear();
			saveTasks();
			updateTaskList();
		} else if (e.getSource() == deleteButton) {
			int selected = taskJList.getSelectedIndex();
			if (selected != -1) {
				tasks.remove(selected);
				saveTasks();
				updateTaskList();
			} else {
				JOptionPane.showMessageDialog(this, "Please select a task to delete.", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		} else if (e.getSource() == sortButton) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			tasks.sort((t1, t2) -> {
				try {
					return sdf.parse(t1.getDueDate()).compareTo(sdf.parse(t2.getDueDate()));
				} catch (ParseException ex) {
					return 0;
				}
			});
			updateTaskList();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new TaskTracker().setVisible(true));
	}
}