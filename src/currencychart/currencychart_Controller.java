package currencychart;

import com.google.gson.stream.JsonReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class currencychart_Controller {

    private ObservableList<String> curr_code_List = FXCollections.observableArrayList("USD-Доллар США", "EUR-Евро", "GBP-Фунт");
    private ObservableList<String> month_com_List = FXCollections.observableArrayList("01-Январь", "02-Февраль", "03-Март", "04-Апрель",
                                                                                "05-Май", "06-Июнь", "07-Июль", "08-Август",
                                                                                "09-Сентябрь", "10-Октябрь", "11-Ноябрь", "12-Декабрь");
    private ObservableList<String> day_com_List = FXCollections.observableArrayList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
                                                                                    "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                                                                                    "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
                                                                                    "31");
    private static String tec_kat = new File("").getAbsolutePath();
    private static String tec_kat_kurs = tec_kat + /*File.separator + "dist" +*/ File.separator + "kurs";

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
    @FXML private Button Calc_button;
    @FXML private Button button_kurs_nbu;
    @FXML private LineChart<String, Number> lchart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML
    private void initialize()
    {
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

        Calc_range();
    }

    // кнопка - Обновить график
    @FXML
    private void Calc_buttonActionPerformed() throws IOException {
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

    private void Calc_range() {
        Calendar now = Calendar.getInstance();   // Gets the current date and time
        int year = now.get(Calendar.YEAR);       // The current year        
        int year_now = year;
        // расчитываем диапазон
        int mYear = (int) Main.getString_Float(minus_year.getText()) + 1;
        int m_is_sred_value_valut;
        int m_is_visible_points;


        // год
        Date[] mDate1 = new Date [mYear];
        Date[] mDate2 = new Date [mYear];
        LocalDate ldate;
        LocalDateTime localDateTime;

        year = year_now - mYear + 1;
        for (int i = 0; i <= mYear - 1; i++) {

            // день
            if (check_day.isSelected()) {

                int mMonthS = month_com.getSelectionModel().getSelectedIndex() + 1;
                int mDay = (int) Main.getString_Float(plus_day.getText()) + (int) Main.getString_Float(minus_day.getText()) + 1;
                int mDayS = day_com.getSelectionModel().getSelectedIndex();

                // стартовая дата        
                ldate = LocalDate.parse ("01." + String.format("%2s", mMonthS).replace(' ', '0') + "." + year , DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ) );
                mDate1[i] = Date.from(ldate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                // выходим на день
                if (mDayS > 0) {
                    localDateTime = mDate1[i].toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    localDateTime = localDateTime.plusDays(mDayS);
                    mDate1[i] = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                }
                // минус дней
                if ((int) Main.getString_Float(minus_day.getText()) > 0) {
                    localDateTime = mDate1[i].toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    localDateTime = localDateTime.minusDays((int) Main.getString_Float(minus_day.getText()));
                    mDate1[i] = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                }
                // плюс необходимое кол-во дней
                localDateTime = mDate1[i].toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                localDateTime = localDateTime.plusDays(mDay);
                mDate2[i] = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            }
            // месяц
            else if (check_month.isSelected()) {

                int mMonth = (int) Main.getString_Float(plus_month.getText()) + (int) Main.getString_Float(minus_month.getText()) + 1;
                int mMonthS = month_com.getSelectionModel().getSelectedIndex() - (int) Main.getString_Float(minus_month.getText()) + 1;
                if (mMonthS < 0) {
                    mMonthS = 12 + mMonthS;
                    if (i == 0) { year = year - 1; }
                }

                ldate = LocalDate.parse ( "01." + String.format("%2s", mMonthS).replace(' ', '0') + "." + year , DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ) );
                mDate1[i] = Date.from(ldate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                // плюс необходимо кол-во месяцев
                localDateTime = mDate1[i].toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                localDateTime = localDateTime.plusMonths(mMonth);
                mDate2[i] = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                // минус 1 день
                localDateTime = mDate2[i].toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                localDateTime = localDateTime.minusDays(1);
                mDate2[i] = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            }
            // год
            else {
                ldate = LocalDate.parse ( "01.01." + year , DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ) );
                mDate1[i] = Date.from(ldate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                ldate = LocalDate.parse ( "31.12." + year , DateTimeFormatter.ofPattern ( "dd.MM.yyyy" ) );
                mDate2[i] = Date.from(ldate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }

            year++;
        }

        // добавляем в панель - график
        String mCurrCode = curr_code.getSelectionModel().getSelectedItem().substring(0, 3);
        String[][] mArray;
        mArray = getKursNbu(mCurrCode, mDate1, mDate2);

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
    private String [][] getKursNbu(String mCurrCode, Date [] mDate1, Date [] mDate2)
    {
        String mPath = tec_kat_kurs + File.separator + mCurrCode;
        LocalDate localDate;
        String tDate;
        String tDate_json;
        LocalDateTime localDateTime;
        File file = null;
        String p_name_date = ""; String p_name_rate = ""; String p_name_kurs = "";
        double m_rate = 100;
        int days = 0;
        int daysp = 0;
        Date mDate;
        String [][] mArray = new String[2][];
        ArrayList<String> mArray_Date = new ArrayList<String>();
        ArrayList<String> mArray_Kurs = new ArrayList<String>();

        // определяем размер массива
        //for (int i = 0; i < mDate1.length; i++) {
        //    days = days + (int)( (mDate2[i].getTime() - mDate1[i].getTime()) / (1000 * 60 * 60 * 24)) + 1;
        //}
        //mArray = new String [2][days];

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
        if (m_file_name.equals("Official hrivnya exchange rates.json")) { p_name_date = "Date"; p_name_rate = "Unit"; p_name_kurs = "Official hrivnya exchange rates"; }
        else if (m_file_name.equals("Офіційний курс гривні щодо іноземних валют.json")) { p_name_date = "Дата"; p_name_rate = "Кількість одиниць"; p_name_kurs = "Офіційний курс гривні"; }

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
                        String name_conv = new String(name.getBytes(), "UTF-8");
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
                Integer num = 0;
                for (int i = 0; i < mDate1.length; i++) {
                    DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                    Integer num_list = 0;
                    for (String list : listDate) {
                        if (format.parse(list).compareTo(mDate1[i]) >= 0 && format.parse(list).compareTo(mDate2[i]) <= 0) {
                            // преобразование DD.MM.YYYY в YYYYMMDD
                            list = list.substring(6, 10) + list.substring(3, 5) + list.substring(0, 2);
                            mArray_Date.add(list);
                            mArray_Kurs.add(listKurs.get(num_list).toString());
                            num++;
                        }
                        num_list++;
                    }
                }

                if (mArray_Date.size() == 0)
                {
                    Main.MessageBoxError("Курсы из файла \n" +  m_file_name + "\n не загружены, возможно изменилась структура", "");
                }

                // переносим в общий массив
                mArray[0] = mArray_Date.toArray(new String[mArray_Date.size()]);
                mArray[1] = mArray_Kurs.toArray(new String[mArray_Kurs.size()]);

            } catch (IOException | ParseException e) {
                e.printStackTrace();
                Main.MessageBoxError(e.toString(), "");
            }
        }
        return mArray;
    }

    private void LineChartGraf(String [][] mArray, int is_sred_value_valut, int is_visible_points) {
        if (mArray == null) { return; }

        // отчищаем график
        lchart.getData().clear();
        lchart.setAnimated(false);

        // Определяем годы
        int m_year_end = Integer.parseInt(mArray[0][mArray[0].length - 1].substring(0, 4));
        int m_year_temp = Integer.parseInt(mArray[0][0].substring(0, 4));
        int m_year_etalon = m_year_temp;

        XYChart.Series series1 = new XYChart.Series<String,Number>();
        series1.setName(Integer.toString(m_year_temp));

        float [][] mArraySred = new float [1][1];
        float m_sred_value = 0;
        if (is_sred_value_valut == 1) {
            // определяем сколько годов
            int m_sred_num = 0;
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
            ObservableList<XYChart.Data> dataList = ((XYChart.Series) lchart.getData().get(0)).getData();
            dataList.forEach(data -> {
                Node node = data.getNode();
                Tooltip tooltip = new Tooltip('(' + data.getXValue().toString() + ';' + data.getYValue().toString() + ')');
                Tooltip.install(node, tooltip);
            });
        }
    }

    public boolean isDateValid(String m_date)
    {
        if (m_date.isEmpty() == true) { return false; }
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern ( "dd.MM.yyyy" );
            LocalDate ldate = LocalDate.parse ( m_date , f );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Date getDateString(String m_date)
    {
        if (isDateValid(m_date) == false) { return null; }
        DateTimeFormatter f = DateTimeFormatter.ofPattern ( "dd.MM.yyyy" );
        LocalDate ldate = LocalDate.parse ( m_date , f );
        Date date = Date.from(ldate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return date;
    }

    public Integer getIntegerDate(String m_date)
    {
        if (isDateValid(m_date) == false) { return null; }
        DateTimeFormatter f = DateTimeFormatter.ofPattern ( "dd.MM.yyyy" );
        LocalDate ldate = LocalDate.parse ( m_date , f );
        Date date = Date.from(ldate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Integer dateint = (int) date.getTime()/1000;
        return dateint;
    }

}
