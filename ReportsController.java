package org.binatel.bill.controllers;

import org.binatel.bill.controllers.util.DateUtil;
import org.binatel.bill.controllers.util.JsfUtil;
import org.binatel.bill.ejb.AccountingClientReportBeelineFacade;
import org.binatel.bill.entities.AccountingClientReportBeeline;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@ManagedBean(name = "reportsController")
@SessionScoped
public class ReportsController implements Serializable {

    @EJB
    private AccountingClientReportBeelineFacade accountingClientReportBeelineFacade;

    private List<AccountingClientReportBeeline> items = null;
    private AccountingClientReportBeeline beelineK0159report;

    private Date curDate;
    private Date selDate;

    private StreamedContent csvFile;

    private static final String CSV_SEPARATOR = ";";

    public ReportsController() {
    }

    private AccountingClientReportBeelineFacade getAccountingClientReportBeelineFacade() {
        return accountingClientReportBeelineFacade;
    }

    public List<AccountingClientReportBeeline> getItems() {
        if (items == null) {
            items = getAccountingClientReportBeelineFacade().findAll();
        }
        return items;
    }

    public AccountingClientReportBeeline getBeelineK0159report() {
        return beelineK0159report;
    }

    public void setBeelineK0159report(AccountingClientReportBeeline beelineK0159report) {
        this.beelineK0159report = beelineK0159report;
    }

    public Date getCurDate() {
        return curDate;
    }

    public void setCurDate(Date curDate) {
        this.curDate = curDate;
    }

    public Date getSelDate() {
        return selDate;
    }

    public void setSelDate(Date selDate) {
        this.selDate = selDate;
    }

    public StreamedContent getCsvFile() {
        return csvFile;
    }


    public String prepareK0159Report () {
        items = null;
        setCurDate(DateUtil.getCurrentDate());
        setSelDate(DateUtil.getCurrentDate());
        return "/reports/beelineK0159report";
    }

    public void changeDateListener () {
        setSelDate(getCurDate());
        JsfUtil.addInfoMessage(selDate.toString());
    }

    public List<AccountingClientReportBeeline> findItemsByDate (long lastDateTs) {
        return getAccountingClientReportBeelineFacade().findDataByDate(lastDateTs);
    }


   public void generateCsv () throws UnsupportedEncodingException {

       long lastMonthDate = DateUtil.getEndDayOfPreviousMonth(selDate);
       List<AccountingClientReportBeeline> dList = getAccountingClientReportBeelineFacade().findDataByDate(lastMonthDate);
       if (dList.isEmpty()) {
           JsfUtil.addErrorMessage("Для выбранной даты данные в таблице отсутствуют");
           return;
       }
       StringBuilder sb = new StringBuilder();
       List<String> strings = new ArrayList<>();
       for (AccountingClientReportBeeline a : dList) {

           strings.add(String.valueOf(a.getReportRowIndex()) + ";" + a.getReportDtBeeContract() + ";" + a.getReportClientContract() + ";" + a.getReportSfacturaNum() + ";" + a.getReportAktNum() + ";"
                                        + a.getReportSfacturaDate() + ";" + a.getReportPaymentDate() + ";" + String.valueOf(a.getReportCurrencyCode()) + ";"
                                        + String.valueOf(a.getReportVatCode()) + ";" + String.valueOf(a.getReportTrafficType()) + ";" + a.getReportServiceDate() + ";"
                                        + String.valueOf(a.getReportTrafficSumPayment()) + ";" + String.valueOf(a.getReportTrafficMinutes()) + ";"
                                        + String.valueOf(a.getReportRegionCode()) + ";" + String.valueOf(a.getReportVatInclude()) + System.getProperty("line.separator"));

       }

       for(String s: strings){
           sb.append(s);
       }

       ByteArrayInputStream stream = new ByteArrayInputStream( sb.toString().getBytes("UTF-8") );

       String docFormat = "csv";
       String fileName = "0TA_BIL_" + DateUtil.getYear(selDate) + "_" + DateUtil.getPreviousMonth(selDate) + "." + docFormat;
       csvFile = new DefaultStreamedContent(stream, "application/csv", fileName);
   }

}
