import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class MainFrame extends JFrame {

    private Database db;

    // ── JPanels ──────────────────────────────────────────────────────────────
    private JPanel loginPanel;
    private JPanel signupPanel;
//    private JPanel libraryPanel;
//    private JPanel userPanel;
//    private JPanel gamePanel;
//    private JPanel reviewsPanel;

    // ── Shared login state ───────────────────────────────────────────────────
    private boolean loginSuccess = false;
    private User    loggedInUser = null;

    // ── Theme colours (reused throughout) ───────────────────────────────────
    private static final Color BG_DARK    = new Color(0x1a1a2e);
    private static final Color BG_MEDIUM  = new Color(0x161B22);   // dialog background
    private static final Color BG_SIDE    = new Color(0x0D1117);
    private static final Color BG_ITEM    = new Color(0x21262D);
    private static final Color BG_ITEM_HV = new Color(0x2D333B);
    private static final Color BG_NAV     = new Color(0x0D1117);
    private static final Color ACCENT     = new Color(0x58A6FF);
    private static final Color ACCENT_DIM = new Color(0x1F6FEB);
    private static final Color TEXT_PRI   = new Color(0xC9D1D9);
    private static final Color TEXT_SEC   = new Color(0x8B949E);
    private static final Color BORDER_COL = new Color(0x30363D);
    private static final Color BTN_GREEN  = new Color(0x2D9C42);
    private static final Color SEL_BG     = new Color(0x1C3A5E);

    // ── Layout constants ─────────────────────────────────────────────────────
    private static final int RESULTS_PER_PAGE = 20;
    private static final int REVIEWS_PER_PAGE = 5;
    private static final int LIBRARY_WIDTH    = 260;
    private static final int NAV_HEIGHT       = 50;

    // ── Auth card names ──────────────────────────────────────────────────────
    private CardLayout authCards;
    private JPanel     authContainer;
    private static final String CARD_LOGIN  = "login";
    private static final String CARD_SIGNUP = "signup";

    // ── Main-app right-panel card names ──────────────────────────────────────
    private static final String CARD_BLANK          = "blank";
    private static final String CARD_SEARCH_GAMES   = "searchGames";
    private static final String CARD_SEARCH_USERS   = "searchUsers";
    private static final String CARD_GAME_DETAIL    = "gameDetail";
    private static final String CARD_USER_DETAIL    = "userDetail";
    private static final String CARD_CREATE_REVIEW  = "createReview";

    // ── Main-app state ───────────────────────────────────────────────────────
    private CardLayout rightCards;
    private JPanel     rightPanel;

    // Game-search state
    private JTextField gameSearchField;
    private JPanel     gameResultsPanel;   // inner panel rebuilt on each search
    private JPanel     gamePagPanel;       // pagination row rebuilt on each search
    private int        currentGamePage  = 1;
    private String     currentGameQuery = "";

    // User-search state
    private JTextField userSearchField;
    private JPanel     userResultsPanel;
    private JPanel     userPagPanel;
    private int        currentUserPage  = 1;
    private String     currentUserQuery = "";

    // Library state
    private JPanel libraryListPanel;       // inner panel holding clickable rows

    // ── Game filter state ──────────────────────────────────────────────────────
    private String                filterDeveloper      = "";   // raw comma-sep string
    private List<String>          filterAgeRatings     = new ArrayList<>();
    private JButton               devFilterBtn;               // dev filter button ref
    private JButton               ratingFilterBtn;            // rating filter button ref
    private JTextField            filterDevField;             // text field inside dev popup
    private Map<String,JCheckBox> filterRatingBoxes    = new LinkedHashMap<>();
    private JPanel                devSuggestionPanel;         // inline suggestion list
    private javax.swing.Timer     devSuggestionTimer;         // 1.5 s idle timer
    private JPopupMenu            devFilterPopup;             // stored so timer can pack()
    private JPopupMenu            ratingFilterPopup;          // stored for clear wiring

    // Detail panel tracking (removed and rebuilt each time a new item is clicked)
    private JPanel currentGameDetailPanel   = null;
    private JPanel currentUserDetailPanel   = null;
    private JPanel currentCreateReviewPanel = null;


    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public MainFrame() {
        this.setTitle("Interweb Game Database: Sign In");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        db = new Database();
        db.connect();

        // test code for createReview
//        Review review = new Review("This game is good.", (byte) 8, "");
//        System.out.println(db.createReview(53, 195, review));

        login();
    }


    // =========================================================================
    // LOGIN FLOW
    // =========================================================================
    private void login() {
        authCards     = new CardLayout();
        authContainer = new JPanel(authCards);

        loginPanel  = buildLoginPanel();
        signupPanel = buildSignupPanel();

        authContainer.add(loginPanel,  CARD_LOGIN);
        authContainer.add(signupPanel, CARD_SIGNUP);

        setContentPane(authContainer);
        pack();
        setLocationRelativeTo(null);
        revalidate();
        authCards.show(authContainer, CARD_LOGIN);
        setVisible(true);
    }

    private void completeLogin(User user) {
        loginSuccess = true;
        loggedInUser = user;
        setVisible(false);
        buildMainApp();
    }


    // =========================================================================
    // BUILD SIGN-IN PANEL
    // =========================================================================
    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);

        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets  = new Insets(6, 30, 6, 30);

        c.gridx = 0; c.gridy = 0; c.insets = new Insets(30, 30, 4, 30);
        JLabel title = new JLabel("Interweb Game DB", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ACCENT);
        panel.add(title, c);

        c.gridy = 1; c.insets = new Insets(0, 30, 24, 30);
        JLabel sub = new JLabel("Sign in to your account", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_SEC);
        panel.add(sub, c);

        c.gridy = 2; c.insets = new Insets(4, 30, 2, 30);
        panel.add(fieldLabel("Email"), c);

        c.gridy = 3; c.insets = new Insets(0, 30, 8, 30);
        JTextField emailField = styledTextField();
        panel.add(emailField, c);

        c.gridy = 4; c.insets = new Insets(4, 30, 2, 30);
        panel.add(fieldLabel("Password"), c);

        c.gridy = 5; c.insets = new Insets(0, 30, 20, 30);
        JPasswordField passField = styledPasswordField();
        panel.add(passField, c);

        c.gridy = 6; c.insets = new Insets(0, 30, 8, 30);
        JLabel errorLbl = new JLabel(" ", SwingConstants.CENTER);
        errorLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLbl.setForeground(new Color(0xF85149));
        panel.add(errorLbl, c);

        c.gridy = 7; c.insets = new Insets(0, 30, 10, 30);
        JButton loginBtn = styledButton("Sign In", BTN_GREEN);
        panel.add(loginBtn, c);

        c.gridy = 8; c.insets = new Insets(4, 30, 30, 30);
        JButton toSignup = linkButton("Don't have an account? Create one");
        panel.add(toSignup, c);

        ActionListener doLogin = e -> {
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword());
            if (email.isEmpty() || pass.isEmpty()) {
                errorLbl.setText("Please fill in both fields.");
                return;
            }
            User user = db.authenticate(email, pass);
            if (user != null) {
                completeLogin(user);
            } else {
                errorLbl.setText("Invalid email or password.");
                passField.setText("");
            }
        };

        loginBtn.addActionListener(doLogin);
        passField.addActionListener(doLogin);

        toSignup.addActionListener(e -> {
            errorLbl.setText(" ");
            emailField.setText("");
            passField.setText("");
            authCards.show(authContainer, CARD_SIGNUP);
        });

        return panel;
    }


    // =========================================================================
    // BUILD SIGN-UP PANEL
    // =========================================================================
    private JPanel buildSignupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);

        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets  = new Insets(4, 30, 4, 30);

        c.gridx = 0; c.gridy = 0; c.insets = new Insets(24, 30, 2, 30);
        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ACCENT);
        panel.add(title, c);

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

        c.gridy = labels.length * 2 + 1; c.insets = new Insets(2, 30, 2, 30);
        JLabel errorLbl = new JLabel(" ", SwingConstants.CENTER);
        errorLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLbl.setForeground(new Color(0xF85149));
        panel.add(errorLbl, c);

        c.gridy = labels.length * 2 + 2; c.insets = new Insets(4, 30, 8, 30);
        JButton createBtn = styledButton("Create Account", ACCENT_DIM);
        panel.add(createBtn, c);

        c.gridy = labels.length * 2 + 3; c.insets = new Insets(2, 30, 20, 30);
        JButton toLogin = linkButton("Already have an account? Sign in");
        panel.add(toLogin, c);

        createBtn.addActionListener(e -> {
            String username  = fields[0].getText().trim();
            String email     = fields[1].getText().trim();
            String pass      = new String(((JPasswordField) fields[2]).getPassword());
            String confirm   = new String(((JPasswordField) fields[3]).getPassword());
            String birthdate = fields[4].getText().trim();
            // must be born after 999 CE
            String dateRegex = "^([12])\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";

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
            for (JTextField f : fields) f.setText("");
            authCards.show(authContainer, CARD_LOGIN);
        });

        return panel;
    }


    // =========================================================================
    // BUILD MAIN APP  ← replaces the old stub
    // =========================================================================
    private void buildMainApp() {
        getContentPane().removeAll();

        this.setTitle("Interweb Game Database");
        this.setSize(1200, 750);
        this.setMinimumSize(new Dimension(900, 600));
        this.setLocationRelativeTo(null);
        this.setResizable(true);

        // ── Root panel: BorderLayout ─────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        // ── Top navigation bar ───────────────────────────────────────────────
        root.add(buildNavBar(), BorderLayout.NORTH);

        // ── Centre split: library (left 1/3) + content (right 2/3) ──────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLibraryPanel(),
                buildRightPanel());
        split.setDividerLocation(LIBRARY_WIDTH);
        split.setDividerSize(3);
        split.setEnabled(false);               // prevent user from dragging divider
        split.setBorder(null);
        split.setBackground(BORDER_COL);

        root.add(split, BorderLayout.CENTER);

        setContentPane(root);
        setVisible(true);

        // Populate library after frame is visible
        refreshLibrary();
    }


    // =========================================================================
    // NAV BAR
    // =========================================================================
    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(BG_NAV);
        nav.setPreferredSize(new Dimension(0, NAV_HEIGHT));
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COL));

        // Left: app title
        JLabel appTitle = new JLabel("  IGDB");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        appTitle.setForeground(ACCENT);
        nav.add(appTitle, BorderLayout.WEST);

        // Right: navigation buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setOpaque(false);

        JButton usersBtn   = navButton("Users");
        JButton gamesBtn   = navButton("Games");
        JButton profileBtn = navButton("Profile");
        JButton createBtn  = navButton("Add Game");
        createBtn.setForeground(BTN_GREEN);   // tinted green to stand out

        usersBtn.addActionListener(e -> showSearchPanel(false));
        gamesBtn.addActionListener(e -> showSearchPanel(true));
        profileBtn.addActionListener(e -> showProfilePanel());
        createBtn.addActionListener(e -> showCreateGameDialog());

        btnPanel.add(createBtn);
        btnPanel.add(usersBtn);
        btnPanel.add(gamesBtn);
        btnPanel.add(profileBtn);
        nav.add(btnPanel, BorderLayout.EAST);

        return nav;
    }

    /** Thin nav-bar button style. */
    private JButton navButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                if (getModel().isRollover() || getModel().isPressed()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isPressed()
                            ? new Color(0x30363D) : new Color(0x21262D));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(TEXT_PRI);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 34));
        return btn;
    }


    // =========================================================================
    // =========================================================================
    // CREATE GAME DIALOG
    // =========================================================================
    /**
     * Modal dialog for creating a new game.
     * Builds a Game object from the entered fields, calls db.createGame(),
     * then searches for the newly inserted row to get its GameID so we can
     * navigate directly to its detail panel (where the user will be owner).
     */
    private void showCreateGameDialog() {
        JDialog dlg = new JDialog(this, "Create New Game", true);
        dlg.setSize(480, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);
        dlg.getContentPane().setBackground(BG_MEDIUM);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_MEDIUM);

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x161B22));
        header.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel titleLbl = new JLabel("Create New Game");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(ACCENT);
        header.add(titleLbl, BorderLayout.WEST);
        outer.add(header, BorderLayout.NORTH);

        // ── Form ──────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_MEDIUM);
        form.setBorder(new EmptyBorder(18, 20, 12, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx   = 0;

        // Fields
        JTextField  titleField   = createDialogTextField();
        JTextField  devField     = createDialogTextField();
        JTextArea   descArea     = new JTextArea(4, 0);
        descArea.setBackground(BG_ITEM); descArea.setForeground(TEXT_PRI);
        descArea.setCaretColor(TEXT_PRI); descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JTextField  releaseDateField = createDialogTextField();
        releaseDateField.setToolTipText("YYYY-MM-DD  (leave blank to omit)");

        String[] ratingOptions = {"", "E", "E10+", "T", "M", "AO", "RP"};
        JComboBox<String> ratingBox = new JComboBox<>(ratingOptions);
        ratingBox.setBackground(BG_ITEM); ratingBox.setForeground(TEXT_PRI);
        ratingBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTextField salesField = createDialogTextField();
        salesField.setText("0");

        JLabel errorLbl = new JLabel(" ");
        errorLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLbl.setForeground(new Color(0xF85149));
        errorLbl.setHorizontalAlignment(SwingConstants.CENTER);

        // Add rows
        addDialogRow(form, c, 0, "Title *",                  titleField);
        addDialogRow(form, c, 1, "Developer",                devField);
        addDialogRow(form, c, 2, "Description",              new JScrollPane(descArea));
        addDialogRow(form, c, 3, "Release Date (YYYY-MM-DD)",releaseDateField);
        addDialogRow(form, c, 4, "Age Rating",               ratingBox);
        addDialogRow(form, c, 5, "Sales",                    salesField);

        c.gridy = 12; c.insets = new Insets(8, 0, 0, 0);
        form.add(errorLbl, c);

        JScrollPane formScroll = new JScrollPane(form,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(BG_MEDIUM);
        outer.add(formScroll, BorderLayout.CENTER);

        // ── Footer: Cancel + Submit ───────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(new Color(0x161B22));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COL));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.setForeground(TEXT_SEC);
        cancelBtn.setBackground(BG_ITEM);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dlg.dispose());

        JButton submitBtn = styledButton("Create Game", BTN_GREEN);
        submitBtn.setPreferredSize(new Dimension(130, 36));

        submitBtn.addActionListener(e -> {
            // ── Validate ──────────────────────────────────────────────────────
            String title  = titleField.getText().trim();
            String dev    = devField.getText().trim();
            String desc   = descArea.getText().trim();
            String rdStr  = releaseDateField.getText().trim();
            String rating = (String) ratingBox.getSelectedItem();
            String salesStr = salesField.getText().trim();

            if (title.isEmpty()) {
                errorLbl.setText("Title is required.");
                return;
            }

            java.sql.Date releaseDate = null;
            if (!rdStr.isEmpty()) {
                try {
                    releaseDate = java.sql.Date.valueOf(java.time.LocalDate.parse(rdStr));
                } catch (Exception ex) {
                    errorLbl.setText("Release date must be YYYY-MM-DD.");
                    return;
                }
            }

            int sales = 0;
            if (!salesStr.isEmpty()) {
                try {
                    sales = Integer.parseInt(salesStr);
                    if (sales < 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    errorLbl.setText("Sales must be a non-negative whole number.");
                    return;
                }
            }

            // ── Create ────────────────────────────────────────────────────────
            Game newGame = new Game(
                    title,
                    dev.isEmpty()    ? null : dev,
                    desc.isEmpty()   ? null : desc,
                    releaseDate,
                    (rating == null || rating.isEmpty()) ? null : rating,
                    sales
            );

            boolean ok = db.createGame(loggedInUser, newGame);
            if (!ok) {
                errorLbl.setText("Failed to create game. Please try again.");
                return;
            }

            dlg.dispose();

            // ── Navigate to the new game's detail panel ───────────────────────
            // createGame() doesn't return the ID, so search by title to get the
            // full Game object (with ID) so showGameDetailPanel can be called.
            LinkedList<Game> found = db.searchGames(title, 1);
            Game created = null;
            if (found != null) {
                for (Game g : found) {
                    if (title.equalsIgnoreCase(g.getTitle())) {
                        created = g;
                        break;
                    }
                }
            }
            if (created != null) {
                showGameDetailPanel(created);
            } else {
                // Fallback: open browse view so the user can find their game
                showSearchPanel(true);
            }
        });

        footer.add(cancelBtn);
        footer.add(submitBtn);
        outer.add(footer, BorderLayout.SOUTH);

        dlg.setContentPane(outer);
        dlg.setVisible(true);
    }

    /** Styled text field for the create-game dialog. */
    private JTextField createDialogTextField() {
        JTextField tf = new JTextField();
        tf.setBackground(BG_ITEM);
        tf.setForeground(TEXT_PRI);
        tf.setCaretColor(TEXT_PRI);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return tf;
    }

    /**
     * Adds a label + control pair as two consecutive GridBagConstraints rows
     * into the create-game dialog form panel.
     */
    private void addDialogRow(JPanel panel, GridBagConstraints c,
                              int rowIndex, String label, JComponent field) {
        c.gridy  = rowIndex * 2;
        c.insets = new Insets(rowIndex == 0 ? 0 : 10, 0, 2, 0);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_SEC);
        panel.add(lbl, c);

        c.gridy  = rowIndex * 2 + 1;
        c.insets = new Insets(0, 0, 0, 0);
        if (field instanceof JScrollPane) {
            c.weighty = 1;
            c.fill    = GridBagConstraints.BOTH;
        } else {
            c.weighty = 0;
            c.fill    = GridBagConstraints.HORIZONTAL;
        }
        panel.add(field, c);
        // Reset after any BOTH rows
        c.weighty = 0;
        c.fill    = GridBagConstraints.HORIZONTAL;
    }


    // =========================================================================
    // LIBRARY PANEL  (left 1/3)
    // =========================================================================
    // =========================================================================
    private JPanel buildLibraryPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_SIDE);
        outer.setPreferredSize(new Dimension(LIBRARY_WIDTH, 0));
        outer.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COL));

        // Header
        JLabel header = new JLabel("  MY LIBRARY");
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setForeground(TEXT_SEC);
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COL));
        outer.add(header, BorderLayout.NORTH);

        // Scrollable game list
        libraryListPanel = new JPanel();
        libraryListPanel.setLayout(new BoxLayout(libraryListPanel, BoxLayout.Y_AXIS));
        libraryListPanel.setBackground(BG_SIDE);

        JScrollPane scroll = new JScrollPane(libraryListPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scroll.getVerticalScrollBar());

        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    /**
     * Fetches the logged-in user's game library and populates the library panel.
     * Uses Database.getUserLibrary(int userID).
     */
    private void refreshLibrary() {
        libraryListPanel.removeAll();

        LinkedList<Game> library = db.getUserLibrary(loggedInUser.getUserID());

        if (library == null || library.isEmpty()) {
            JLabel empty = new JLabel("  No games in library.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            empty.setForeground(TEXT_SEC);
            empty.setBorder(new EmptyBorder(12, 8, 0, 8));
            libraryListPanel.add(empty);
        } else {
            for (Game game : library) {
                libraryListPanel.add(buildLibraryRow(game));
            }
        }

        libraryListPanel.add(Box.createVerticalGlue());
        libraryListPanel.revalidate();
        libraryListPanel.repaint();
    }

    /** One clickable row in the library sidebar. */
    private JPanel buildLibraryRow(Game game) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG_SIDE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x21262D)),
                new EmptyBorder(6, 12, 6, 12)));

        JLabel title = new JLabel(game.getTitle());
        title.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        title.setForeground(TEXT_PRI);
        row.add(title, BorderLayout.CENTER);

        // Hover highlighting
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                row.setBackground(BG_ITEM_HV);
                title.setForeground(ACCENT);
            }
            @Override public void mouseExited(MouseEvent e) {
                row.setBackground(BG_SIDE);
                title.setForeground(TEXT_PRI);
            }
            @Override public void mouseClicked(MouseEvent e) {
                showGameDetailPanel(game);
            }
        });
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return row;
    }


    // =========================================================================
    // RIGHT PANEL  (2/3) — CardLayout host
    // =========================================================================
    private JPanel buildRightPanel() {
        rightCards = new CardLayout();
        rightPanel = new JPanel(rightCards);
        rightPanel.setBackground(BG_DARK);

        // Blank card shown on startup
        JPanel blank = new JPanel(new GridBagLayout());
        blank.setBackground(BG_DARK);
        JLabel hint = new JLabel("Use the buttons above to search Games or Users");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hint.setForeground(TEXT_SEC);
        blank.add(hint);

        rightPanel.add(blank,                      CARD_BLANK);
        rightPanel.add(buildSearchPanel(true),     CARD_SEARCH_GAMES);
        rightPanel.add(buildSearchPanel(false),    CARD_SEARCH_USERS);

        // Placeholder card for create-review (rebuilt per game when needed)
        rightPanel.add(new JPanel(), CARD_CREATE_REVIEW);

        rightCards.show(rightPanel, CARD_BLANK);
        return rightPanel;
    }


    // =========================================================================
    // SEARCH PANEL  (shared builder for games and users)
    // =========================================================================

    /**
     * Builds one search panel.
     * @param forGames true → game search panel, false → user search panel
     */
    private JPanel buildSearchPanel(boolean forGames) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_DARK);
        outer.setBorder(new EmptyBorder(16, 20, 16, 20));

        // ── Search bar: [  text field  ] [Filter] [Search] ───────────────────
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JTextField searchField = styledTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.putClientProperty("placeholderText",
                forGames ? "Search games…" : "Search users…");

        // ── Filter popups (games only) ────────────────────────────────────────
        if (forGames) {
            devFilterPopup    = buildDevFilterPopup();
            ratingFilterPopup = buildRatingFilterPopup();
        }

        // ── Dev filter button (games only) ────────────────────────────────────
        JButton devBtn = forGames ? buildFilterButton("Developer") : null;
        if (forGames) {
            devFilterBtn = devBtn;
            devBtn.addActionListener(e -> {
                if (ratingFilterPopup.isVisible()) ratingFilterPopup.setVisible(false);
                if (devFilterPopup.isVisible()) { devFilterPopup.setVisible(false); }
                else { devFilterPopup.show(devBtn, 0, devBtn.getHeight() + 2); }
            });
        }

        // ── Rating filter button (games only) ─────────────────────────────────
        JButton ratingBtn = forGames ? buildFilterButton("Age Rating") : null;
        if (forGames) {
            ratingFilterBtn = ratingBtn;
            ratingBtn.addActionListener(e -> {
                if (devFilterPopup.isVisible()) devFilterPopup.setVisible(false);
                if (ratingFilterPopup.isVisible()) { ratingFilterPopup.setVisible(false); }
                else { ratingFilterPopup.show(ratingBtn, 0, ratingBtn.getHeight() + 2); }
            });
        }

        // Group all right-side buttons: [Dev] [Rating] [Search]
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightBtns.setOpaque(false);
        if (forGames) { rightBtns.add(devBtn); rightBtns.add(ratingBtn); }

        JButton searchBtn = styledButton("Search", ACCENT_DIM);
        searchBtn.setPreferredSize(new Dimension(90, 36));
        rightBtns.add(searchBtn);

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(rightBtns,   BorderLayout.EAST);
        outer.add(searchBar, BorderLayout.NORTH);

        // ── Results list (inner panel inside a scroll pane) ──────────────────
        JPanel resultsInner = new JPanel();
        resultsInner.setLayout(new BoxLayout(resultsInner, BoxLayout.Y_AXIS));
        resultsInner.setBackground(BG_DARK);

        JScrollPane resultsScroll = new JScrollPane(resultsInner,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultsScroll.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        resultsScroll.getVerticalScrollBar().setUnitIncrement(20);
        styleScrollBar(resultsScroll.getVerticalScrollBar());

        outer.add(resultsScroll, BorderLayout.CENTER);

        // ── Pagination bar ───────────────────────────────────────────────────
        JPanel pagPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 8));
        pagPanel.setOpaque(false);
        outer.add(pagPanel, BorderLayout.SOUTH);

        // Store references for later refresh
        if (forGames) {
            gameSearchField  = searchField;
            gameResultsPanel = resultsInner;
            gamePagPanel     = pagPanel;
        } else {
            userSearchField  = searchField;
            userResultsPanel = resultsInner;
            userPagPanel     = pagPanel;
        }

        // ── Wire up search ───────────────────────────────────────────────────
        ActionListener doSearch = e -> {
            if (forGames) {
                currentGameQuery = searchField.getText().trim();
                currentGamePage  = 1;
                refreshGameResults();
            } else {
                currentUserQuery = searchField.getText().trim();
                currentUserPage  = 1;
                refreshUserResults();
            }
        };
        searchBtn.addActionListener(doSearch);
        searchField.addActionListener(doSearch);   // Enter key

        return outer;
    }

    // =========================================================================
    // FILTER BUTTON FACTORY
    // =========================================================================
    /** Shared styled button used for both Dev and Rating filter buttons. */
    private JButton buildFilterButton(String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = new Color(0x30363D);
                g2.setColor(getModel().isPressed()  ? base.darker()   :
                            getModel().isRollover() ? base.brighter() : base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(TEXT_PRI);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 8, 0, 8));
        btn.setPreferredSize(new Dimension(100, 36));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }


    // =========================================================================
    // DEVELOPER FILTER POPUP
    // =========================================================================
    /**
     * Developer filter popup.
     *
     * The text field accepts a comma-separated list of developer names, e.g.
     * "Sony,Nintendo".  After 1.5 s of no typing the last token (after the
     * final comma) is passed to db.getDeveloperSuggestions(lastToken).
     * Clicking a suggestion replaces that token and appends a comma so the
     * user can continue typing more names.
     *
     * Apply passes the full raw string to db.searchGames as the developer arg.
     */
    private JPopupMenu buildDevFilterPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        popup.setBackground(BG_ITEM);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_ITEM);
        wrapper.setPreferredSize(new Dimension(300, 260));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x161B22));
        header.setBorder(new EmptyBorder(8, 12, 8, 8));
        JLabel titleLbl = new JLabel("DEVELOPER FILTER");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TEXT_SEC);
        header.add(titleLbl, BorderLayout.WEST);
        JButton clearBtn = makeInlineClearButton();
        header.add(clearBtn, BorderLayout.EAST);
        wrapper.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG_ITEM);
        body.setBorder(new EmptyBorder(10, 12, 6, 12));

        JLabel devLbl = new JLabel("Developers (comma-separated)");
        devLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        devLbl.setForeground(TEXT_SEC);
        devLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(devLbl);
        body.add(Box.createVerticalStrut(4));

        filterDevField = new JTextField(filterDeveloper);
        filterDevField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filterDevField.setBackground(BG_SIDE);
        filterDevField.setForeground(TEXT_PRI);
        filterDevField.setCaretColor(TEXT_PRI);
        filterDevField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        filterDevField.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterDevField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        body.add(filterDevField);
        body.add(Box.createVerticalStrut(6));

        // Suggestion panel — always present, populated after idle timeout
        devSuggestionPanel = new JPanel();
        devSuggestionPanel.setLayout(new BoxLayout(devSuggestionPanel, BoxLayout.Y_AXIS));
        devSuggestionPanel.setBackground(BG_SIDE);

        JScrollPane suggestScroll = new JScrollPane(devSuggestionPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        suggestScroll.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        suggestScroll.getViewport().setBackground(BG_SIDE);
        suggestScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        suggestScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        suggestScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 120));
        styleScrollBar(suggestScroll.getVerticalScrollBar());
        body.add(suggestScroll);

        wrapper.add(body, BorderLayout.CENTER);

        // Apply row
        JPanel applyRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        applyRow.setBackground(new Color(0x161B22));
        applyRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COL));
        JButton applyBtn = styledButton("Apply Developer", ACCENT_DIM);
        applyBtn.setPreferredSize(new Dimension(145, 32));
        applyRow.add(applyBtn);
        wrapper.add(applyRow, BorderLayout.SOUTH);

        // Idle timer: fires 1.5 s after last keystroke, queries DB for suggestions
        devSuggestionTimer = new javax.swing.Timer(1500, e -> {
            devSuggestionTimer.stop();
            String typed     = filterDevField.getText();
            String[] parts   = typed.split(",", -1);
            String lastToken = parts[parts.length - 1].trim();
            devSuggestionPanel.removeAll();
            if (!lastToken.isEmpty()) {
                LinkedList<String> suggestions = db.searchDevelopers(lastToken);
                if (suggestions != null) {
                    for (String s : suggestions) {
                        devSuggestionPanel.add(buildSuggestionRow(s));
                    }
                }
            }
            devSuggestionPanel.revalidate();
            devSuggestionPanel.repaint();
        });
        devSuggestionTimer.setRepeats(false);

        // On every keystroke: clear stale suggestions and restart timer
        filterDevField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { onDevFieldTyped(); }
            @Override public void removeUpdate(DocumentEvent e)  { onDevFieldTyped(); }
            @Override public void changedUpdate(DocumentEvent e) { onDevFieldTyped(); }
        });

        // Apply: commit filterDeveloper, clear age rating filter (mutually exclusive)
        applyBtn.addActionListener(e -> {
            filterDeveloper  = filterDevField.getText().trim();
            filterAgeRatings = new ArrayList<>();           // clear the other filter
            filterRatingBoxes.values().forEach(cb -> cb.setSelected(false));
            currentGamePage  = 1;
            popup.setVisible(false);
            refreshGameResults();
            updateFilterBtnAppearance();
        });

        // Clear: wipe field, state, suggestions, refresh
        clearBtn.addActionListener(e -> {
            filterDevField.setText("");
            devSuggestionPanel.removeAll();
            devSuggestionPanel.revalidate();
            devSuggestionPanel.repaint();
            filterDeveloper = "";
            currentGamePage = 1;
            popup.setVisible(false);
            refreshGameResults();
            updateFilterBtnAppearance();
        });

        popup.setLayout(new BorderLayout());
        popup.add(wrapper, BorderLayout.CENTER);
        return popup;
    }

    /** Resets stale suggestions and restarts the idle timer on each keystroke. */
    private void onDevFieldTyped() {
        if (devSuggestionTimer  != null) devSuggestionTimer.restart();
        if (devSuggestionPanel  != null) {
            devSuggestionPanel.removeAll();
            devSuggestionPanel.revalidate();
            devSuggestionPanel.repaint();
        }
    }

    /**
     * One clickable row in the developer suggestion list.
     * Clicking replaces the last comma-token in filterDevField with this
     * suggestion and appends a comma ready for the next developer.
     */
    private JPanel buildSuggestionRow(String suggestion) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG_SIDE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COL),
                new EmptyBorder(4, 10, 4, 10)));

        JLabel lbl = new JLabel(suggestion);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_PRI);
        row.add(lbl, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { row.setBackground(BG_ITEM_HV); }
            @Override public void mouseExited (MouseEvent e) { row.setBackground(BG_SIDE); }
            @Override public void mouseClicked(MouseEvent e) {
                String current   = filterDevField.getText();
                String[] parts   = current.split(",", -1);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    sb.append(parts[i].trim()).append(",");
                }
                sb.append(suggestion).append(",");
                filterDevField.setText(sb.toString());
                filterDevField.setCaretPosition(filterDevField.getText().length());
                devSuggestionPanel.removeAll();
                devSuggestionPanel.revalidate();
                devSuggestionPanel.repaint();
            }
        });
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return row;
    }


    // =========================================================================
    // AGE RATING FILTER POPUP
    // =========================================================================
    /** Builds the age-rating popup with one checkbox per ESRB rating. */
    private JPopupMenu buildRatingFilterPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        popup.setBackground(BG_ITEM);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_ITEM);
        wrapper.setPreferredSize(new Dimension(260, 165));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x161B22));
        header.setBorder(new EmptyBorder(8, 12, 8, 8));
        JLabel titleLbl = new JLabel("AGE RATING FILTER");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TEXT_SEC);
        header.add(titleLbl, BorderLayout.WEST);
        JButton clearBtn = makeInlineClearButton();
        header.add(clearBtn, BorderLayout.EAST);
        wrapper.add(header, BorderLayout.NORTH);

        // Checkbox grid
        String[] ratings = {"E", "E10+", "T", "M", "AO", "RP"};
        filterRatingBoxes.clear();

        JPanel checkGrid = new JPanel(new GridLayout(2, 3, 10, 6));
        checkGrid.setBackground(BG_ITEM);
        checkGrid.setBorder(new EmptyBorder(10, 14, 10, 14));

        for (String r : ratings) {
            JCheckBox cb = new JCheckBox(r);
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cb.setForeground(TEXT_PRI);
            cb.setBackground(BG_ITEM);
            cb.setFocusPainted(false);
            cb.setSelected(filterAgeRatings.contains(r));
            filterRatingBoxes.put(r, cb);
            checkGrid.add(cb);
        }
        wrapper.add(checkGrid, BorderLayout.CENTER);

        // Apply row
        JPanel applyRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        applyRow.setBackground(new Color(0x161B22));
        applyRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COL));
        JButton applyBtn = styledButton("Apply Ratings", ACCENT_DIM);
        applyBtn.setPreferredSize(new Dimension(125, 32));
        applyRow.add(applyBtn);
        wrapper.add(applyRow, BorderLayout.SOUTH);

        applyBtn.addActionListener(e -> {
            filterAgeRatings = filterRatingBoxes.entrySet().stream()
                    .filter(en -> en.getValue().isSelected())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            filterDeveloper = "";                           // clear the other filter
            filterDevField.setText("");
            currentGamePage = 1;
            popup.setVisible(false);
            refreshGameResults();
            updateFilterBtnAppearance();
        });

        clearBtn.addActionListener(e -> {
            filterRatingBoxes.values().forEach(cb -> cb.setSelected(false));
            filterAgeRatings = new ArrayList<>();
            currentGamePage  = 1;
            popup.setVisible(false);
            refreshGameResults();
            updateFilterBtnAppearance();
        });

        popup.setLayout(new BorderLayout());
        popup.add(wrapper, BorderLayout.CENTER);
        return popup;
    }


    // =========================================================================
    // FILTER SHARED HELPERS
    // =========================================================================
    /** Small "Clear" link-button used in both filter popup headers. */
    private JButton makeInlineClearButton() {
        JButton btn = new JButton("Clear");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(ACCENT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Updates both filter button labels/colours to reflect active state.
     *  When one filter is active the other button is dimmed to signal
     *  mutual exclusivity — applying one always clears the other. */
    private void updateFilterBtnAppearance() {
        boolean devActive    = !filterDeveloper.isEmpty();
        boolean ratingActive = !filterAgeRatings.isEmpty();
        boolean eitherActive = devActive || ratingActive;

        if (devFilterBtn != null) {
            devFilterBtn.setText(devActive ? "Developer ●" : "Developer");
            devFilterBtn.setForeground(
                    devActive    ? ACCENT    :
                    eitherActive ? TEXT_SEC  : TEXT_PRI);  // dim when rating is active
            devFilterBtn.repaint();
        }
        if (ratingFilterBtn != null) {
            ratingFilterBtn.setText(ratingActive ? "Age Rating ●" : "Age Rating");
            ratingFilterBtn.setForeground(
                    ratingActive ? ACCENT    :
                    eitherActive ? TEXT_SEC  : TEXT_PRI);  // dim when dev is active
            ratingFilterBtn.repaint();
        }
    }
    /**
     * Adds one labelled filter row to the options panel inside the filter popup.
     * Kept as a utility for future user-filter implementation.
     */
    private void addFilterItem(JPanel panel, String label, JComponent control) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBackground(BG_ITEM);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x21262D)),
                new EmptyBorder(6, 12, 6, 12)));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_SEC);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(lbl);
        row.add(Box.createVerticalStrut(3));

        control.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        control.setAlignmentX(Component.LEFT_ALIGNMENT);
        control.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        if (control instanceof JTextField tf) {
            tf.setBackground(BG_SIDE);
            tf.setForeground(TEXT_PRI);
            tf.setCaretColor(TEXT_PRI);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COL),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        } else if (control instanceof JComboBox<?> cb) {
            cb.setBackground(BG_SIDE);
            cb.setForeground(TEXT_PRI);
            cb.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        } else if (control instanceof JCheckBox chk) {
            chk.setBackground(BG_ITEM);
            chk.setForeground(TEXT_PRI);
        }

        row.add(control);
        panel.add(row);
    }


    // =========================================================================
    // SHOW / SWITCH RIGHT PANEL
    // =========================================================================
    private void showSearchPanel(boolean forGames) {
        if (forGames) {
            rightCards.show(rightPanel, CARD_SEARCH_GAMES);
            // Trigger an initial blank-query search to pre-populate the list
            if (gameResultsPanel.getComponentCount() == 0) {
                currentGameQuery = "";
                currentGamePage  = 1;
                refreshGameResults();
            }
        } else {
            rightCards.show(rightPanel, CARD_SEARCH_USERS);
            if (userResultsPanel.getComponentCount() == 0) {
                currentUserQuery = "";
                currentUserPage  = 1;
                refreshUserResults();
            }
        }
    }

    /** Called when the Profile nav button is pressed — shows the logged-in user's page. */
    private void showProfilePanel() {
        showUserDetailPanel(loggedInUser);
    }

    /** Called when a game row is clicked (library or search result). */
    private void showGameDetailPanel(Game game) {
        if (currentGameDetailPanel != null) {
            rightPanel.remove(currentGameDetailPanel);
        }
        currentGameDetailPanel = buildGameDetailPanel(game);
        rightPanel.add(currentGameDetailPanel, CARD_GAME_DETAIL);
        rightCards.show(rightPanel, CARD_GAME_DETAIL);
    }

    /** Called when a user row is clicked in search results, or Profile is pressed. */
    private void showUserDetailPanel(User user) {
        if (currentUserDetailPanel != null) {
            rightPanel.remove(currentUserDetailPanel);
        }
        currentUserDetailPanel = buildUserDetailPanel(user);
        rightPanel.add(currentUserDetailPanel, CARD_USER_DETAIL);
        rightCards.show(rightPanel, CARD_USER_DETAIL);
    }

    /** Opens the create-review form for a specific game. */
    private void showCreateReviewPanel(Game game) {
        if (currentCreateReviewPanel != null) {
            rightPanel.remove(currentCreateReviewPanel);
        }
        currentCreateReviewPanel = buildCreateReviewPanel(game);
        rightPanel.add(currentCreateReviewPanel, CARD_CREATE_REVIEW);
        rightCards.show(rightPanel, CARD_CREATE_REVIEW);
    }


    // =========================================================================
    // REFRESH RESULTS
    // =========================================================================
    private void refreshGameResults() {
        gameResultsPanel.removeAll();

        LinkedList<Game> results;
        int totalPages;

        if (!filterDeveloper.isEmpty()) {
            // Developer filter active — use dev-only overload
            results    = db.searchGames(currentGameQuery, currentGamePage, filterDeveloper);
            totalPages = db.getGameSearchPageCount(currentGameQuery, filterDeveloper);
        } else if (!filterAgeRatings.isEmpty()) {
            // Age rating filter active — use rating-only overload
            results    = db.searchGames(currentGameQuery, currentGamePage, filterAgeRatings);
            totalPages = db.getGameSearchPageCount(currentGameQuery, filterAgeRatings);
        } else {
            // No filter
            results    = db.searchGames(currentGameQuery, currentGamePage);
            totalPages = db.getGameSearchPageCount(currentGameQuery);
        }

        if (results == null || results.isEmpty()) {
            gameResultsPanel.add(emptyResultLabel("No games found."));
        } else {
            for (Game g : results) {
                gameResultsPanel.add(buildGameResultRow(g));
            }
        }

        gameResultsPanel.add(Box.createVerticalGlue());
        gameResultsPanel.revalidate();
        gameResultsPanel.repaint();

        rebuildPagination(gamePagPanel, currentGamePage, totalPages,
                page -> { currentGamePage = page; refreshGameResults(); });
    }

    private void refreshUserResults() {
        userResultsPanel.removeAll();

        LinkedList<User> results = db.searchUsers(currentUserQuery, currentUserPage);
        int totalPages           = db.getUserSearchPageCount(currentUserQuery);

        if (results == null || results.isEmpty()) {
            userResultsPanel.add(emptyResultLabel("No users found."));
        } else {
            for (User u : results) {
                userResultsPanel.add(buildUserResultRow(u));
            }
        }

        userResultsPanel.add(Box.createVerticalGlue());
        userResultsPanel.revalidate();
        userResultsPanel.repaint();

        rebuildPagination(userPagPanel, currentUserPage, totalPages,
                page -> { currentUserPage = page; refreshUserResults(); });
    }


    // =========================================================================
    // RESULT ROW BUILDERS
    // =========================================================================
    private JPanel buildGameResultRow(Game game) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(BG_ITEM);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COL),
                new EmptyBorder(10, 14, 10, 14)));

        // Left: title + developer
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JLabel titleLbl = new JLabel(game.getTitle());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(TEXT_PRI);

        // Truncate description for the subtitle line
        String descRaw = game.getGameDescription();
        String desc    = (descRaw != null && descRaw.length() > 80)
                ? descRaw.substring(0, 80) + "…" : (descRaw == null ? "" : descRaw);
        JLabel descLbl = new JLabel(game.getDeveloper() + "  ·  " + desc);
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLbl.setForeground(TEXT_SEC);

        info.add(titleLbl);
        info.add(descLbl);
        row.add(info, BorderLayout.CENTER);

        // Right: age rating
        if (game.getAgeRating() != null) {
            JLabel rating = new JLabel(game.getAgeRating());
            rating.setFont(new Font("Segoe UI", Font.BOLD, 11));
            rating.setForeground(TEXT_SEC);
            rating.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COL),
                    new EmptyBorder(2, 6, 2, 6)));
            JPanel ratingWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            ratingWrap.setOpaque(false);
            ratingWrap.add(rating);
            row.add(ratingWrap, BorderLayout.EAST);
        }

        addHoverAndClick(row, BG_ITEM, BG_ITEM_HV, () -> showGameDetailPanel(game));
        return row;
    }

    private JPanel buildUserResultRow(User user) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(BG_ITEM);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COL),
                new EmptyBorder(10, 14, 10, 14)));

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JLabel nameLbl = new JLabel(user.getUsername());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(TEXT_PRI);

        String bioRaw = user.getBio();
        String bio    = (bioRaw != null && bioRaw.length() > 80)
                ? bioRaw.substring(0, 80) + "…" : (bioRaw == null ? "" : bioRaw);
        JLabel bioLbl = new JLabel(bio.isEmpty() ? "No bio." : bio);
        bioLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bioLbl.setForeground(TEXT_SEC);

        info.add(nameLbl);
        info.add(bioLbl);
        row.add(info, BorderLayout.CENTER);

        // Right: join date
        if (user.getJoinDate() != null) {
            JLabel joinedLbl = new JLabel("Joined " + user.getJoinDate().toString());
            joinedLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            joinedLbl.setForeground(TEXT_SEC);
            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            wrap.setOpaque(false);
            wrap.add(joinedLbl);
            row.add(wrap, BorderLayout.EAST);
        }

        addHoverAndClick(row, BG_ITEM, BG_ITEM_HV, () -> showUserDetailPanel(user));
        return row;
    }

    private JLabel emptyResultLabel(String msg) {
        JLabel lbl = new JLabel(msg, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lbl.setForeground(TEXT_SEC);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(30, 0, 0, 0));
        return lbl;
    }


    // =========================================================================
    // PAGINATION
    // =========================================================================
    /**
     * Clears pagPanel and rebuilds page number buttons.
     * Shows:  ◀  1  2  3  …  n  ▶
     * with the current page highlighted.
     *
     * @param onPageSelect called with the chosen page number (1-indexed)
     */
    private void rebuildPagination(JPanel pagPanel,
                                   int currentPage,
                                   int totalPages,
                                   PageSelectListener onPageSelect) {
        pagPanel.removeAll();

        if (totalPages <= 1) {
            pagPanel.revalidate();
            pagPanel.repaint();
            return;
        }

        // < Prev
        JButton prev = pageNavButton("<");
        prev.setEnabled(currentPage > 1);
        prev.addActionListener(e -> onPageSelect.onSelect(currentPage - 1));
        pagPanel.add(prev);

        // Page number buttons — always show first, last, and a window around current.
        // Use a skipping flag so exactly one "..." is emitted per collapsed gap,
        // and no real page number is ever replaced by "...".
        boolean skipping = false;
        for (int p = 1; p <= totalPages; p++) {
            boolean nearCurrent = Math.abs(p - currentPage) <= 2;
            boolean isEdge      = (p == 1 || p == totalPages);

            if (nearCurrent || isEdge) {
                skipping = false;
                JButton pageBtn = pageNumberButton(String.valueOf(p), p == currentPage);
                final int pg = p;
                pageBtn.addActionListener(e -> onPageSelect.onSelect(pg));
                pagPanel.add(pageBtn);
            } else {
                if (!skipping) {
                    skipping = true;
                    JLabel ellipsis = new JLabel("...");
                    ellipsis.setForeground(TEXT_SEC);
                    ellipsis.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    pagPanel.add(ellipsis);
                }
            }
        }

        // > Next
        JButton next = pageNavButton(">");
        next.setEnabled(currentPage < totalPages);
        next.addActionListener(e -> onPageSelect.onSelect(currentPage + 1));
        pagPanel.add(next);

        pagPanel.revalidate();
        pagPanel.repaint();
    }

    /** Functional interface for pagination callbacks. */
    @FunctionalInterface
    private interface PageSelectListener {
        void onSelect(int page);
    }

    private JButton pageNumberButton(String text, boolean active) {
        Color bg = active ? ACCENT_DIM : BG_ITEM;
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() && !active ? BG_ITEM_HV : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(active ? Color.WHITE : TEXT_PRI);
        btn.setMargin(new Insets(0, 0, 0, 0));  // override Metal's default (2,14,2,14) margin
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(34, 30));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setEnabled(!active);   // no-op click on current page
        return btn;
    }

    private JButton pageNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(TEXT_PRI);
        btn.setMargin(new Insets(0, 0, 0, 0));  // override Metal's default (2,14,2,14) margin
        btn.setBackground(BG_ITEM);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL),
                new EmptyBorder(2, 8, 2, 8)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(40, 30));
        return btn;
    }

    // =========================================================================
    // SHARED HELPERS
    // =========================================================================

    /** Attach hover colouring and a click action to any JPanel. */
    private void addHoverAndClick(JPanel panel, Color normal, Color hover, Runnable onClick) {
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { panel.setBackground(hover);  }
            @Override public void mouseExited (MouseEvent e) { panel.setBackground(normal); }
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Style a JScrollBar to match the dark theme. */
    private void styleScrollBar(JScrollBar bar) {
        bar.setBackground(BG_SIDE);
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor       = new Color(0x30363D);
                this.trackColor       = BG_SIDE;
                this.thumbHighlightColor   = new Color(0x444C56);
                this.thumbDarkShadowColor  = BG_SIDE;
                this.thumbLightShadowColor = BG_SIDE;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    // ── Field / button factory methods (reused from auth panels) ─────────────
    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SEC);
        return lbl;
    }

    private JTextField styledTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(0, 36));
        tf.setBackground(new Color(0x21262D));
        tf.setForeground(TEXT_PRI);
        tf.setCaretColor(TEXT_PRI);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        return tf;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setPreferredSize(new Dimension(0, 36));
        pf.setBackground(new Color(0x21262D));
        pf.setForeground(TEXT_PRI);
        pf.setCaretColor(TEXT_PRI);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COL),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        return pf;
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
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
        btn.setForeground(ACCENT);
        btn.setBackground(BG_DARK);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }


    // =========================================================================
    // GAME DETAIL PANEL
    // =========================================================================
    private JPanel buildGameDetailPanel(Game game) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_DARK);

        ScrollablePanel infoContent = new ScrollablePanel();
        infoContent.setBackground(BG_DARK);
        infoContent.setBorder(new EmptyBorder(24, 28, 24, 20));

        // ── Title row: title (left) + rating column (right) ───────────────────
        JPanel titleRow = new JPanel(new BorderLayout(16, 0));
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Title — switches to editable on Edit press
        JTextField titleField = new JTextField(game.getTitle() != null ? game.getTitle() : "");
        titleField.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleField.setForeground(TEXT_PRI);
        titleField.setBackground(BG_DARK);
        titleField.setCaretColor(TEXT_PRI);
        titleField.setEditable(false);
        titleField.setFocusable(false);
        titleField.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        titleRow.add(titleField, BorderLayout.CENTER);

        // Rating column — fixed pixel size, Java2D polygon stars (no Unicode).
        double avgRating   = db.getAvgRating(game.getGameID());
        int    reviewCount = db.getGameReviewCount(game.getGameID());

        String ratingStr = (avgRating == Math.floor(avgRating))
                ? String.format("%.0f / 5", avgRating)
                : String.format("%.1f / 5", avgRating);
        JLabel ratingNumLbl = new JLabel(ratingStr);
        ratingNumLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ratingNumLbl.setForeground(new Color(0xF1C40F));
        ratingNumLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel reviewsLbl = new JLabel(reviewCount + (reviewCount == 1 ? " review" : " reviews"));
        reviewsLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        reviewsLbl.setForeground(TEXT_SEC);
        reviewsLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel starsPanel = buildStarRatingPanel(avgRating);
        starsPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel ratingCol = new JPanel();
        ratingCol.setLayout(new BoxLayout(ratingCol, BoxLayout.Y_AXIS));
        ratingCol.setOpaque(false);
        ratingCol.setPreferredSize(new Dimension(120, 72));
        ratingCol.setMinimumSize(new Dimension(120, 72));
        ratingCol.setMaximumSize(new Dimension(120, 72));
        ratingCol.add(starsPanel);
        ratingCol.add(Box.createVerticalStrut(4));
        ratingCol.add(ratingNumLbl);
        ratingCol.add(Box.createVerticalStrut(2));
        ratingCol.add(reviewsLbl);

        titleRow.add(ratingCol, BorderLayout.EAST);
        infoContent.add(titleRow);
        infoContent.add(Box.createVerticalStrut(8));

        // ── Developer ─────────────────────────────────────────────────────────
        JLabel devLbl = new JLabel(game.getDeveloper() != null ? game.getDeveloper() : "Unknown Developer");
        devLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        devLbl.setForeground(TEXT_SEC);
        devLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoContent.add(devLbl);

        // ── Hours played (only shown if user owns the game) ────────────────────
        boolean userOwns    = db.userOwnsGame(loggedInUser.getUserID(), game.getGameID());
        boolean userCreated = db.userIsCreator(loggedInUser, game);
        if (userOwns) {
            int hours = db.getHoursPlayed(loggedInUser.getUserID(), game.getGameID());
            JLabel hoursLbl = new JLabel("Hours Played: " + hours);
            hoursLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            hoursLbl.setForeground(TEXT_SEC);
            hoursLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoContent.add(Box.createVerticalStrut(4));
            infoContent.add(hoursLbl);
        }

        infoContent.add(Box.createVerticalStrut(20));

        addDetailSeparator(infoContent);
        infoContent.add(Box.createVerticalStrut(16));

        // ── Description — switches to editable on Edit press ──────────────────
        JTextArea descArea = new JTextArea(game.getGameDescription() != null ? game.getGameDescription() : "");
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setForeground(TEXT_PRI);
        descArea.setBackground(BG_DARK);
        descArea.setCaretColor(TEXT_PRI);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoContent.add(descArea);
        infoContent.add(Box.createVerticalStrut(24));

        addDetailSeparator(infoContent);
        infoContent.add(Box.createVerticalStrut(16));

        // ── Stats ─────────────────────────────────────────────────────────────
        infoContent.add(detailStatLabel(String.format("%,d Sales", game.getSales())));
        infoContent.add(Box.createVerticalStrut(8));
        infoContent.add(detailStatLabel(
                "Released " + (game.getReleaseDate() != null ? game.getReleaseDate().toString() : "Unknown")));
        infoContent.add(Box.createVerticalStrut(8));
        infoContent.add(detailStatLabel(
                "Age Rating: " + (game.getAgeRating() != null ? game.getAgeRating() : "Not Rated")));
        infoContent.add(Box.createVerticalStrut(16));

        JScrollPane infoScroll = new JScrollPane(infoContent,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.setBorder(null);
        infoScroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(infoScroll.getVerticalScrollBar());
        // Match viewport background to the panel so no white gap shows
        // when content is shorter than the visible area.
        infoScroll.getViewport().setBackground(BG_DARK);

        // ── Reviews panel (right ~1/3) ────────────────────────────────────────
        JPanel reviewsPanel = buildGameReviewsPanel(game, userOwns);
        reviewsPanel.setPreferredSize(new Dimension(300, 0));
        outer.add(infoScroll,   BorderLayout.CENTER);
        outer.add(reviewsPanel, BorderLayout.EAST);

        // ── Action bar (SOUTH, always visible) ────────────────────────────────
        // userOwns and userCreated are already computed above (needed for hours played)
        boolean[] editing   = {false};   // captured by lambdas below

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionBar.setBackground(BG_SIDE);
        actionBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COL));

        // Edit/Save button — only shown if logged-in user is the game's creator
        if (userCreated) {
            JButton editBtn = styledButton("Edit", new Color(0x6E7681));
            editBtn.setPreferredSize(new Dimension(90, 36));
            editBtn.addActionListener(e -> {
                editing[0] = !editing[0];
                if (editing[0]) {
                    titleField.setFocusable(true);
                    titleField.setEditable(true);
                    titleField.setBackground(BG_ITEM);
                    titleField.setBorder(BorderFactory.createLineBorder(ACCENT_DIM));
                    descArea.setFocusable(true);
                    descArea.setEditable(true);
                    descArea.setBackground(BG_ITEM);
                    descArea.setBorder(BorderFactory.createLineBorder(ACCENT_DIM));
                    editBtn.setText("Save");
                } else {
                    String newTitle = titleField.getText().trim();
                    String newDesc  = descArea.getText().trim();
                    if (!newTitle.isEmpty()) {
                        db.updateGameInfo(game.getGameID(), newTitle, newDesc);
                    }
                    titleField.setEditable(false);
                    titleField.setFocusable(false);
                    titleField.setBackground(BG_DARK);
                    titleField.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                    descArea.setEditable(false);
                    descArea.setFocusable(false);
                    descArea.setBackground(BG_DARK);
                    descArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                    editBtn.setText("Edit");
                }
            });
            actionBar.add(editBtn);
        }

        // ADD (not owned) or LOG (owned)
        if (!userOwns) {
            JButton addBtn = styledButton("ADD", BTN_GREEN);
            addBtn.setPreferredSize(new Dimension(90, 36));
            addBtn.addActionListener(e -> {
                db.addGameToLibrary(loggedInUser.getUserID(), game.getGameID());
                refreshLibrary();
                showGameDetailPanel(game);   // rebuild panel to reflect new ownership
            });
            actionBar.add(addBtn);
        } else {
            JButton logBtn = styledButton("LOG", ACCENT_DIM);
            logBtn.setPreferredSize(new Dimension(90, 36));
            logBtn.addActionListener(e -> {
                String input = JOptionPane.showInputDialog(this,
                        "How many hours have you played " + game.getTitle() + "?",
                        "Log Playtime", JOptionPane.PLAIN_MESSAGE);
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int hours = Integer.parseInt(input.trim());
                        if (hours < 1) {
                            throw new NumberFormatException("Hours logged must be greater than 0");
                        }
                        db.logHoursPlayed(loggedInUser.getUserID(), game.getGameID(), hours);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Please enter a valid whole number of hours.",
                                "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            actionBar.add(logBtn);
        }

        outer.add(actionBar, BorderLayout.SOUTH);
        return outer;
    }


    // =========================================================================
    // USER DETAIL PANEL
    // =========================================================================
    private JPanel buildUserDetailPanel(User user) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_DARK);

        // ── Scrollable info content ───────────────────────────────────────────
        ScrollablePanel infoContent = new ScrollablePanel();
        infoContent.setBackground(BG_DARK);
        infoContent.setBorder(new EmptyBorder(24, 28, 24, 20));

        // ── Username row: username (left) + review count (right) ─────────────
        JPanel usernameRow = new JPanel(new BorderLayout(16, 0));
        usernameRow.setOpaque(false);
        usernameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JTextField usernameField = new JTextField(user.getUsername() != null ? user.getUsername() : "");
        usernameField.setFont(new Font("Segoe UI", Font.BOLD, 24));
        usernameField.setForeground(TEXT_PRI);
        usernameField.setBackground(BG_DARK);
        usernameField.setCaretColor(TEXT_PRI);
        usernameField.setEditable(false);
        usernameField.setFocusable(false);
        usernameField.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        usernameRow.add(usernameField, BorderLayout.CENTER);

        int userReviewCount = db.getUserReviewCount(user.getUserID());
        JLabel reviewCountLbl = new JLabel(userReviewCount + (userReviewCount == 1 ? " review" : " reviews"));
        reviewCountLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reviewCountLbl.setForeground(TEXT_SEC);
        usernameRow.add(reviewCountLbl, BorderLayout.EAST);

        infoContent.add(usernameRow);
        infoContent.add(Box.createVerticalStrut(4));

        // ── Email ─────────────────────────────────────────────────────────────
        JTextField emailField = new JTextField(user.getUserEmail() != null ? user.getUserEmail() : "");
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emailField.setForeground(TEXT_SEC);
        emailField.setBackground(BG_DARK);
        emailField.setCaretColor(TEXT_PRI);
        emailField.setEditable(false);
        emailField.setFocusable(false);
        emailField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        infoContent.add(emailField);
        infoContent.add(Box.createVerticalStrut(20));

        addDetailSeparator(infoContent);
        infoContent.add(Box.createVerticalStrut(16));

        // ── Bio ───────────────────────────────────────────────────────────────
        boolean hasBio    = user.getBio() != null && !user.getBio().trim().isEmpty();
        String  bioText   = hasBio ? user.getBio() : "User has no bio.";
        JTextArea bioArea = new JTextArea(bioText);
        bioArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bioArea.setForeground(hasBio ? TEXT_PRI : TEXT_SEC);
        bioArea.setBackground(BG_DARK);
        bioArea.setCaretColor(TEXT_PRI);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        bioArea.setEditable(false);
        bioArea.setFocusable(false);
        bioArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        bioArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        bioArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        infoContent.add(bioArea);
        infoContent.add(Box.createVerticalStrut(16));

        JScrollPane infoScroll = new JScrollPane(infoContent,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.setBorder(null);
        infoScroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(infoScroll.getVerticalScrollBar());
        infoScroll.getViewport().setBackground(BG_DARK);

        infoScroll.getViewport().setBackground(BG_DARK);

        // ── Reviews panel (right ~1/3) ────────────────────────────────────────
        JPanel reviewsPanel = buildUserReviewsPanel(user);
        reviewsPanel.setPreferredSize(new Dimension(300, 0));
        outer.add(infoScroll,   BorderLayout.CENTER);
        outer.add(reviewsPanel, BorderLayout.EAST);

        // ── Action bar — only for the logged-in user's own profile ────────────
        boolean isOwnProfile = (user.getUserID() == loggedInUser.getUserID());
        if (isOwnProfile) {
            boolean[] editing = {false};

            JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            actionBar.setBackground(BG_SIDE);
            actionBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COL));

            JButton editBtn = styledButton("Edit", new Color(0x6E7681));
            editBtn.setPreferredSize(new Dimension(90, 36));
            editBtn.addActionListener(e -> {
                editing[0] = !editing[0];
                if (editing[0]) {
                    usernameField.setFocusable(true);
                    usernameField.setEditable(true);
                    usernameField.setBackground(BG_ITEM);
                    usernameField.setBorder(BorderFactory.createLineBorder(ACCENT_DIM));
                    emailField.setFocusable(true);
                    emailField.setEditable(true);
                    emailField.setBackground(BG_ITEM);
                    emailField.setBorder(BorderFactory.createLineBorder(ACCENT_DIM));
                    if (bioArea.getText().equals("User has no bio.")) bioArea.setText("");
                    bioArea.setFocusable(true);
                    bioArea.setEditable(true);
                    bioArea.setForeground(TEXT_PRI);
                    bioArea.setBackground(BG_ITEM);
                    bioArea.setBorder(BorderFactory.createLineBorder(ACCENT_DIM));
                    editBtn.setText("Save");
                } else {
                    String newUsername = usernameField.getText().trim();
                    String newEmail    = emailField.getText().trim();
                    String newBio      = bioArea.getText().trim();
                    if (!newUsername.isEmpty() && !newEmail.isEmpty()) {
                        db.updateUserInfo(loggedInUser.getUserID(), newUsername, newEmail, newBio);
                        loggedInUser.setUsername(newUsername);
                        loggedInUser.setUserEmail(newEmail);
                        loggedInUser.setBio(newBio);
                    }
                    usernameField.setEditable(false);
                    usernameField.setFocusable(false);
                    usernameField.setBackground(BG_DARK);
                    usernameField.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                    emailField.setEditable(false);
                    emailField.setFocusable(false);
                    emailField.setBackground(BG_DARK);
                    emailField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                    bioArea.setEditable(false);
                    bioArea.setFocusable(false);
                    bioArea.setBackground(BG_DARK);
                    bioArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                    if (newBio.isEmpty()) {
                        bioArea.setText("User has no bio.");
                        bioArea.setForeground(TEXT_SEC);
                    }
                    editBtn.setText("Edit");
                }
            });
            actionBar.add(editBtn);
            outer.add(actionBar, BorderLayout.SOUTH);
        }

        return outer;
    }


    // =========================================================================
    // GAME REVIEWS PANEL
    // =========================================================================
    /**
     * Builds the right-side reviews panel for a game page.
     * Shows up to REVIEWS_PER_PAGE reviews at a time with pagination.
     * If userOwns is true, a "+" button in the header lets the user add a review.
     */
    private JPanel buildGameReviewsPanel(Game game, boolean userOwns) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SIDE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COL));

        // ── Header row ────────────────────────────────────────────────────────
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(BG_SIDE);
        headerRow.setPreferredSize(new Dimension(0, 36));
        headerRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COL));

        JLabel headerLbl = new JLabel("  REVIEWS");
        headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        headerLbl.setForeground(TEXT_SEC);
        headerRow.add(headerLbl, BorderLayout.WEST);

        if (userOwns) {
            JButton addReviewBtn = new JButton("+");
            addReviewBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            addReviewBtn.setForeground(ACCENT);
            addReviewBtn.setContentAreaFilled(false);
            addReviewBtn.setBorderPainted(false);
            addReviewBtn.setFocusPainted(false);
            addReviewBtn.setMargin(new Insets(0, 0, 0, 6));
            addReviewBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addReviewBtn.setToolTipText("Write a review");
            addReviewBtn.addActionListener(e -> showCreateReviewPanel(game));
            headerRow.add(addReviewBtn, BorderLayout.EAST);
        }
        panel.add(headerRow, BorderLayout.NORTH);

        // ── Review list ───────────────────────────────────────────────────────
        LinkedList<Review> allReviews = db.getGameReviews(game.getGameID());

        ScrollablePanel reviewListPanel = new ScrollablePanel();
        reviewListPanel.setBackground(BG_SIDE);

        JScrollPane scroll = new JScrollPane(reviewListPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.getViewport().setBackground(BG_SIDE);
        styleScrollBar(scroll.getVerticalScrollBar());
        panel.add(scroll, BorderLayout.CENTER);

        // ── Pagination ────────────────────────────────────────────────────────
        JPanel pagPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 4));
        pagPanel.setBackground(BG_SIDE);
        panel.add(pagPanel, BorderLayout.SOUTH);

        // Inner method (via array trick) to refresh the visible page
        int[] currentPage  = {1};
        Runnable[] refresh = {null};
        refresh[0] = () -> {
            reviewListPanel.removeAll();
            if (allReviews == null || allReviews.isEmpty()) {
                JLabel empty = new JLabel("No reviews yet.");
                empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                empty.setForeground(TEXT_SEC);
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                empty.setBorder(new EmptyBorder(16, 0, 0, 0));
                reviewListPanel.add(empty);
            } else {
                int start = (currentPage[0] - 1) * REVIEWS_PER_PAGE;
                int idx = 0;
                for (Review r : allReviews) {
                    if (idx >= start && idx < start + REVIEWS_PER_PAGE) {
                        reviewListPanel.add(buildGameReviewRow(r, refresh[0]));
                    }
                    if (++idx >= start + REVIEWS_PER_PAGE) break;
                }
            }
            reviewListPanel.revalidate();
            reviewListPanel.repaint();

            int total = (allReviews == null || allReviews.isEmpty()) ? 1
                    : (int) Math.ceil(allReviews.size() / (double) REVIEWS_PER_PAGE);
            rebuildPagination(pagPanel, currentPage[0], total, p -> {
                currentPage[0] = p;
                refresh[0].run();
            });
        };
        refresh[0].run();

        return panel;
    }


    // =========================================================================
    // USER REVIEWS PANEL
    // =========================================================================
    /**
     * Builds the right-side reviews panel for a user page.
     * Shows up to REVIEWS_PER_PAGE reviews the user has written, with pagination.
     */
    private JPanel buildUserReviewsPanel(User user) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SIDE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COL));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(BG_SIDE);
        headerRow.setPreferredSize(new Dimension(0, 36));
        headerRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COL));
        JLabel headerLbl = new JLabel("  REVIEWS");
        headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        headerLbl.setForeground(TEXT_SEC);
        headerRow.add(headerLbl, BorderLayout.WEST);
        panel.add(headerRow, BorderLayout.NORTH);

        LinkedList<Review> allReviews = db.getUserReviews(user.getUserID());

        ScrollablePanel reviewListPanel = new ScrollablePanel();
        reviewListPanel.setBackground(BG_SIDE);

        JScrollPane scroll = new JScrollPane(reviewListPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.getViewport().setBackground(BG_SIDE);
        styleScrollBar(scroll.getVerticalScrollBar());
        panel.add(scroll, BorderLayout.CENTER);

        JPanel pagPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 4));
        pagPanel.setBackground(BG_SIDE);
        panel.add(pagPanel, BorderLayout.SOUTH);

        int[] currentPage  = {1};
        Runnable[] refresh = {null};
        refresh[0] = () -> {
            reviewListPanel.removeAll();
            if (allReviews == null || allReviews.isEmpty()) {
                JLabel empty = new JLabel("No reviews yet.");
                empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                empty.setForeground(TEXT_SEC);
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                empty.setBorder(new EmptyBorder(16, 0, 0, 0));
                reviewListPanel.add(empty);
            } else {
                int start = (currentPage[0] - 1) * REVIEWS_PER_PAGE;
                int idx = 0;
                for (Review r : allReviews) {
                    if (idx >= start && idx < start + REVIEWS_PER_PAGE) {
                        reviewListPanel.add(buildUserReviewRow(r, refresh[0]));
                    }
                    if (++idx >= start + REVIEWS_PER_PAGE) break;
                }
            }
            reviewListPanel.revalidate();
            reviewListPanel.repaint();

            int total = (allReviews == null || allReviews.isEmpty()) ? 1
                    : (int) Math.ceil(allReviews.size() / (double) REVIEWS_PER_PAGE);
            rebuildPagination(pagPanel, currentPage[0], total, p -> {
                currentPage[0] = p;
                refresh[0].run();
            });
        };
        refresh[0].run();

        return panel;
    }


    // =========================================================================
    // REVIEW ROW BUILDERS
    // =========================================================================

    /**
     * One review row for a game's review panel.
     * Top-left: username (clickable link → user page)
     * Top-right: N/5 rating text
     * Below: review text
     *
     * NOTE: Requires getUserID() and getGameID() getters on Review.java.
     * See stub methods at the bottom of this file.
     */
    /**
     * One review row for a game's review panel.
     * Top-left: username (clickable → user page)
     * Top-right: N/5 rating  +  Delete button (only if this review belongs to the logged-in user)
     * Below: review text
     *
     * @param onDelete called after a successful deletion so the review list refreshes
     */
    private JPanel buildGameReviewRow(Review review, Runnable onDelete) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setBackground(BG_ITEM);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COL),
                new EmptyBorder(8, 10, 8, 10)));

        // Top row: username link (left) + [rating + optional delete] (right)
        JPanel topRow = new JPanel(new BorderLayout(6, 0));
        topRow.setOpaque(false);

        User reviewer = review.getUser();
        String username = (reviewer != null) ? reviewer.getUsername() : "Unknown User";
        JButton userLink = new JButton(username);
        userLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLink.setForeground(ACCENT);
        userLink.setContentAreaFilled(false);
        userLink.setBorderPainted(false);
        userLink.setFocusPainted(false);
        userLink.setMargin(new Insets(0, 0, 0, 0));
        userLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userLink.addActionListener(e -> {
            if (reviewer != null) showUserDetailPanel(reviewer);
        });
        topRow.add(userLink, BorderLayout.WEST);

        // Right side: rating label, and delete button if this is the logged-in user's review
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);

        double starRating = review.getRating() / 2.0;
        String ratingTxt  = (starRating == Math.floor(starRating))
                ? String.format("%.0f/5", starRating)
                : String.format("%.1f/5", starRating);
        JLabel ratingLbl = new JLabel(ratingTxt);
        ratingLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ratingLbl.setForeground(new Color(0xF1C40F));
        rightPanel.add(ratingLbl);

        boolean isOwner = reviewer != null &&
                reviewer.getUserID() == loggedInUser.getUserID();
        if (isOwner) {
            rightPanel.add(buildDeleteButton(review, onDelete));
        }

        topRow.add(rightPanel, BorderLayout.EAST);
        row.add(topRow, BorderLayout.NORTH);

        // Review text
        JTextArea textArea = new JTextArea(review.getReviewText() != null ? review.getReviewText() : "");
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textArea.setForeground(TEXT_PRI);
        textArea.setBackground(BG_ITEM);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setBorder(null);
        row.add(textArea, BorderLayout.CENTER);

        return row;
    }

    /**
     * One review row for a user's review panel.
     * Top-left: game title (clickable → game page)
     * Top-right: N/5 rating  +  Delete button (only if this review belongs to the logged-in user)
     * Below: review text
     *
     * @param onDelete called after a successful deletion so the review list refreshes
     */
    private JPanel buildUserReviewRow(Review review, Runnable onDelete) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setBackground(BG_ITEM);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COL),
                new EmptyBorder(8, 10, 8, 10)));

        JPanel topRow = new JPanel(new BorderLayout(6, 0));
        topRow.setOpaque(false);

        Game reviewedGame = review.getGame();
        String gameTitle = (reviewedGame != null) ? reviewedGame.getTitle() : "Unknown Game";
        JButton gameLink = new JButton(gameTitle);
        gameLink.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gameLink.setForeground(ACCENT);
        gameLink.setContentAreaFilled(false);
        gameLink.setBorderPainted(false);
        gameLink.setFocusPainted(false);
        gameLink.setMargin(new Insets(0, 0, 0, 0));
        gameLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gameLink.addActionListener(e -> {
            if (reviewedGame != null) showGameDetailPanel(reviewedGame);
        });
        topRow.add(gameLink, BorderLayout.WEST);

        // Right side: rating label, and delete button if this is the logged-in user's review
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightPanel.setOpaque(false);

        double starRating = review.getRating() / 2.0;
        String ratingTxt  = (starRating == Math.floor(starRating))
                ? String.format("%.0f/5", starRating)
                : String.format("%.1f/5", starRating);
        JLabel ratingLbl = new JLabel(ratingTxt);
        ratingLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ratingLbl.setForeground(new Color(0xF1C40F));
        rightPanel.add(ratingLbl);

        User reviewer = review.getUser();
        boolean isOwner = reviewer != null &&
                reviewer.getUserID() == loggedInUser.getUserID();
        if (isOwner) {
            rightPanel.add(buildDeleteButton(review, onDelete));
        }

        topRow.add(rightPanel, BorderLayout.EAST);
        row.add(topRow, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea(review.getReviewText() != null ? review.getReviewText() : "");
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textArea.setForeground(TEXT_PRI);
        textArea.setBackground(BG_ITEM);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setBorder(null);
        row.add(textArea, BorderLayout.CENTER);

        return row;
    }


    /**
     * Builds the small red Delete button shown on reviews owned by the logged-in user.
     * Asks for confirmation, then calls db.deleteReview() and invokes onDelete to
     * refresh the review list the button lives in.
     *
     * ReviewID is retrieved via findReviewID() — see the stub at the bottom of this file.
     */
    private JButton buildDeleteButton(Review review, Runnable onDelete) {
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        deleteBtn.setForeground(new Color(0xF85149));
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setBorderPainted(true);
        deleteBtn.setBorder(BorderFactory.createLineBorder(new Color(0xF85149)));
        deleteBtn.setFocusPainted(false);
        deleteBtn.setMargin(new Insets(1, 5, 1, 5));
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this review?",
                    "Delete Review",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            int reviewID = review.getReviewID();
            if (reviewID <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Could not identify this review. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = db.deleteReview(reviewID);
            if (success) {
                onDelete.run();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete review. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return deleteBtn;
    }


    // =========================================================================
    // CREATE REVIEW PANEL
    // =========================================================================
    private JPanel buildCreateReviewPanel(Game game) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_DARK);

        ScrollablePanel content = new ScrollablePanel();
        content.setBackground(BG_DARK);
        content.setBorder(new EmptyBorder(28, 28, 20, 28));

        // Title
        JLabel titleLbl = new JLabel("Review: " + game.getTitle());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(TEXT_PRI);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(24));

        // ── Star rating selector ───────────────────────────────────────────────
        JLabel ratingHeader = new JLabel("Your Rating");
        ratingHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ratingHeader.setForeground(TEXT_SEC);
        ratingHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(ratingHeader);
        content.add(Box.createVerticalStrut(6));

        double[] ratingHolder = {3.0};

        JLabel ratingNumLbl = new JLabel("3.0 / 5");
        ratingNumLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ratingNumLbl.setForeground(new Color(0xF1C40F));

        // Callback to update the label when a star is clicked
        Runnable onRatingChanged = () -> {
            String rs = (ratingHolder[0] == Math.floor(ratingHolder[0]))
                    ? String.format("%.0f / 5", ratingHolder[0])
                    : String.format("%.1f / 5", ratingHolder[0]);
            ratingNumLbl.setText(rs);
        };

        JPanel starRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        starRow.setOpaque(false);
        starRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        starRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JPanel interactiveStars = buildInteractiveStarPanel(ratingHolder, onRatingChanged);
        starRow.add(interactiveStars);
        starRow.add(ratingNumLbl);
        content.add(starRow);
        content.add(Box.createVerticalStrut(20));

        // ── Review text ────────────────────────────────────────────────────────
        JLabel reviewHeader = new JLabel("Your Review");
        reviewHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reviewHeader.setForeground(TEXT_SEC);
        reviewHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(reviewHeader);
        content.add(Box.createVerticalStrut(6));

        JTextArea reviewText = new JTextArea();
        reviewText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reviewText.setForeground(TEXT_PRI);
        reviewText.setBackground(BG_ITEM);
        reviewText.setCaretColor(TEXT_PRI);
        reviewText.setLineWrap(true);
        reviewText.setWrapStyleWord(true);
        reviewText.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane textScroll = new JScrollPane(reviewText,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textScroll.setBorder(BorderFactory.createLineBorder(BORDER_COL));
        textScroll.setPreferredSize(new Dimension(0, 160));
        textScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        textScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        styleScrollBar(textScroll.getVerticalScrollBar());
        content.add(textScroll);

        JScrollPane outerScroll = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outerScroll.setBorder(null);
        outerScroll.getViewport().setBackground(BG_DARK);
        styleScrollBar(outerScroll.getVerticalScrollBar());
        outer.add(outerScroll, BorderLayout.CENTER);

        // ── Button bar ────────────────────────────────────────────────────────
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonBar.setBackground(BG_SIDE);
        buttonBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COL));

        JButton cancelBtn = styledButton("Cancel", new Color(0x6E7681));
        cancelBtn.setPreferredSize(new Dimension(90, 36));
        cancelBtn.addActionListener(e -> showGameDetailPanel(game));

        JButton submitBtn = styledButton("Create Review", BTN_GREEN);
        submitBtn.setPreferredSize(new Dimension(150, 36));
        submitBtn.addActionListener(e -> {
            String text = reviewText.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please write a review before submitting.",
                        "Missing Review", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // ratingHolder[0] is 0.5–5.0; database stores as 1–10 (/10 scale)
            byte dbRating = (byte) Math.round(ratingHolder[0] * 2);
            Review review = new Review(text, dbRating, "");
            boolean success = db.createReview(loggedInUser.getUserID(), game.getGameID(), review);
            if (success) {
                showGameDetailPanel(game);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to submit review. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonBar.add(cancelBtn);
        buttonBar.add(submitBtn);
        outer.add(buttonBar, BorderLayout.SOUTH);

        return outer;
    }


    // =========================================================================
    // INTERACTIVE STAR RATING WIDGET  (used in create review panel)
    // =========================================================================
    /**
     * Returns a clickable star panel backed by ratingHolder[0] (0.5 – 5.0).
     * Click a star once → full star (integer value).
     * Click the same full star again → half star (value - 0.5, min 0.5).
     * onChanged is called after every click so the caller can update a label.
     */
    private JPanel buildInteractiveStarPanel(double[] ratingHolder, Runnable onChanged) {
        final int STAR_SPACING = 32;
        final int OUTER_R      = 12;
        final int INNER_R      = 5;

        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color gold  = new Color(0xF1C40F);
                Color empty = new Color(0x444C56);
                int yCenter = getHeight() / 2;
                for (int i = 0; i < 5; i++) {
                    int cx   = i * STAR_SPACING + OUTER_R + 4;
                    double fill = Math.min(1.0, Math.max(0.0, ratingHolder[0] - i));
                    paintStar(g2, cx, yCenter, OUTER_R, INNER_R, fill, gold, empty);
                }
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(5 * STAR_SPACING + 8, 30));
        panel.setMaximumSize(new Dimension(5 * STAR_SPACING + 8, 30));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                // Map x-pixel to star index 1–5
                int starIdx = e.getX() / STAR_SPACING + 1;
                if (starIdx < 1) starIdx = 1;
                if (starIdx > 5) starIdx = 5;

                if (ratingHolder[0] == starIdx) {
                    // Same full star clicked → toggle to half
                    ratingHolder[0] = Math.max(0.5, starIdx - 0.5);
                } else {
                    ratingHolder[0] = starIdx;
                }
                panel.repaint();
                if (onChanged != null) onChanged.run();
            }
        });

        return panel;
    }


    // =========================================================================
    // STAR RATING COMPONENT
    // =========================================================================
    /**
     * Returns a fixed-size JPanel that paints 5 stars using Java2D polygons —
     * fully compatible with Metal L&F (no Unicode characters required).
     * Each star can be full, half, or empty depending on the rating value.
     *
     * @param rating a value from 0.0 to 5.0; supports half-star increments
     */
    private JPanel buildStarRatingPanel(double rating) {
        return new JPanel() {
            {
                setOpaque(false);
                setPreferredSize(new Dimension(115, 22));
                setMaximumSize(new Dimension(115, 22));
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color goldColor  = new Color(0xF1C40F);
                Color emptyColor = new Color(0x444C56);
                int outerR = 9;
                int innerR = 4;
                int spacing = 23;
                int yCenter = getHeight() / 2;

                for (int i = 0; i < 5; i++) {
                    int cx = i * spacing + outerR + 2;
                    // How much of this star is filled (0.0 – 1.0)
                    double fill = Math.min(1.0, Math.max(0.0, rating - i));
                    paintStar(g2, cx, yCenter, outerR, innerR, fill, goldColor, emptyColor);
                }
                g2.dispose();
            }
        };
    }

    private JLabel buildStarRatingLabel(double rating) {
        // rating is 0.0–5.0
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            double diff = rating - i;
            if (diff >= 0.75)      sb.append("★");   // full star
            else if (diff >= 0.25) sb.append("½");   // half star (or use "✦")
            else                   sb.append("☆");   // empty star
        }
        JLabel lbl = new JLabel(sb.toString());
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lbl.setForeground(new Color(0xF1C40F));
        lbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
        return lbl;
    }

    /**
     * Paints one 5-pointed star centred at (cx, cy).
     * fill 0.0 = empty, 0.5 = half, 1.0 = full.
     */
    private void paintStar(Graphics2D g2, int cx, int cy,
                            int outerR, int innerR, double fill,
                            Color fillColor, Color emptyColor) {
        final int POINTS = 5;
        int[] xPts = new int[POINTS * 2];
        int[] yPts = new int[POINTS * 2];

        for (int i = 0; i < POINTS; i++) {
            // Outer point
            double outerAngle = Math.PI / 2 + i * 2.0 * Math.PI / POINTS;
            xPts[i * 2]     = cx + (int) Math.round(outerR * Math.cos(outerAngle));
            yPts[i * 2]     = cy - (int) Math.round(outerR * Math.sin(outerAngle));
            // Inner (concave) point
            double innerAngle = outerAngle + Math.PI / POINTS;
            xPts[i * 2 + 1] = cx + (int) Math.round(innerR * Math.cos(innerAngle));
            yPts[i * 2 + 1] = cy - (int) Math.round(innerR * Math.sin(innerAngle));
        }

        Polygon star = new Polygon(xPts, yPts, POINTS * 2);
        java.awt.Rectangle b = star.getBounds();

        // Draw empty star as base
        g2.setColor(emptyColor);
        g2.fillPolygon(star);

        // Clip to the filled fraction and overdraw in gold
        if (fill > 0.0) {
            Shape oldClip = g2.getClip();
            int clipW = (int) Math.round(b.width * fill);
            g2.setClip(b.x, b.y, clipW, b.height);
            g2.setColor(fillColor);
            g2.fillPolygon(star);
            g2.setClip(oldClip);
        }
    }


    // =========================================================================
    // DETAIL PANEL HELPERS
    // =========================================================================
    /** A small coloured stat label used inside detail panels. */
    private JLabel detailStatLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_SEC);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /** Adds a full-width 1px horizontal divider line to a BoxLayout panel. */
    private void addDetailSeparator(JPanel panel) {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COL);
        sep.setBackground(BORDER_COL);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(sep);
    }

    /**
     * A JPanel that properly implements Scrollable so the JScrollPane viewport
     * constrains its width — required for JTextArea line-wrapping to work.
     * Cannot be an anonymous class because Java anonymous classes cannot
     * implement interfaces in addition to extending a class.
     */
    private static class ScrollablePanel extends JPanel implements javax.swing.Scrollable {
        ScrollablePanel() { setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); }
        @Override public Dimension getPreferredScrollableViewportSize()                        { return getPreferredSize(); }
        @Override public int  getScrollableUnitIncrement (java.awt.Rectangle r, int o, int d) { return 16; }
        @Override public int  getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) { return 50; }
        @Override public boolean getScrollableTracksViewportWidth()                            { return true;  }
        @Override public boolean getScrollableTracksViewportHeight()                           { return false; }
    }

}
