package currencychart;

import com.google.gson.stream.JsonReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class currencychart_Controller {

    private final ObservableList<String> curr_code_List = FXCollections.observableArrayList("USD-Доллар США", "EUR-Евро", "GBP-Фунт");
    private final ObservableList<String> month_com_List = FXCollections.observableArrayList("01-Январь", "02-Февраль", "03-Март", "04-Апрель",
                                                                                "05-Май", "06-Июнь", "07-Июль", "08-Август",
                                                                                "09-Сентябрь", "10-Октябрь", "11-Ноябрь", "12-Декабрь");
    private final ObservableList<String> day_com_List = FXCollections.observableArrayList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
                                                                                    "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                                                                                    "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
                                                                                    "31");
    private final ObservableList<String> source_code_List = FXCollections.observableArrayList("Из файла JSON", "С сайта НБУ");
    private static final String tec_kat = new File("").getAbsolutePath();
    private static final String tec_kat_kurs = tec_kat + /*File.separator + "dist" +*/ File.separator + "kurs";

    @FXML private ComboBox<String> curr_code;
    @FXML private ComboBox<String> year_com;
    @FXML private TextField minus_year;
    @FXML private ComboBox<String> month_com;
    @FXML private TextField minus_month;
    @FXML private TextField plus_month;
    @FXML private CheckBox check_month;
    @FXML private ComboBox<String> day_com;
    @FXML private TextField minus_day;
    @FXML private TextField plus_day;
    @FXML private CheckBox check_day;
    @FXML private CheckBox sred_value_valut;
    @FXML private CheckBox visible_points;
    //@FXML private Button Calc_button;
    @FXML private Button button_kurs_nbu;
    @FXML private LineChart<String, Number> lchart;
    //@FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private ComboBox<String> source_code;
    @FXML private CheckBox del_cache_file;
    @FXML private CheckBox not_create_cache_file;

    @FXML
    private void initialize() throws TransformerException {
        // в интерфейсе
        // по умолчанию - валюта
        curr_code.setItems(curr_code_List);
        curr_code.getSelectionModel().select(0); // первое значение

        // по умолчанию - год
        Calendar now = Calendar.getInstance();   // Gets the current date and time
        int year = now.get(Calendar.YEAR);       // The current year
        for (int i = 1; i <= 5; i++ ) {
            year_com.getItems().add(Integer.toString(year--));
        }
        year_com.getSelectionModel().select(0); // первое значение
        minus_year.setText("3");

        // по умолчанию - месяц
        month_com.setItems(month_com_List);
        int month = now.get(Calendar.MONTH);
        month_com.getSelectionModel().select(month);
        minus_month.setText("1");
        plus_month.setText("1");
        check_month.setSelected(false);

        // по умолчанию - день
        day_com.setItems(day_com_List);
        int day = now.get(Calendar.DAY_OF_MONTH);
        day_com.getSelectionModel().select(day - 1);
        minus_day.setText("15");
        plus_day.setText("15");
        check_day.setSelected(true);

        // по умолчанию - источник
        source_code.setItems(source_code_List);
        source_code.getSelectionModel().select(0); // первое значение
        del_cache_file.setDisable(true);
        del_cache_file.setSelected(false);
        not_create_cache_file.setDisable(true);
        not_create_cache_file.setSelected(false);

        Calc_range();
    }

    // кнопка - Обновить график
    @FXML
    private void Calc_buttonActionPerformed() throws TransformerException {
        // Обновить график
        // расчет диапазонов и вывод данных
        Calc_range();
    }

    // кнопка - Ссылка на курсы НБУ
    @FXML
    private void button_kurs_nbuActionPerformed() {
        // Ссылка на курсы НБУ
        try {
            Desktop d = Desktop.getDesktop();
            d.browse(new URI("https://bank.gov.ua/control/uk/curmetal/currency/search/form/period"));
        }
        catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    // поле со списком - источник данных
    @FXML
    private void source_codeAction() {
        // блокируем кнопку если из сайта
        button_kurs_nbu.setDisable(false);
        del_cache_file.setDisable(true);
        not_create_cache_file.setDisable(true);
        if (source_code.getSelectionModel().getSelectedIndex() == 1) {
            button_kurs_nbu.setDisable(true);
            del_cache_file.setDisable(false);
            not_create_cache_file.setDisable(false);
        }
    }

    private void Calc_range() throws TransformerException {
        Calendar now = Calendar.getInstance();   // Gets the current date and time
        int year = now.get(Calendar.YEAR);       // The current year        
        int year_now = year;
        // расчитываем диапазон
        int mYear = (int) Main.getString_Float(minus_year.getText()) + 1;
        int m_is_sred_value_valut;
        int m_is_visible_points;

        // год
        LocalDate[] mDate1 = new LocalDate [mYear];
        LocalDate[] mDate2 = new LocalDate [mYear];

        year = year_now - mYear + 1;
        for (int i = 0; i <= mYear - 1; i++) {

            // день
            if (check_day.isSelected()) {

                int mMonthS = month_com.getSelectionModel().getSelectedIndex() + 1;
                int mDay = (int) Main.getString_Float(plus_day.getText()) + (int) Main.getString_Float(minus_day.getText()) + 1;
                int mDayS = day_com.getSelectionModel().getSelectedIndex();

                // стартовая дата        
                mDate1[i] = LocalDate.parse ("01." + String.format("%2s", mMonthS).replace(' ', '0') + "." + year , DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ) );
                // выходим на день
                if (mDayS > 0) {
                    mDate1[i] = mDate1[i].plusDays(mDayS);
                }
                // минус дней
                if ((int) Main.getString_Float(minus_day.getText()) > 0) {
                    mDate1[i] = mDate1[i].minusDays((int) Main.getString_Float(minus_day.getText()));
                }
                // плюс необходимое кол-во дней
                mDate2[i] = mDate1[i].plusDays(mDay);
            }
            // месяц
            else if (check_month.isSelected()) {

                int mMonth = (int) Main.getString_Float(plus_month.getText()) + (int) Main.getString_Float(minus_month.getText()) + 1;
                int mMonthS = month_com.getSelectionModel().getSelectedIndex() - (int) Main.getString_Float(minus_month.getText()) + 1;
                if (mMonthS < 0) {
                    mMonthS = 12 + mMonthS;
                    if (i == 0) { year = year - 1; }
                }

                mDate1[i] = LocalDate.parse ( "01." + String.format("%2s", mMonthS).replace(' ', '0') + "." + year , DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ) );
                // плюс необходимо кол-во месяцев
                mDate2[i] = mDate1[i].plusMonths(mMonth);
                // минус 1 день
                mDate2[i] = mDate2[i].minusDays(1);
            }
            // год
            else {
                mDate1[i] = LocalDate.parse ( "01.01." + year , DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ));
                mDate2[i] = LocalDate.parse ( "31.12." + year , DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ));
            }

            year++;
        }

        // добавляем в панель - график
        String mCurrCode = curr_code.getSelectionModel().getSelectedItem().substring(0, 3);
        String[][] mArray = null;

        int source_index = source_code.getSelectionModel().getSelectedIndex();
        if (source_index == 0) {
            mArray = getKursNbu(mCurrCode, mDate1, mDate2);
        } else if (source_index == 1) {
            mArray = getKursNbu_WEB(mCurrCode, mDate1, mDate2);
        }

        // считать среднее значение
        m_is_sred_value_valut = 0;
        if (sred_value_valut.isSelected()) { m_is_sred_value_valut = 1; }

        // показывать значения в точках
        m_is_visible_points = 0;
        if (visible_points.isSelected()) { m_is_visible_points = 1; }

        // рисуем график
        LineChartGraf(mArray, m_is_sred_value_valut, m_is_visible_points);
    }

    // Получить курс НБУ
    private String [][] getKursNbu(String mCurrCode, LocalDate [] mDate1, LocalDate [] mDate2)
    {
        String mPath = tec_kat_kurs + File.separator + mCurrCode;
        File file = null;
        String p_name_date = ""; String p_name_rate = ""; String p_name_kurs = "";
        double m_rate = 100;
        String [][] mArray = new String[2][];
        ArrayList<String> mArray_Date = new ArrayList<>();
        ArrayList<String> mArray_Kurs = new ArrayList<>();

        ////////////////////////////////////////////////////////////////////////////////////
        // https://bank.gov.ua/control/uk/curmetal/currency/search/form/period
        // проверяем существование каталога, елс его нет создаем его
        new File(mPath).mkdirs();
        // ищем файлы с расширением json
        File dir = new File(mPath);
        File[] matchingFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("json");
            }
        });
        // берем файл последний из списка
        for (File file_row:matchingFiles) {
            file = file_row;
        }

        if (file == null) {
            Main.MessageBoxError(mPath , "Не найден файл *.json в каталоге");
        }

        if (!file.exists()) {
            Main.MessageBoxError(mPath, "Не найден файл *.json в каталоге");
        }

        // json
        //Official hrivnya exchange rates.json - en
        //Офіційний курс гривні щодо іноземних валют.json - uk
        String m_file_name = file.getName();
        if (m_file_name.equals("Official hrivnya exchange rates.json")) { p_name_date = "Date"; p_name_rate = "Unit"; p_name_kurs = "Official hrivnya exchange rates, UAH"; }
        else if (m_file_name.equals("Офіційний курс гривні щодо іноземних валют.json")) { p_name_date = "Дата"; p_name_rate = "Кількість одиниць"; p_name_kurs = "Офіційний курс гривні, грн"; }

        if (file.isFile()) {
            try {
                List<String> listDate = new ArrayList<>();
                List<Double> listKurs = new ArrayList<>();
                JsonReader reader = new JsonReader(new FileReader(file.getPath()));
                reader.beginArray();
                while (reader.hasNext()) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        String name_conv = new String(name.getBytes(), StandardCharsets.UTF_8);
                        if (name_conv.equals(p_name_date)) {
                            listDate.add(reader.nextString());      // дата
                        } else if (name_conv.equals(p_name_rate)) {        // количество
                            m_rate = reader.nextDouble();
                        } else if (name_conv.equals(p_name_kurs)) {        // курс
                            listKurs.add(reader.nextDouble() / m_rate);
                        } else {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();
                }
                reader.endArray();
                reader.close();

                // после получения списков, заполняем массив нужными данными
                for (int i = 0; i < mDate1.length; i++) {
                    int num_list = 0;
                    for (String list : listDate) {
                        LocalDate list_date = getDateString(list);
                        if (list_date.compareTo(mDate1[i]) >= 0 && list_date.compareTo(mDate2[i]) <= 0) {
                            // преобразование DD.MM.YYYY в YYYYMMDD
                            list = list.substring(6, 10) + list.substring(3, 5) + list.substring(0, 2);
                            mArray_Date.add(list);
                            mArray_Kurs.add(listKurs.get(num_list).toString());
                        }
                        num_list++;
                    }
                }

                if (mArray_Date.size() == 0)
                {
                    Main.MessageBoxError("Курсы из файла \n" +  m_file_name + "\n не загружены, возможно изменилась структура", "");
                }

                // переносим в общий массив
                mArray[0] = mArray_Date.toArray(new String[0]);
                mArray[1] = mArray_Kurs.toArray(new String[0]);

            } catch (IOException e) {
                e.printStackTrace();
                Main.MessageBoxError(e.toString(), "");
            }
        }
        return mArray;
    }

    // Получить курс НБУ (Онлайн)
    private String [][] getKursNbu_WEB(String mCurrCode, LocalDate [] mDate1, LocalDate [] mDate2) throws TransformerException {
        String mPath = tec_kat_kurs + File.separator + mCurrCode;
        File file;
        String mPathXml;
        String [][] mArray = new String[2][];
        ArrayList<String> mArray_Date = new ArrayList<>();
        ArrayList<String> mArray_Kurs = new ArrayList<>();
        String p_periodStartTime;
        String p_periodEndTime;
        int p_currency_id = 0;
        //
        p_periodStartTime = mDate1[0].format(DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ));
        p_periodEndTime = mDate2[mDate2.length - 1].format(DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ));
        //
        switch (mCurrCode) {
            case "USD":
                p_currency_id = 169;
                break;
            case "EUR":
                p_currency_id = 196;
                break;
            case "GBP":
                p_currency_id = 163;
                break;
        }

        // действия, если папка не существует, создаем
        new File(mPath).mkdirs();

        mPathXml = mPath + File.separator + "search_results (" + p_periodStartTime + " - " + p_periodEndTime + ").xml";
        file = new File(mPathXml);

        // Удаляет Кеш файл
        if (del_cache_file.isSelected()) {
            file.delete();
            file = new File(mPathXml);
        }

        // не сохранять на диске
        if (not_create_cache_file.isSelected()) {
            file = new File("");
        }

        Document doc = null; // документ xml
        // Если нет файла взять его с сайта
        if (!file.exists()) {
            ////////////////////////////////////////////////////////////////////////////////////
            // чтение файла с НБУ
            // https://old.bank.gov.ua/control/uk/curmetal/currency/search?formType=searchPeriodForm&time_step=daily&currency=169&periodStartTime=01.11.2018&periodEndTime=02.11.2018&outer=xml&execute=%D0%92%D0%B8%D0%BA%D0%BE%D0%BD%D0%B0%D1%82%D0%B8
            try {
                URL xmlURL = new URL("https://old.bank.gov.ua/control/uk/curmetal/currency/search?formType=searchPeriodForm&time_step=daily&currency=" + p_currency_id + "&periodStartTime=" + p_periodStartTime + "&periodEndTime=" + p_periodEndTime + "&outer=xml&execute=%D0%92%D0%B8%D0%BA%D0%BE%D0%BD%D0%B0%D1%82%D0%B8");
                InputStream xml = xmlURL.openStream();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(xml);
                xml.close();
                // не сохранять на диске (выключено)
                if (!not_create_cache_file.isSelected()) {
                    // сохранение на локальном диске
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    Source source = new DOMSource(doc);
                    Result result = new StreamResult(new FileOutputStream(mPathXml));
                    transformer.transform(source, result);
                    // перечитать созданный файл
                    file = new File(mPathXml);
                }
            }
            catch (IOException | ParserConfigurationException | SAXException | TransformerConfigurationException e){
                e.printStackTrace();
                Main.MessageBoxError(e.toString(), "");
            }
        }

        try {
            List<String> listDate = new ArrayList<>();
            List<Double> listKurs = new ArrayList<>();
            LocalDate m_date_old = null;
            // чтение XML файла
            // не сохранять на диске (выключено)
            if (!not_create_cache_file.isSelected()) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(file);
            }
            assert doc != null;
            doc.getDocumentElement().normalize();
            NodeList nodeLst = doc.getElementsByTagName("currency");
            for (int s = 0; s < nodeLst.getLength(); s++) {
                org.w3c.dom.Node fstNode = nodeLst.item(s);
                if (fstNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element fstElmnt = (Element) fstNode;
                    Element message = (Element) fstElmnt.getElementsByTagName("date").item(0);
                    String date_text = message.getTextContent();
                    message = (Element) fstElmnt.getElementsByTagName("exchange_rate").item(0);
                    String kurs_text = (message.getTextContent().replace(",", "."));
                    message = (Element) fstElmnt.getElementsByTagName("number_of_units").item(0);
                    String rate_text = message.getTextContent();

                    LocalDate m_date = getDateString(date_text);
                    int kol_day = Period.between(mDate1[0], m_date).getDays();
                    // если в начале не хватает курсов дополняем
                    if (s == 0 && kol_day > 0) {
                        LocalDate m_date_temp = mDate1[0];
                        for (int k = 0; k < kol_day; k++) {
                            listDate.add(m_date_temp.format(DateTimeFormatter.ofPattern ( "dd.MM.yyyy" )));      // дата
                            listKurs.add((double) Float.parseFloat(kurs_text) / Float.parseFloat(rate_text));
                            m_date_temp = m_date_temp.plusDays(1);
                        }
                    }
                    // восстанавливаем курсы если есть пустые участки
                    if (s > 0) {
                        assert m_date_old != null;
                        kol_day = Period.between(m_date_old, m_date).getDays();
                        if (kol_day > 1) {
                            LocalDate m_date_temp = m_date_old;
                            for (int k = 0; k < kol_day - 1; k++) {
                                m_date_temp = m_date_temp.plusDays(1);
                                listDate.add(m_date_temp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));      // дата
                                listKurs.add((double) (Float.parseFloat(kurs_text) / Float.parseFloat(rate_text)));
                            }
                        }
                    }

                    listDate.add(date_text);      // дата
                    listKurs.add((double) (Float.parseFloat(kurs_text) / Float.parseFloat(rate_text)));
                    m_date_old = m_date;
                }
            }

            // после получения списков, заполняем массив нужными данными
            for (int i = 0; i < mDate1.length; i++) {
                int num_list = 0;
                for (String list : listDate) {
                    LocalDate list_date = getDateString(list);
                    if (list_date.compareTo(mDate1[i]) >= 0 && list_date.compareTo(mDate2[i]) <= 0) {
                        // преобразование DD.MM.YYYY в YYYYMMDD
                        list = list.substring(6, 10) + list.substring(3, 5) + list.substring(0, 2);
                        mArray_Date.add(list);
                        mArray_Kurs.add(listKurs.get(num_list).toString());
                    }
                    num_list++;
                }
            }

            if (mArray_Date.size() == 0) {
                Main.MessageBoxError("Курсы не загружены, возможно изменилась структура", "");
            }

            // переносим в общий массив
            mArray[0] = mArray_Date.toArray(new String[0]);
            mArray[1] = mArray_Kurs.toArray(new String[0]);

        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            Main.MessageBoxError(e.toString(), "");
        }

        return mArray;
    }

    private void LineChartGraf(String [][] mArray, int is_sred_value_valut, int is_visible_points) {
        if (mArray == null) { return; }

        // отчищаем график
        lchart.getData().clear();
        lchart.setAnimated(false);

        // Определяем годы
        int m_year_temp = Integer.parseInt(mArray[0][0].substring(0, 4));
        int m_year_etalon = m_year_temp;

        XYChart.Series series1 = new XYChart.Series<String,Number>();
        series1.setName(Integer.toString(m_year_temp));

        float [][] mArraySred = new float [1][1];
        float m_sred_value = 0;
        if (is_sred_value_valut == 1) {
            // определяем сколько годов
            int m_sred_num;
            int m_kol_year = Integer.parseInt(mArray[0][mArray[0].length - 1].substring(0, 4)) - Integer.parseInt(mArray[0][0].substring(0, 4)) + 1;
            mArraySred = new float [2][m_kol_year];
            int m_base_year = Integer.parseInt(mArray[0][0].substring(0, 4));
            // идем по годам
            for (int ii = 0; ii < m_kol_year; ii++) {
                // идем по всему массиву
                m_sred_value = 0;
                m_sred_num = 0;
                for (int iii = 0; iii < mArray[0].length; iii++) {
                    if (mArray[1][iii] != null) {
                        if (Integer.toString(m_base_year).equals(mArray[0][iii].substring(0, 4))) {
                            m_sred_value += Float.parseFloat(mArray[1][iii].replace(",", "."));
                            m_sred_num++;
                        }
                    }
                }
                if (m_sred_num == 0) { m_sred_num = 1; }
                m_sred_value = m_sred_value / m_sred_num;
                if (m_sred_value == 0) { m_sred_value = 1; }
                mArraySred[0][ii] = m_base_year;
                mArraySred[1][ii] = m_sred_value;
                // следующий год
                m_base_year++;
            }
        }

        float m_etalon_min = 0, m_etalon_max = 0, m_etalon_init = 0;
        for (int ii = 0; ii < mArray[0].length; ii++)
        {
            m_year_temp = Integer.parseInt(mArray[0][ii].substring(0, 4));
            // если год меняется, генерируем новый график
            if (m_year_etalon != m_year_temp) {
                // добавляем при каждом изменении, кроме первого
                if (ii > 0) {
                    lchart.getData().add(series1);
                }
                // переинициализация
                series1 = new XYChart.Series<String,Number>();
                series1.setName(Integer.toString(m_year_temp));
                m_year_etalon = m_year_temp;
            }
            // добавление значений
            if (mArray[1][ii] != null) {
                int m_day = Integer.parseInt(mArray[0][ii].substring(6, 8));
                int m_months = Integer.parseInt(mArray[0][ii].substring(4, 6));
                int m_year = Integer.parseInt(mArray[0][ii].substring(0, 4));

                // убираем высокосный, чтобы не было ошибок
                if (m_months == 2 && m_day == 29) {
                    m_day = 28;
                }

                String ValueDate = m_day + "." + m_months;
                float FloatData = Float.parseFloat(mArray[1][ii].replace(",", "."));

                if (is_sred_value_valut == 1) {
                    for (int iii = 0; iii < mArraySred[0].length; iii++) {
                        if (mArraySred[0][iii] == m_year) {
                            m_sred_value = mArraySred[1][iii];
                        }
                    }
                    series1.getData().add(new XYChart.Data<String, Number>(ValueDate, FloatData / m_sred_value));
                }
                else {
                    series1.getData().add(new XYChart.Data<String, Number>(ValueDate, FloatData));
                }

                // поиск мин. и макс. значения
                float m_value_data = FloatData;
                if (is_sred_value_valut == 1) m_value_data = FloatData / m_sred_value;
                if (m_etalon_init == 0) {
                    m_etalon_min = m_value_data;
                    m_etalon_max = m_value_data;
                    m_etalon_init = 1;
                }
                if (m_etalon_min > m_value_data) m_etalon_min = m_value_data;
                if (m_etalon_max < m_value_data) m_etalon_max = m_value_data;

            }
        }
        // последний год
        lchart.getData().add(series1);

        // ручное масштабирование
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(m_etalon_min - (m_etalon_max - m_etalon_min)/20);
        yAxis.setUpperBound(m_etalon_max + (m_etalon_max - m_etalon_min)/20);
        yAxis.setTickUnit((m_etalon_max - m_etalon_min)/20);

        // Всплывающие подсказки в узлах
        if (is_visible_points == 1) {
            int kol_charts = lchart.getData().size();
            for (int k = 0; k < kol_charts; k++) {
                ObservableList<XYChart.Data> dataList;
                dataList = ((XYChart.Series) lchart.getData().get(k)).getData();
                dataList.forEach(data -> {
                    Node node = data.getNode();
                    //Tooltip tooltip = new Tooltip('(' + data.getXValue().toString() + ';' + data.getYValue().toString() + ')');
                    Tooltip tooltip = new Tooltip(data.getYValue().toString());
                    Tooltip.install(node, tooltip);
                });
            }
        }
    }

    public boolean isDateValid(String m_date)
    {
        if (m_date.isEmpty()) { return false; }
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern ( "dd.MM.yyyy" );
            LocalDate ldate = LocalDate.parse ( m_date , f );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public LocalDate getDateString(String m_date)
    {
        if (!isDateValid(m_date)) { return null; }
        DateTimeFormatter f = DateTimeFormatter.ofPattern ( "dd.MM.yyyy" );
        return LocalDate.parse ( m_date , f );
    }

}
