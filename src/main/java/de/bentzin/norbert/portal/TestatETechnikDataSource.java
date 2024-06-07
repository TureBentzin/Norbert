package de.bentzin.norbert.portal;

import de.bentzin.norbert.Account;
import de.bentzin.norbert.Overview;
import de.bentzin.norbert.Task;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a data source for ta testat.etchnik.fh-aachen.de instance
 */
public class TestatETechnikDataSource  implements TestatDataSource {

    private URL webUrl;

    @Override
    public void connect(@NotNull URL url) {
        webUrl = url;
    }


    @Override
    public @NotNull OverviewReturn getOverviewFor(@NotNull Account account) throws IllegalArgumentException, IOException {
        Connection.Response res = Jsoup.connect(webUrl + "/student_login.php").data("F_Matr", Integer.toString(account.matr_nr())).method(Connection.Method.POST).execute();

        if (!res.parse().title().equals("PTV - Fh-Aachen - Studentenbereich")) {
            throw new IllegalArgumentException("Couldn't log in");
        }

        String sessionCookie = res.cookie("PHPSESSID");

        if (sessionCookie == null || sessionCookie.length() != 27) {
            throw new IOException("Couldn't extract session Cookie");
        }

        return new OverviewReturn(getOverviewFor(account, sessionCookie), sessionCookie);
    }

    @Override
    public @NotNull List<Overview> getOverviewFor(@NotNull Account account, @NotNull String sessionToken) throws IllegalArgumentException, IOException {
        List<Overview> res = new LinkedList<>();

        Connection con = Jsoup.newSession().cookie("PHPSESSID", "sessionToken");
        Document home = con.url(webUrl + "/student_login.php").post();
        if (!home.title().equals("PTV - Fh-Aachen - Studentenmenü")) {
            throw new IOException("couldn't load homepage");
        }

        Elements options = home.select("option");
        for (Element entry : options) {
            String id = entry.attr("value");

            Document modulPage = con.url(webUrl + "/student_login.php?do=show").data("F_AEvent", id).post();

            if (!modulPage.title().equals("PTV - Fh-Aachen - Testatverwaltung")) {
                throw new IOException("couldn't load modulPage");
            }

            Elements tableRows = modulPage.select("table#inner_table").select("tr:not([bgcolor])");

            Collection<Task> tasks = new LinkedList<>();

            tableRows.forEach(taskRow -> {
                boolean complete = Objects.requireNonNull(taskRow.child(2).firstChild()).attr("src").equals("images/checked.gif");  // lmao....
                tasks.add(new Task(taskRow.child(1).text(), complete));
            });

            res.add(new Overview(account, entry.text(), tasks));
        }


        return res;
    }

    @Override
    public void close() throws IOException {
        //TODO may be called at any time in program execution
        //Close all connections
    }
}