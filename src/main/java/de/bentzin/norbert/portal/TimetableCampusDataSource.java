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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a data source for ta testat.etchnik.fh-aachen.de instance
 */
public class TimetableCampusDataSource implements TimetableDataSource {

    private URL webUrl = null;
    private final @NotNull Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void connect(@NotNull URL url) {
        webUrl = url;
    }

    @NotNull
    @Override
    public timetableReturn getOverviewFor(@NotNull String modulCode) throws IllegalArgumentException, IOException {
        //TODO
        return null;
    }


    @Override
    public void close() throws IOException {
        webUrl = null;
    }

    @Override
    public boolean isClosed(){
        return webUrl == null;
    }

}