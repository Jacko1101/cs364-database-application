import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;

public class MainFrame extends JFrame {
    private Database db;

    // ── JPanels ───────────────────────────────────────────────────────────────
    private JPanel loginPanel;
    private JPanel signupPanel;
    private JPanel libraryPanel;
    private JPanel userPanel;
    private JPanel gamePanel;
    private JPanel reviewsPanel;


    // ── Shared login state ────────────────────────────────────────────────────
    private boolean loginSuccess = false;
    private String  loggedInUser = null;   // store username after auth



    public MainFrame() {
        //execute prelogin
        this.setTitle("Interweb Game Database: Sign In");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 600);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        setVisible(true);

        //execute login / sign up items
        login();

        setVisible(false);
        //once verified
        db = new Database();
        db.connect();

        this.setTitle("Interweb Game Database");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exits on close of app
        this.setSize(800, 600);
        this.setLocationRelativeTo(null); // launches in middle
        this.setResizable(true);

        setVisible(true);

        //run normal db function


    }


    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN FLOW
    // Uses a CardLayout inside the frame so we can flip between
    // "Sign In" and "Create Account" without opening extra windows.
    // ─────────────────────────────────────────────────────────────────────────
    private CardLayout authCards;
    private JPanel     authContainer;

    private static final String CARD_LOGIN  = "login";
    private static final String CARD_SIGNUP = "signup";

    private void login() {
        authCards = new CardLayout();
        authContainer = new JPanel(authCards);

        loginPanel = buildLoginPanel();
        signupPanel = buildSignupPanel();

        authContainer.add(loginPanel, CARD_LOGIN);
        authContainer.add(signupPanel, CARD_SIGNUP);

        setContentPane(authContainer);
        revalidate();
        authCards.show(authContainer, CARD_LOGIN);


    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUILD SIGN-IN PANEL
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0x1a1a2e));

        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets  = new Insets(6, 30, 6, 30);

        // ── Title ─────────────────────────────────────────────────────────────
        c.gridx = 0; c.gridy = 0; c.insets = new Insets(30, 30, 4, 30);
        JLabel title = new JLabel("Interweb Game DB", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(0x58A6FF));
        panel.add(title, c);

        c.gridy = 1; c.insets = new Insets(0, 30, 24, 30);
        JLabel sub = new JLabel("Sign in to your account", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(0x8B949E));
        panel.add(sub, c);

        // ── Email ─────────────────────────────────────────────────────────────
        c.gridy = 2; c.insets = new Insets(4, 30, 2, 30);
        panel.add(fieldLabel("Email"), c);

        c.gridy = 3; c.insets = new Insets(0, 30, 8, 30);
        JTextField emailField = styledTextField();
        panel.add(emailField, c);

        // ── Password ──────────────────────────────────────────────────────────
        c.gridy = 4; c.insets = new Insets(4, 30, 2, 30);
        panel.add(fieldLabel("Password"), c);

        c.gridy = 5; c.insets = new Insets(0, 30, 20, 30);
        JPasswordField passField = styledPasswordField();
        panel.add(passField, c);

        // ── Error label (hidden until needed) ────────────────────────────────
        c.gridy = 6; c.insets = new Insets(0, 30, 8, 30);
        JLabel errorLbl = new JLabel(" ", SwingConstants.CENTER);
        errorLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLbl.setForeground(new Color(0xF85149));
        panel.add(errorLbl, c);

        // ── Sign In button ────────────────────────────────────────────────────
        c.gridy = 7; c.insets = new Insets(0, 30, 10, 30);
        JButton loginBtn = styledButton("Sign In", new Color(0x238636));
        panel.add(loginBtn, c);

        // ── Switch to sign-up ─────────────────────────────────────────────────
        c.gridy = 8; c.insets = new Insets(4, 30, 30, 30);
        JButton toSignup = linkButton("Don't have an account? Create one");
        panel.add(toSignup, c);

        // ── Actions ───────────────────────────────────────────────────────────
        ActionListener doLogin = e -> {
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword());

            if (email.isEmpty() || pass.isEmpty()) {
                errorLbl.setText("Please fill in both fields.");
                return;
            }

            // TODO: swap this stub for your real Database.authenticate() call
            String username = db.authenticate(email, pass);
            if (username != null) {
                completeLogin(username);
            } else {
                errorLbl.setText("Invalid email or password.");
                passField.setText("");
            }
        };

        loginBtn.addActionListener(doLogin);
        passField.addActionListener(doLogin);   // Enter key in password field

        toSignup.addActionListener(e -> {
            errorLbl.setText(" ");
            emailField.setText("");
            passField.setText("");
            authCards.show(authContainer, CARD_SIGNUP);
        });

        return panel;
    }

    private JDialog authBlocker; // dismissed when auth completes

    /** Call this when login succeeds to unblock the constructor. */
    private void completeLogin(String username) {
        loginSuccess = true;
        loggedInUser = username;
        if (authBlocker != null) authBlocker.dispose();
    }


    // ─────────────────────────────────────────────────────────────────────────
    // BUILD SIGN-UP PANEL
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildSignupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0x1a1a2e));

        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets  = new Insets(4, 30, 4, 30);

        // ── Title ─────────────────────────────────────────────────────────────
        c.gridx = 0; c.gridy = 0; c.insets = new Insets(24, 30, 2, 30);
        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x58A6FF));
        panel.add(title, c);

        // ── Fields ────────────────────────────────────────────────────────────
        c.insets = new Insets(4, 30, 2, 30);

        String[] labels = {"Username", "Email", "Password", "Confirm Password", "Birthdate (YYYY-MM-DD)"};
        JTextField[] fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            c.gridy = (i * 2) + 1; c.insets = new Insets(6, 30, 1, 30);
            panel.add(fieldLabel(labels[i]), c);

            c.gridy = (i * 2) + 2; c.insets = new Insets(0, 30, 4, 30);
            fields[i] = (i == 2 || i == 3) ? styledPasswordField() : styledTextField();
            panel.add(fields[i], c);
        }
        // ── Error label ───────────────────────────────────────────────────────
        c.gridy = labels.length * 2 + 1; c.insets = new Insets(2, 30, 2, 30);
        JLabel errorLbl = new JLabel(" ", SwingConstants.CENTER);
        errorLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLbl.setForeground(new Color(0xF85149));
        panel.add(errorLbl, c);

        // ── Create Account button ─────────────────────────────────────────────
        c.gridy = labels.length * 2 + 2; c.insets = new Insets(4, 30, 8, 30);
        JButton createBtn = styledButton("Create Account", new Color(0x1F6FEB));
        panel.add(createBtn, c);

        // ── Back to login ─────────────────────────────────────────────────────
        c.gridy = labels.length * 2 + 3; c.insets = new Insets(2, 30, 20, 30);
        JButton toLogin = linkButton("Already have an account? Sign in");
        panel.add(toLogin, c);

        // ── Actions ───────────────────────────────────────────────────────────
        createBtn.addActionListener(e -> {
            String username  = fields[0].getText().trim();
            String email     = fields[1].getText().trim();
            String pass      = new String(((JPasswordField) fields[2]).getPassword());
            String confirm   = new String(((JPasswordField) fields[3]).getPassword());
            String birthdate = fields[4].getText().trim();
            String dateRegex = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"; // yyyy-mm-dd

            // Validation
            if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                errorLbl.setText("Username, email, and password are required."); return;
            }
            if (!pass.equals(confirm)) {
                errorLbl.setText("Passwords do not match."); return;
            }
            if (pass.length() < 6) {
                errorLbl.setText("Password must be at least 6 characters."); return;
            }
            if (!email.contains("@")) {
                errorLbl.setText("Enter a valid email address."); return;
            }
            if (!birthdate.matches(dateRegex) || birthdate.compareTo(LocalDate.now().toString()) > 0) {
                errorLbl.setText("Enter a valid birthdate."); return;
            }

            // call db register
            boolean registered = db.register(username, email, pass, birthdate);
            if (registered) {
                JOptionPane.showMessageDialog(this,
                        "Account created! Please sign in.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                for (JTextField f : fields) f.setText("");
                errorLbl.setText(" ");
                authCards.show(authContainer, CARD_LOGIN);
            } else {
                errorLbl.setText("Username or email already in use.");
            }
        });

        toLogin.addActionListener(e -> {
            errorLbl.setText(" ");
            for (JTextField f : fields) {
                f.setText("");
            }
            authCards.show(authContainer, CARD_LOGIN);
        });

        return panel;
    }


    private void buildMainApp() {
        // TODO: build your libraryPanel, userPanel, gamePanel, reviewsPanel here
        JLabel placeholder = new JLabel("Welcome, " + loggedInUser + "!", SwingConstants.CENTER);
        placeholder.setFont(new Font("Segoe UI", Font.BOLD, 20));
        getContentPane().add(placeholder);


        //TODO
    }


    // ─────────────────────────────────────────────────────────────────────────
    // COMPONENT HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    /* ---- made by LLM to make look good ---- */
    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(0x8B949E));
        return lbl;
    }

    private JTextField styledTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(0, 36));
        tf.setBackground(new Color(0x21262D));
        tf.setForeground(new Color(0xC9D1D9));
        tf.setCaretColor(new Color(0xC9D1D9));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x30363D)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        return tf;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setPreferredSize(new Dimension(0, 36));
        pf.setBackground(new Color(0x21262D));
        pf.setForeground(new Color(0xC9D1D9));
        pf.setCaretColor(new Color(0xC9D1D9));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x30363D)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        return pf;
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()  ? bg.darker()   :
                        getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton linkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(new Color(0x58A6FF));
        btn.setBackground(new Color(0x1a1a2e));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }








}