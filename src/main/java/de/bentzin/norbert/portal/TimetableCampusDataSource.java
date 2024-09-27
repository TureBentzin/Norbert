package de.bentzin.norbert.portal;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

    @Override
    public @NotNull timetableReturn getTimetableFor(@NotNull String modulCode) throws IllegalArgumentException, IOException {
        return new timetableReturn(getEvents(modulCode), modulCode);
    }

    @Override
    public void close() throws IOException {
        webUrl = null;
    }

    @Override
    public boolean isClosed(){
        return webUrl == null;
    }

    //TODO Logging and error handling
    private List<EventWrapper> getEvents(String lvnr) throws IOException{
        List<EventWrapper> result = new LinkedList<>();

        Connection con = Jsoup.newSession().url(webUrl);
        Document home = con.get();

        //extract semester gguid
        String semesterGguid = home.selectFirst("select[name=\"term\"]").firstElementChild().attr("value");

        List<Element> query = con.newRequest().url(new URL(webUrl, "/campus/all/eventlist.asp")).method(Connection.Method.GET)
                .data("find", lvnr,
                        "from", "",
                        "itemsperpage", "",
                        "tguid", semesterGguid,
                        "lang", "de")
                .post().selectFirst("tbody[class=\"tablecontent\"]").children().stream().toList();

        Iterator<Element> iter = query.iterator();

        while (iter.hasNext()){
            Element e = iter.next();
            if (e.is("tr[id]") || e.is("tr[class$=collapsible_closed]") || e.is("tr[class$=collapsible_opened]")){    //Header row
                if(
                        !e.selectFirst("td[id=\"SWS\"]").selectFirst("a").text().toLowerCase().contains("praktikum")   //kein Praktikum
                                && e.firstElementChild().is("td[class^=toggle]") && !e.firstElementChild().children().isEmpty()   //keine Termine
                ){
                    iter.next();    //Skip terminblock von nicht Praktikum
                }
            }else { //Time row

                String[] dateTimeString;
                for (Element timeRow : e.select("tr[class^=collapsible_closed]")) {
                    dateTimeString = timeRow.selectFirst("span[class^=date]").text().split(" ");
                    if(timeRow.is("tr[class=\"collapsible_closed\"]")){
                        result.add(new EventWrapper(
                                new DateEvent(LocalDate.parse(dateTimeString[0], DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                        LocalTime.parse(dateTimeString[1]),
                                        LocalTime.parse(dateTimeString[3])),
                                null,
                                false));

                    } else if(timeRow.is("tr[class=\"collapsible_closed failureApp\"]")){
                        result.add(new EventWrapper(
                                null,
                                new WeekdayEvent(
                                        DayOfWeek.of(WeekDayConverter.valueOf(dateTimeString[0]).ordinal()),
                                        LocalTime.parse(dateTimeString[1]),
                                        LocalTime.parse(dateTimeString[3])),
                                true));
                    }
                }
            }
        }

        return result;
    }

    private enum WeekDayConverter{
        PLUS_ONE,Mo,Di,Mi,Do,Fr,Sa,So
    }
}