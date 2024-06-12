package ua.edu.nung.pz.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.logging.*;
import ua.edu.nung.pz.dao.entity.Firebase;
import ua.edu.nung.pz.dao.entity.User;
import ua.edu.nung.pz.dao.repository.UserRepository;
import ua.edu.nung.pz.view.MainPage;
import ua.edu.nung.pz.view.ViewConfig;

import java.io.*;
import java.util.Properties;


@WebServlet(name = "StartServlet", urlPatterns = {"/*"}, loadOnStartup = 1)
public class StartServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(StartServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String context = "";
        HttpSession httpSession = request.getSession(false);
        User user = null;
        String userName = "";

        if (httpSession != null) {
            user = (User) httpSession.getAttribute(User.USER_SESSION_NAME);
            userName = user == null ? "" : user.getDisplayName();
        }

        logger.info("Successfully started");

        switch (request.getPathInfo()) {
            case "/contacts":
                context = "<h2>Our Contacts!</h2>\n";
                break;
            case "/forgotpassword":
                context = "<h2>Restore Password!</h2>\n";
                break;
            default:
                context = "<h2>Hello World from Servlet!</h2>\n";
        }

        String builderPage = MainPage.Builder.newInstance()
                .setTitle("Green Shop")
                .setHeader(userName != null ? userName : "")  // Перевірка на null
                .setBody(context)
                .setFooter()
                .build()
                .getFullPage();

        out.println(builderPage);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contextPath = request.getContextPath();
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        HttpSession httpSession;
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        System.out.println(user.getEmail() + " " + user.getPassword());
        Firebase firebase = Firebase.getInstance();
        if (firebase.getUserByEmail(user.getEmail()).equals(Firebase.USER_EXISTS)) {
            String firebaseResponse = firebase.signInWithEmailAndPassword(user.getEmail(), user.getPassword());
            if(firebaseResponse.equals(Firebase.PASSWORD_OK)) {
                UserRepository userRepository = new UserRepository();
                User userDb = userRepository.getUserByEmail(user.getEmail());
                System.out.println(userDb.getEmail()+" "+ userDb.getPassword() +" "+ userDb.getDisplayName());
                if (userDb != null) {
                    System.out.println(userDb.getDisplayName());

                    userDb.setDisplayName("Test User");  // Встановлюємо DisplayName
                    httpSession = request.getSession();
                    httpSession.setAttribute(User.USER_SESSION_NAME, userDb);
                    logger.info("Successfully login " + userDb);
                } else {
                    logger.info("No user found in DB with email " + user.getEmail());
                }
            }  else {
                logger.info("Wrong Password " + user.getEmail());
            }
        } else {
            logger.info("User NOT Exist " + user.getEmail());
            //user.setDisplayName("Test User");
            String userMsg = firebase.createUser(user);
            httpSession = request.getSession();
            httpSession.setAttribute(User.USER_SESSION_NAME, user);
        }

        response.sendRedirect("/");
    }

    @Override
    public void init() throws ServletException {
        super.init();
        String pathBuilder = getServletContext().getRealPath("htmlBuilder/");

        ViewConfig viewConfig = ViewConfig.getInstance();
        viewConfig.setPath(pathBuilder);

        initFirebase();
        initLogger();
    }

    private void initFirebase() {
        Properties props = new Properties();
        InputStream is = getClass().getClassLoader().getResourceAsStream("app.properties");
        try {
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Firebase.getInstance().setFirebaseConfigPath(props.getProperty("file.path"));
        Firebase.getInstance().setFirebaseName(props.getProperty("firebase.name"));
        Firebase.getInstance().setApiKey(props.getProperty("web.api.key"));
        Firebase.getInstance().setSignInUrl(props.getProperty("signInUrl"));
        Firebase.getInstance().init();
    }

    private void initLogger() {
        try {
            Handler fh = new FileHandler(getServletContext().getRealPath("logs/app.log"));
            fh.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(fh);
            Logger.getLogger("").addHandler(new ConsoleHandler());
            Logger.getLogger("").setLevel(Level.INFO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
