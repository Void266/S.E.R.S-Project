package javamongo1.sersproject.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.prefs.Preferences;

/** Shared RMMC-inspired colors and component styling for the Swing interface. */
public final class RmmcTheme {
    private static final Preferences PREFS = Preferences.userNodeForPackage(RmmcTheme.class);
    private static volatile boolean darkMode = PREFS.getBoolean("darkMode", false);
    private static final Color DARK_BACKGROUND = new Color(24, 29, 39);
    private static final Color DARK_SURFACE = new Color(34, 41, 54);
    private static final Color DARK_ELEVATED = new Color(42, 50, 65);
    private static final Color DARK_TEXT = new Color(235, 239, 247);
    private static final Color DARK_BORDER = new Color(65, 77, 97);
    private static final Color DARK_NAVY = new Color(20, 35, 67);
    private static final Color DARK_BLUE = new Color(69, 99, 160);
    public static final Color NAVY = new Color(13, 26, 91);
    public static final Color BLUE = new Color(25, 47, 145);
    public static final Color PALE_BLUE = new Color(232, 237, 250);
    public static final Color IVORY = Color.WHITE;
    public static final Color SURFACE = Color.WHITE;
    public static final Color INK = new Color(29, 32, 43);
    public static final Color MUTED = new Color(105, 111, 127);
    public static final Color BORDER = new Color(220, 223, 232);
    private static final String LOGO_PATH = "/javamongo1/sersproject/logo/RBRamonLogo.png";
    private static final String[] BACKGROUND_PATHS = {
        "/javamongo1/sersproject/logo/background 1.jpg",
        "/javamongo1/sersproject/logo/background 2.jpg",
        "/javamongo1/sersproject/logo/background 3.jpg"
    };

    private RmmcTheme() { }

    public static boolean isDarkMode() { return darkMode; }

    public static void setDarkMode(boolean enabled) {
        darkMode = enabled;
        PREFS.putBoolean("darkMode", enabled);
    }

    public static ImageIcon logoIcon(int width, int height) {
        URL resource = RmmcTheme.class.getResource(LOGO_PATH);
        Image source = null;
        if (resource != null) {
            source = new ImageIcon(resource).getImage();
        } else {
            File file = new File("src/main/java/javamongo1/sersproject/logo/RBRamonLogo.png");
            if (!file.isFile()) file = new File("src/main/resources/javamongo1/sersproject/logo/RBRamonLogo.png");
            if (file.isFile()) source = new ImageIcon(file.getAbsolutePath()).getImage();
        }
        return source == null ? new ImageIcon() : new ImageIcon(source.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    public static void styleFrame(JFrame frame) {
        if (frame.getContentPane() instanceof JComponent root)
            root.putClientProperty("sers.applicationRoot", Boolean.TRUE);
        frame.getContentPane().setBackground(darkMode ? DARK_BACKGROUND : IVORY);
        frame.getRootPane().setBackground(darkMode ? DARK_BACKGROUND : IVORY);
        frame.setIconImage(logoIcon(96, 96).getImage());
        applyToTree(frame.getContentPane());
    }

    /** Installs a cover-scaled school image beneath a screen's Swing components. */
    public static void installBackground(JFrame frame, int imageIndex) {
        if (imageIndex < 0 || imageIndex >= BACKGROUND_PATHS.length) return;
        URL resource = RmmcTheme.class.getResource(BACKGROUND_PATHS[imageIndex]);
        Image source = resource == null ? null : new ImageIcon(resource).getImage();
        if (source == null) {
            String fileName = "background " + (imageIndex + 1) + ".jpg";
            File file = new File("src/main/java/javamongo1/sersproject/logo/" + fileName);
            if (!file.isFile()) file = new File("src/main/resources/javamongo1/sersproject/logo/" + fileName);
            if (file.isFile()) source = new ImageIcon(file.getAbsolutePath()).getImage();
        }
        if (source == null) return;

        final Image background = source;
        JPanel surface = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                int width = getWidth(), height = getHeight();
                if (width <= 0 || height <= 0) return;
                double scale = Math.max((double) width / background.getWidth(this),
                        (double) height / background.getHeight(this));
                int drawWidth = (int) Math.ceil(background.getWidth(this) * scale);
                int drawHeight = (int) Math.ceil(background.getHeight(this) * scale);
                graphics.drawImage(background, (width - drawWidth) / 2,
                        (height - drawHeight) / 2, drawWidth, drawHeight, this);
                Graphics2D overlay = (Graphics2D) graphics.create();
                overlay.setColor(new Color(226, 239, 255, 118));
                overlay.fillRect(0, 0, width, height);
                overlay.dispose();
            }
        };
        surface.setOpaque(false);
        frame.setContentPane(surface);
    }

    /** Login background without extra decorative shapes over the form. */
    public static void installLoginBackground(JFrame frame) {
        URL resource = RmmcTheme.class.getResource(BACKGROUND_PATHS[1]);
        Image source = resource == null ? null : new ImageIcon(resource).getImage();
        if (source == null) {
            File file = new File("src/main/java/javamongo1/sersproject/logo/background 2.jpg");
            if (!file.isFile()) file = new File("src/main/resources/javamongo1/sersproject/logo/background 2.jpg");
            if (file.isFile()) source = new ImageIcon(file.getAbsolutePath()).getImage();
        }
        if (source == null) return;

        final Image background = source;
        JPanel surface = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                int width = getWidth(), height = getHeight();
                if (width <= 0 || height <= 0) return;
                double scale = Math.max((double) width / background.getWidth(this),
                        (double) height / background.getHeight(this));
                int drawWidth = (int) Math.ceil(background.getWidth(this) * scale);
                int drawHeight = (int) Math.ceil(background.getHeight(this) * scale);
                graphics.drawImage(background, (width - drawWidth) / 2,
                        (height - drawHeight) / 2, drawWidth, drawHeight, this);

                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new GradientPaint(0, 0, new Color(235, 244, 255, 54),
                        0, height, new Color(255, 255, 255, 92)));
                g.fillRect(0, 0, width, height);
                g.dispose();
            }
        };
        surface.setOpaque(false);
        frame.setContentPane(surface);
    }

    public static void applyToTree(Container root) {
        if (root instanceof JComponent component) applyPalette(component);
        for (Component child : root.getComponents()) applyComponent(child);
        root.repaint();
    }

    private static void applyComponent(Component component) {
        if (component instanceof JComponent jc) applyPalette(jc);
        if (component instanceof Container container)
            for (Component child : container.getComponents()) applyComponent(child);
    }

    private static void applyPalette(JComponent component) {
        rememberLightStyle(component);
        Color originalBackground = (Color) component.getClientProperty("sers.lightBackground");
        Color originalForeground = (Color) component.getClientProperty("sers.lightForeground");

        if (component instanceof JButton button) {
            styleButton(button, Boolean.TRUE.equals(button.getClientProperty("sers.primaryButton")));
        } else if (component instanceof JTable table) {
            styleTable(table);
        } else if (component instanceof JTextComponent text) {
            text.setForeground(darkMode ? DARK_TEXT : INK);
            text.setBackground(darkMode ? DARK_ELEVATED : Color.WHITE);
            text.setCaretColor(darkMode ? DARK_TEXT : INK);
            text.setSelectionColor(darkMode ? DARK_BLUE : PALE_BLUE);
            if (darkMode) text.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DARK_BORDER),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            else text.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        } else if (component instanceof JLabel label) {
            if (darkMode && !Color.WHITE.equals(originalForeground)
                    && !(label.getIcon() != null && label.getText() == null)) {
                label.setForeground(isLowContrast(originalForeground) ? DARK_TEXT : originalForeground);
            } else {
                label.setForeground(isDarkPaletteColor(originalForeground) ? INK : originalForeground);
            }
        } else if (component instanceof JScrollPane scroll) {
            scroll.setBackground(darkMode ? DARK_SURFACE : Color.WHITE);
            if (scroll.getViewport() != null)
                scroll.getViewport().setBackground(darkMode ? DARK_SURFACE : Color.WHITE);
        } else {
            Object fixed = component.getClientProperty("sers.fixedNavy");
            if (component.isOpaque()) {
                if (darkMode) {
                    component.setBackground(Boolean.TRUE.equals(fixed) ? DARK_NAVY : DARK_SURFACE);
                    component.setForeground(DARK_TEXT);
                } else if (Boolean.TRUE.equals(component.getClientProperty("sers.applicationRoot"))) {
                    component.setBackground(IVORY);
                    component.setForeground(INK);
                } else {
                    component.setBackground(Boolean.TRUE.equals(fixed) ? NAVY : lightBackground(originalBackground));
                    component.setForeground(isDarkPaletteColor(originalForeground) ? INK : originalForeground);
                }
            }
        }
    }

    private static Color lightBackground(Color original) {
        if (original == null || isDarkPaletteColor(original)) return Color.WHITE;
        return original;
    }

    private static boolean isDarkPaletteColor(Color color) {
        return DARK_BACKGROUND.equals(color) || DARK_SURFACE.equals(color)
                || DARK_ELEVATED.equals(color) || DARK_NAVY.equals(color) || DARK_BLUE.equals(color);
    }

    private static void rememberLightStyle(JComponent component) {
        if (component.getClientProperty("sers.lightStyleSaved") != null) return;
        component.putClientProperty("sers.lightStyleSaved", Boolean.TRUE);
        component.putClientProperty("sers.lightBackground", component.getBackground());
        component.putClientProperty("sers.lightForeground", component.getForeground());
        if (component.getBorder() != null)
            component.putClientProperty("sers.lightBorder", component.getBorder());
        if (component instanceof JButton button)
            button.putClientProperty("sers.primaryButton", button.getBackground().equals(BLUE));
    }

    private static boolean isLowContrast(Color color) {
        if (color == null) return true;
        double luminance = (0.2126 * color.getRed() + 0.7152 * color.getGreen()
                + 0.0722 * color.getBlue()) / 255.0;
        return luminance < 0.58;
    }

    public static JPanel header(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBackground(darkMode ? DARK_NAVY : NAVY);
            header.putClientProperty("sers.fixedNavy", Boolean.TRUE);
            header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
            
        JLabel logo = new JLabel(logoIcon(58, 58));
            header.add(logo, BorderLayout.WEST);
            
        JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            
        JLabel heading = new JLabel(title);
            heading.setForeground(Color.WHITE);
            heading.setFont(new Font("SansSerif", Font.BOLD, 23));
        text.add(heading);
        
        if (subtitle != null && !subtitle.isBlank()) {
            JLabel sub = new JLabel(subtitle);
                sub.setForeground(new Color(220, 225, 244));
                sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
                
            text.add(Box.createVerticalStrut(3));
            text.add(sub);
        }
        
        header.add(text, BorderLayout.CENTER);
        return header;
    }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(darkMode ? DARK_SURFACE : SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(20, 22, 20, 22)));
        return panel;
    }

    public static void styleButton(JButton button, boolean primary) {
        if (button.getClientProperty("sers.primaryButton") == null)
            button.putClientProperty("sers.primaryButton", primary);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(primary && darkMode ? Color.WHITE : primary ? Color.WHITE : darkMode ? DARK_TEXT : NAVY);
        button.setBackground(primary ? (darkMode ? DARK_BLUE : BLUE) : darkMode ? DARK_ELEVATED : Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(true);
        button.setUI(new RoundedButtonUI(16));
        button.setBorder(new RoundedButtonBorder(primary ? (darkMode ? DARK_BLUE : BLUE) : darkMode ? DARK_BORDER : BORDER, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static final class RoundedButtonBorder extends AbstractBorder {
        private final Color outline;
        private final int radius;

        private RoundedButtonBorder(Color outline, int radius) {
            this.outline = outline;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
            if (!(component instanceof JButton button)) return;
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape rounded = new java.awt.geom.RoundRectangle2D.Float(x, y, width - 1, height - 1, radius, radius);
            g2.setColor(button.isEnabled() ? outline : BORDER);
            g2.draw(rounded);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(9, 16, 9, 16);
        }

        @Override
        public Insets getBorderInsets(Component component, Insets insets) {
            insets.set(9, 16, 9, 16);
            return insets;
        }
    }

    private static final class RoundedButtonUI extends BasicButtonUI {
        private final int radius;

        private RoundedButtonUI(int radius) {
            this.radius = radius;
        }

        @Override
        public void paint(Graphics graphics, JComponent component) {
            JButton button = (JButton) component;
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(button.isEnabled() ? button.getBackground() : (darkMode ? DARK_BORDER : new Color(230, 231, 235)));
            g2.fillRoundRect(0, 0, button.getWidth() - 1, button.getHeight() - 1, radius, radius);
            g2.dispose();
            super.paint(graphics, component);
        }
    }

    public static void styleField(JTextComponent field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setForeground(darkMode ? DARK_TEXT : INK);
        field.setBackground(darkMode ? DARK_ELEVATED : Color.WHITE);
        field.setCaretColor(darkMode ? DARK_TEXT : INK);
        Border padding = BorderFactory.createEmptyBorder(8, 10, 8, 10);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(darkMode ? DARK_BORDER : BORDER), padding));
    }

    public static void styleTable(JTable table) {
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setForeground(darkMode ? DARK_TEXT : INK);
        table.setBackground(darkMode ? DARK_SURFACE : Color.WHITE);
        table.setSelectionBackground(darkMode ? new Color(55, 72, 112) : PALE_BLUE);
        table.setSelectionForeground(darkMode ? Color.WHITE : NAVY);
        table.setRowHeight(30);
        table.setGridColor(darkMode ? DARK_BORDER : new Color(235, 237, 242));
        table.setShowVerticalLines(false);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(Color.WHITE);
        header.setBackground(darkMode ? DARK_NAVY : NAVY);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
    }

}
