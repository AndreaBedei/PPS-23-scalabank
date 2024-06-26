package scalabank.gui;

import scala.Tuple2;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;

class SwingFunctionalFacade {

    public interface Frame {
        String CLOSED = "CLOSED";

        /**
         * @return the underlying JFrame
         */
        JFrame jFrame();

        /**
         * Sets the size of the window
         * @param width the width of the window
         * @param height the height of the window
         * @return the frame itself
         */
        Frame setSize(int width, int height);

        /**
         * Adds a new view (panel) to the window
         * @param name the name of the view
         * @param layout the layout of the view
         * @return the frame itself
         */
        Frame addView(String name, LayoutManager layout);

        /**
         * Displays a view on the window
         * @param name the selected view
         * @return the frame itself
         */
        Frame showView(String name);

        /**
         * Adds a panel to another panel
         * @param name the name of the panel
         * @param layout the layout of the panel
         * @param panel the container in which the panel is inserted
         * @param constraints the constraints on the new panel
         * @return the frame itself
         */
        Frame addPanel(String name, LayoutManager layout, String panel, Object constraints);

        /**
         * Adds a button to a panel
         * @param name the name of the button
         * @param text the text of the button
         * @param panel the container in which the button is inserted
         * @param constraints the constraints on the new button
         * @return the frame itself
         */
        Frame addButton(String name, String text, String panel, Object constraints);

        /**
         * Adds a label to a panel
         * @param name the name of the label
         * @param text the text of the label
         * @param panel the container in which the label is inserted
         * @param constraints the constraints on the new label
         * @return the frame itself
         */
        Frame addLabel(String name, String text, String panel, Object constraints);

        /**
         * Updates the text of a label
         * @param name the name of the label
         * @param text the new text
         * @return the frame itself
         */
        Frame changeLabel(String name, String text);

        /**
         * Adds an input element to a panel
         * @param name the name of the input element
         * @param columns the width of the input element in columns
         * @param panel the container in which the input element is inserted
         * @param constraints the constraints on the new input element
         * @return the frame itself
         */
        Frame addInput(String name, int columns, String panel, Object constraints);

        /**
         * Returns the text inside an input element
         * @param name the name of the input element
         * @return the text in the input element
         */
        String getInputText(String name);

        /**
         * Sets the text inside an input element
         * @param name the name of the input element
         * @param text the new text
         */
        Frame setInputText(String name, String text);

        /**
         * Adds an combobox to a panel
         * @param name the name of the combobox
         * @param options the list of options for the combobox
         * @param panel the container in which the combobox is inserted
         * @param constraints the constraints on the new combobox
         * @return the frame itself
         */
        Frame addComboBox(String name, String[] options, String panel, Object constraints);

        /**
         * Returns the selected element inside a combobox, which will be null in case of no selection
         * @param name the name of the combobox
         * @return the selected element in the combobox
         */
        String getComboBoxSelection(String name);

        /**
         * Updates the contents of a combobox
         * @param name the name of the combobox
         * @param options the list of options for the combobox
         * @return the frame itself
         */
        Frame updateComboBox(String name, String[] options);

        /**
         * Adds a list to a panel
         * @param name the name of the list
         * @param contents the contents of the list
         * @param panel the container in which the list is inserted
         * @param constraints the constraints on the new list
         * @return the frame itself
         */
        Frame addList(String name, Vector<String> contents, String panel, Object constraints);

        /**
         * Updates the contents of a list
         * @param name the name of the list
         * @param contents the new contents of the list
         * @return the frame itself
         */
        Frame updateList(String name, Vector<String> contents);

        /**
         * Creates a spacer inside a panel
         * @param width the width of the spacer
         * @param height the height of the spacer
         * @param panel the container in which the spacer is inserted
         * @param constraints the constraints on the spacer
         * @return the frame itself
         */
        Frame addSpacer(int width, int height, String panel, Object constraints);

        /**
         * Displays the window
         * @return the frame itself
         */
        Frame show();

        /**
         * Returns a supplier for the events generated by the window
         * @return the supplier of eventsa
         */
        Supplier<String> events();
    }

    // TODO: change
    public static Frame createFrame(){
        return new FrameImpl();
    }

    private static class FrameImpl implements Frame {
        private final JFrame jframe = new JFrame();
        private final Map<String, JButton> buttons = new HashMap<>();
        private final Map<String, JLabel> labels = new HashMap<>();
        private final Map<String, JTextField> textFields = new HashMap<>();
        private final Map<String, JComboBox<String>> comboBoxes = new HashMap<>();
        private final Map<String, JList<String>> lists = new HashMap<>();
        private String currentView = "";
        private final Map<String, JPanel> views = new HashMap<>();
        private final Map<String, JPanel> panels = new HashMap<>();

        private final LinkedBlockingQueue<String> eventQueue = new LinkedBlockingQueue<>();

        private final Supplier<String> events = () -> {
            try{
                return eventQueue.take();
            } catch (InterruptedException e){
                return "";
            }
        };
        public FrameImpl() {
            this.jframe.setLayout(new FlowLayout());
            this.jframe.setResizable(false);
            this.jframe.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e){
                    try {
                        eventQueue.put(Frame.CLOSED);
                    } catch (InterruptedException ex){}
                }
            });
        }

        @Override
        public JFrame jFrame() {
            return jframe;
        }

        @Override
        public Frame setSize(int width, int height) {
            this.jframe.setSize(width, height);
            return this;
        }

        private void verifyPanelExists(String name) {
            if (!this.panels.containsKey(name)) {
                throw new IllegalArgumentException("A panel with name " + name + " does not exist.");
            }
        }

        @Override
        public Frame addButton(String name, String text, String panel, Object constraints) {
            if (this.buttons.containsKey(name)) {
                throw new IllegalArgumentException("A button with name " + name + " already exists.");
            }
            verifyPanelExists(panel);

            JButton jb = new JButton(text);
            jb.setActionCommand(name);
            this.buttons.put(name, jb);

            jb.addActionListener(e -> {
                try {
                    eventQueue.put(name);
                } catch (InterruptedException ex){}
            });
            this.panels.get(panel).add(jb, constraints);
            return this;
        }

        @Override
        public Frame addLabel(String name, String text, String panel, Object constraints) {
            if (this.labels.containsKey(name)) {
                throw new IllegalArgumentException("A label with name " + name + " already exists.");
            }
            verifyPanelExists(panel);

            JLabel jl = new JLabel(text);
            this.labels.put(name, jl);
            this.panels.get(panel).add(jl, constraints);
            return this;
        }

        @Override
        public Frame changeLabel(String name, String text) {
            if (!this.labels.containsKey(name)) {
                throw new IllegalArgumentException("A label with name " + name + " does not exist.");
            }

            this.labels.get(name).setText(text);
            return this;
        }

        @Override
        public Frame addInput(String name, int columns, String panel, Object constraints) {
            if (this.textFields.containsKey(name)) {
                throw new IllegalArgumentException("An input with name " + name + " already exists.");
            }
            verifyPanelExists(panel);

            JTextField jt = new JTextField("", columns);
            this.textFields.put(name, jt);
            this.panels.get(panel).add(jt, constraints);
            return this;
        }

        @Override
        public String getInputText(String name) {
            if (!this.textFields.containsKey(name)) {
                throw new IllegalArgumentException("An input with name " + name + " does not exist.");
            }

            return this.textFields.get(name).getText();
        }

        @Override
        public Frame setInputText(String name, String text) {
            if (!this.textFields.containsKey(name)) {
                throw new IllegalArgumentException("An input with name " + name + " does not exist.");
            }

            this.textFields.get(name).setText(text);
            return this;
        }

        @Override
        public Frame addComboBox(String name, String[] options, String panel, Object constraints) {
            if (this.comboBoxes.containsKey(name)) {
                throw new IllegalArgumentException("A combobox with name " + name + " already exists.");
            }
            verifyPanelExists(panel);

            JComboBox<String> jc = new JComboBox<>(options);
            this.comboBoxes.put(name, jc);
            this.panels.get(panel).add(jc, constraints);
            return this;
        }

        @Override
        public String getComboBoxSelection(String name) {
            if (!this.comboBoxes.containsKey(name)) {
                throw new IllegalArgumentException("A combobox with name " + name + " does not exist.");
            }

            return (String) this.comboBoxes.get(name).getSelectedItem();
        }

        @Override
        public Frame updateComboBox(String name, String[] options) {
            if (!this.comboBoxes.containsKey(name)) {
                throw new IllegalArgumentException("A combobox with name " + name + " does not exist.");
            }

            var comboBox = this.comboBoxes.get(name);
            comboBox.removeAllItems();
            comboBox.setModel(new DefaultComboBoxModel<>(options));
            return this;
        }


        @Override
        public Frame addList(String name, Vector<String> contents, String panel, Object constraints) {
            if (this.lists.containsKey(name)) {
                throw new IllegalArgumentException("A list with name " + name + " already exists.");
            }
            verifyPanelExists(panel);

            JList<String> jl = new JList<>(contents);
            this.lists.put(name, jl);
            var pane = new JScrollPane(jl);
            pane.setPreferredSize(new Dimension(pane.getPreferredSize().width, 70));
            this.panels.get(panel).add(pane, constraints);
            return this;
        }

        @Override
        public Frame updateList(String name, Vector<String> contents) {
            if (!this.lists.containsKey(name)) {
                throw new IllegalArgumentException("A list with name " + name + " does not exist.");
            }

            this.lists.get(name).setListData(contents);
            return this;
        }

        @Override
        public Frame addView(String name, LayoutManager layout) {
            if (this.panels.containsKey(name)) {
                throw new IllegalArgumentException("A view or panel with name " + name + " already exists.");
            }

            JPanel jp = new JPanel(layout);
            jp.setVisible(false);
            this.views.put(name, jp);
            this.panels.put(name, jp);
            this.jframe.getContentPane().add(jp);
            return this;
        }

        @Override
        public Frame showView(String name) {
            if (!this.views.containsKey(name)) {
                throw new IllegalArgumentException("A view with name " + name + " does not exist.");
            }
            
            if (!this.currentView.isEmpty()) {
                this.views.get(currentView).setVisible(false);
            }
            this.currentView = name;
            this.views.get(currentView).setVisible(true);
            return this;
        }

        @Override
        public Frame addPanel(String name, LayoutManager layout, String panel, Object constraints) {
            if (this.panels.containsKey(name)) {
                throw new IllegalArgumentException("A panel with name " + name + " already exists.");
            }
            verifyPanelExists(panel);

            JPanel jp = new JPanel();
            if (layout instanceof BoxLayout) {
                jp.setLayout(new BoxLayout(jp, ((BoxLayout) layout).getAxis()));
            } else {
                jp.setLayout(layout);
            }
            
            this.panels.put(name, jp);
            this.panels.get(panel).add(jp, constraints);
            jp.setVisible(true);
            return this;
        }

        @Override
        public Frame addSpacer(int width, int height, String panel, Object constraints) {
            verifyPanelExists(panel);

            this.panels.get(panel).add(Box.createRigidArea(new Dimension(width, height)), constraints);
            return this;
        }

        @Override
        public Supplier<String> events() {
            return events;
        }

        @Override
        public Frame show() {
            this.jframe.setVisible(true);
            return this;
        }

    }
}
