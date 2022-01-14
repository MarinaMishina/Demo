package org.binatel.bill.controllers;

import org.binatel.bill.controllers.util.AddContractBalanceBean;
import org.binatel.bill.controllers.util.DateUtil;
import org.binatel.bill.ejb.*;
import org.binatel.bill.entities.*;
import org.binatel.bill.controllers.util.JsfUtil;
import org.binatel.bill.controllers.util.JsfUtil.PersistAction;
import org.binatel.bill.controllers.util.SaveDataInRedisBean;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.ActionEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


@ManagedBean(name = "clientAccountingDocsController")
@SessionScoped
public class ClientAccountingDocsController implements Serializable {

    @EJB
    private org.binatel.bill.ejb.ClientAccountingDocsFacade ejbFacade;

    @EJB
    private ContractClientFacade contractFacade;

    @EJB
    private CompanyClientFacade companyClientFacade;
    private List<CompanyClient> edoClients;

    @EJB
    private ClientAccountingDocsDataFacade docsDataFacade;
    private ClientAccountingDocsData docsData;

    @EJB
    private AccountingData4ExportTo1CFacade accountingData4ExportTo1CFacade;


    private List<ClientAccountingDocs> edoItems = null;
    private List<ClientAccountingDocs> selectedItems = null;

    private ClientAccountingDocs selected;

    private List<ContractClient> contracts;

    private Date curDate;

    @ManagedProperty("#{companyClientController}")

    private CompanyClientController companyClientController;
    private CompanyClient companyClient;

    private StreamedContent pdfFile;
    private StreamedContent xmlFile;

    // Константы для заполнения счета-фактуры в формате XML
    private static final String FILE_NAME_PART_1 = "ON_NSCHFDOPPR_";
    private static final String ID_EDO = "2BM";
    private static final String ID_SENDER_DIADOC = "201505260306237557139";
    private static final String VERS_PROG = "Diadoc 1.0";
    private static final String VERS_FORM = "5.01";
    
    private static final String SERVICE_NAME = "Услуги телефонной связи";
    private static final String OKEI = "362";
    private static final String OKEI_NAME = "мес";
    private static final String NAL_STAVKA = "20%";
    private static final String AKCIZ = "без акциза";

    public ClientAccountingDocsController() {
    }

    public void setCompanyClientController(CompanyClientController companyClientController) {
        this.companyClientController = companyClientController;
    }

    public CompanyClient getCompanyClient() {
        if (companyClientController != null) {
            companyClient = companyClientController.getSelected();
        }
        return companyClient;
    }

    public void setCompanyClient(CompanyClient companyClient) {
        this.companyClient = companyClient;
    }

    private ClientAccountingDocsFacade getFacade() {
        return ejbFacade;
    }

    private ContractClientFacade getContractFacade() {
        return contractFacade;
    }

    private CompanyClientFacade getCompanyClientFacade() {
        return companyClientFacade;
    }

    private ClientAccountingDocsDataFacade getDocsDataFacade() {
        return docsDataFacade;
    }

    public ClientAccountingDocs getSelected() {
        return selected;
    }

    private AccountingData4ExportTo1CFacade getAccountingData4ExportTo1CFacade() {
        return accountingData4ExportTo1CFacade;
    }

    public void setSelected(ClientAccountingDocs selected) {
        this.selected = selected;
    }

    public ClientAccountingDocsData getDocsData() {
        return docsData;
    }

    public void setDocsData(ClientAccountingDocsData docsData) {
        this.docsData = docsData;
    }

    public List<ContractClient> getContracts() {
        return contracts;
    }

    public void setContracts(List<ContractClient> contracts) {
        this.contracts = contracts;
    }

    public List<ClientAccountingDocs> getSelectedItems() {
        long endDate = DateUtil.getEndDayOfPreviousMonth(getCurDate());
        selectedItems = getFacade().findByContractAndPeriod(contracts, endDate);
        return selectedItems;
    }


    public List<ClientAccountingDocs> getEdoItems() {
        long endDate = DateUtil.getEndDayOfPreviousMonth(getCurDate());
        List <CompanyClient> clientsList = getCompanyClientFacade().findClientsWithEdo((short) 0);
        List <ContractClient> contractList = findContractWithEdoClients(clientsList);
        edoItems = getFacade().findByContractAndPeriod(contractList, endDate);
        return edoItems;
    }

    private List<ContractClient> findContractWithEdoClients (List<CompanyClient> clientList) {
        if (clientList == null) {
            JsfUtil.addErrorMessage("Клиентов с ЭДО не найдено");
            return null;
        }
        List<ContractClient> edoContracts = new ArrayList<>();
        for (CompanyClient a: clientList) {
             List<ContractClient> tempList = getContractFacade().getContractsByCompany(a);
                for (ContractClient contract: tempList) {
                    edoContracts.add(contract);
                }
        }
        return edoContracts;
    }

    public Date getCurDate() {
        return curDate;
    }

    public void setCurDate(Date curDate) {
        this.curDate = curDate;
    }

    public StreamedContent getPdfFile() {
        return pdfFile;
    }

    public StreamedContent getXmlFile() {
        return xmlFile;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    public String prepareList () {
        contracts = contractFacade.getContractsByCompany(getCompanyClient());
        setCurDate(DateUtil.getCurrentDate());
        selected = null;
        selectedItems = null;
        return "/clientDocs/ViewDocs";
    }

    public String prepareAllEdoList () {
        setCurDate(DateUtil.getCurrentDate());
        selected = null;
        selectedItems = null;
        return "/tools/allEdoDocs";
    }

    public String convertLongToStrDate(long itemLong) {
        String strDate = DateUtil.convertLongToShortFmtDate(itemLong);
        return strDate;
    }


   public void writeDocDataToStream (ActionEvent event) {
       ClientAccountingDocs selected = (ClientAccountingDocs) event.getComponent().getAttributes().get("selDoc");
       setSelected(selected);

       docsData = getDocsDataFacade().find(selected.getDocDataId());
       byte[] bytes = docsData.getDocData();
       InputStream is = new ByteArrayInputStream(bytes);
       String docFormat = "pdf";
       String fileName = getSelected().getYear() + "_" + getSelected().getMonth() + "_" + getSelected().getDocType().getType() + "_" + getSelected().getDocNumber() + "." + docFormat;
       pdfFile = new DefaultStreamedContent(is, "application/pdf", fileName);
   }


   public boolean getSeller (String docNumber) {
       if (docNumber.contains("0TA#")) {
           return true;
       }
       return false;
   }

   public String getStringMonth (String monthNumber) {

        HashMap<String, String> monthMap = new HashMap<>();
        monthMap.put("01", "Январь");
        monthMap.put("02", "Февраль");
        monthMap.put("03", "Март");
        monthMap.put("04", "Апрель");
        monthMap.put("05", "Май");
        monthMap.put("06", "Июнь");
        monthMap.put("07", "Июль");
        monthMap.put("08", "Август");
        monthMap.put("09", "Сентябрь");
        monthMap.put("10", "Октябрь");
        monthMap.put("11", "Ноябрь");
        monthMap.put("12", "Декабрь");

        return monthMap.get(monthNumber);
   }

   public void createXmlFaktura (ActionEvent event) throws Exception {
       ClientAccountingDocs selected = (ClientAccountingDocs) event.getComponent().getAttributes().get("selDocXml");

       DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
       DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
       Document document = documentBuilder.newDocument();

       //Данные для заполнения
       String name = selected.getContractId().getCompanyId().getName();
       String inn = selected.getContractId().getCompanyId().getInn();
       String kpp = selected.getContractId().getCompanyId().getKpp();
       String edoIndex = selected.getContractId().getCompanyId().getEdoAddrDataId().getIndex();
       String edoRegion = selected.getContractId().getCompanyId().getEdoAddrDataId().getRegion();
       String edoArea = selected.getContractId().getCompanyId().getEdoAddrDataId().getArea();
       String edoCity = selected.getContractId().getCompanyId().getEdoAddrDataId().getCity();
       String edoLocality = selected.getContractId().getCompanyId().getEdoAddrDataId().getLocality();
       String edoStreet = selected.getContractId().getCompanyId().getEdoAddrDataId().getStreet();
       String edoHouse = selected.getContractId().getCompanyId().getEdoAddrDataId().getHouse();
       String edoBuilding = selected.getContractId().getCompanyId().getEdoAddrDataId().getBuilding();
       String edoOffice = selected.getContractId().getCompanyId().getEdoAddrDataId().getOffice();
       String edoId = selected.getContractId().getCompanyId().getEdoId();
       String edoDate = DateUtil.getDateForEdo();
       String edoTime = DateUtil.getTimeForEdo();
       String docNumber = selected.getDocNumber();
       String docDate = DateUtil.convertLongToShortFmtDate(selected.getDateTs());
       String edoDateForSchFactura = DateUtil.getDateSchFacturaForEdo();
       String edoDocPeriod = selected.getMonth();
       String edoDocYear = String.valueOf(selected.getYear());
       //TODO
       // Написать обработчик исключения!!!
       AccountingData4ExportTo1C edo1CObj = accountingData4ExportTo1CFacade.findByContractIdAndDocNumber(selected.getContractId(), docNumber);
       double docSumWoVat = Math.round(edo1CObj.getDocSumWoVat()*100.0)/100.0;
       short svatPercent = edo1CObj.getVatPercent();
       double docSum = Math.round(docSumWoVat*1.2*100.0)/100.0;
       double dnalog = Math.round((docSum - docSumWoVat)*100.0)/100.0;
       String edoDocSumWoVat = String.valueOf(docSumWoVat);
       String vatPercent = String.valueOf(svatPercent);
       String edoDocSumWithVat = String.valueOf(docSum);
       String nalog = String.valueOf(dnalog);

       // Получить продавца
       boolean sellerFlag = getSeller(selected.getDocNumber());

       // Файл
       Element root = document.createElement("Файл");
       document.appendChild(root);

       // Файл attributes
       Attr attr_0_1 = document.createAttribute("ИдФайл");
       attr_0_1.setValue("ON_NSCHFDOPPR_" + edoId + "_2BM-7702798640-770201001-201505260306237557139_" + edoDate);
       root.setAttributeNode(attr_0_1);

       Attr attr_0_2 = document.createAttribute("ВерсФорм");
       attr_0_2.setValue("5.01");
       root.setAttributeNode(attr_0_2);

       Attr attr_0_3 = document.createAttribute("ВерсПрог");
       attr_0_3.setValue("Diadoc 1.0");
       root.setAttributeNode(attr_0_3);

       // СвУчДокОбор
       Element svUchDocObor = document.createElement("СвУчДокОбор");
       root.appendChild(svUchDocObor);

       // СвУчДокОбор attributes
       Attr attr_1_1 = document.createAttribute("ИдОтпр");
       attr_1_1.setValue("2BM-7702798640-770201001-201505260306237557139");
       svUchDocObor.setAttributeNode(attr_1_1);

       Attr attr_1_2 = document.createAttribute("ИдПол");
       attr_1_2.setValue(edoId);
       //attr_1_2.setValue("2BM-7713609655-771301001-201511031244001516672");
       svUchDocObor.setAttributeNode(attr_1_2);

       // СвОЭДОтпр
       Element svEdoOtpr = document.createElement("СвОЭДОтпр");
       svUchDocObor.appendChild(svEdoOtpr);

       // СвОЭДОтпр attributes
       Attr attr_1_2_1 = document.createAttribute("ИННЮЛ");
       attr_1_2_1.setValue("6663003127");
       svEdoOtpr.setAttributeNode(attr_1_2_1);

       Attr attr_1_2_2 = document.createAttribute("ИдЭДО");
       attr_1_2_2.setValue("2BM");
       svEdoOtpr.setAttributeNode(attr_1_2_2);

       Attr attr_1_2_3 = document.createAttribute("НаимОрг");
       attr_1_2_3.setValue("АО &quot;ПФ &quot;СКБ Контур&quot;");
       svEdoOtpr.setAttributeNode(attr_1_2_3);

       // Документ
       Element doc = document.createElement("Документ");
       root.appendChild(doc);

       // Документ attributes
       Attr attr_2_1 = document.createAttribute("КНД");
       attr_2_1.setValue("1115131");
       doc.setAttributeNode(attr_2_1);

       Attr attr_2_2 = document.createAttribute("ВремИнфПр");
       attr_2_2.setValue(edoTime);
       //attr_2_2.setValue("15.12.59");
       doc.setAttributeNode(attr_2_2);

       Attr attr_2_3 = document.createAttribute("ДатаИнфПр");
       attr_2_3.setValue(edoDateForSchFactura);
       doc.setAttributeNode(attr_2_3);

       Attr attr_2_4 = document.createAttribute("НаимЭконСубСост");
       if (sellerFlag) {
           attr_2_4.setValue(VIMPELCOM);
       } else {
           attr_2_4.setValue(CIFRA_TELECOM);
       }
       doc.setAttributeNode(attr_2_4);

       Attr attr_2_5 = document.createAttribute("Функция");
       attr_2_5.setValue("СЧФ");
       doc.setAttributeNode(attr_2_5);

       // СвСчФакт
       Element svScFact = document.createElement("СвСчФакт");
       doc.appendChild(svScFact);

       // СвСчФакт attributes
       Attr attr_2_1_1 = document.createAttribute("НомерСчФ");
       attr_2_1_1.setValue(docNumber);
       svScFact.setAttributeNode(attr_2_1_1);

       Attr attr_2_1_2 = document.createAttribute("ДатаСчФ");
       attr_2_1_2.setValue(docDate);
       svScFact.setAttributeNode(attr_2_1_2);

       Attr attr_2_1_3 = document.createAttribute("КодОКВ");
       attr_2_1_3.setValue("643");
       svScFact.setAttributeNode(attr_2_1_3);

       // СвПрод
       Element svProd = document.createElement("СвПрод");
       svScFact.appendChild(svProd);

       // ИдСв
       Element idSv = document.createElement("ИдСв");
       svProd.appendChild(idSv);

       // СвЮЛУч
       Element svUlUch = document.createElement("СвЮЛУч");
       idSv.appendChild(svUlUch);

       // СвЮЛУч attributes
       Attr attr_2_4_1 = document.createAttribute("НаимОрг");
       if (sellerFlag) {
           attr_2_4_1.setValue(VIMPELCOM);
       }
       else {
           attr_2_4_1.setValue(CIFRA_TELECOM);
       }
       svUlUch.setAttributeNode(attr_2_4_1);

       Attr attr_2_4_2 = document.createAttribute("ИННЮЛ");
       if(sellerFlag) {
           attr_2_4_2.setValue(VIMPELCOM_INN);
       }
       else {
           attr_2_4_2.setValue(CT_INN);
       }
       svUlUch.setAttributeNode(attr_2_4_2);

       Attr attr_2_4_3 = document.createAttribute("КПП");
       if(sellerFlag) {
           attr_2_4_3.setValue(VIMPELCOM_KPP);
       }
       else {
           attr_2_4_3.setValue(CT_KPP);
       }
       svUlUch.setAttributeNode(attr_2_4_3);

       // Адрес
       Element address = document.createElement("Адрес");
       svProd.appendChild(address);

       // АдрРФ
       Element adrRF = document.createElement("АдрРФ");
       address.appendChild(adrRF);

       // АдрРФ attributes
       Attr attr_2_5_1 = document.createAttribute("КодРегион");
       attr_2_5_1.setValue("77");
       adrRF.setAttributeNode(attr_2_5_1);

       Attr attr_2_5_2 = document.createAttribute("Индекс");
       if (sellerFlag) {
           attr_2_5_2.setValue(VIMPELCOM_ADDR_INDEX);
       } else
       {
           attr_2_5_2.setValue(CT_ADDR_INDEX);
       }
       adrRF.setAttributeNode(attr_2_5_2);

       Attr attr_2_5_3 = document.createAttribute("Улица");
       if (sellerFlag) {
           attr_2_5_3.setValue(VIMPELCOM_ADDR_ULICA);
       }
       else {
           attr_2_5_3.setValue(CT_ADDR_ULICA);
       }
       adrRF.setAttributeNode(attr_2_5_3);

       Attr attr_2_5_4 = document.createAttribute("Дом");
       if (sellerFlag) {
           attr_2_5_4.setValue(VIMPELCOM_ADDR_DOM);
       }
       else {
           attr_2_5_4.setValue(CT_ADDR_DOM);
       }
       adrRF.setAttributeNode(attr_2_5_4);

       if (sellerFlag) {
           Attr attr_2_5_5 = document.createAttribute("Корпус");
           attr_2_5_5.setValue(VIMPELCOM_ADDR_KORPUS);
           adrRF.setAttributeNode(attr_2_5_5);
       }
       else {
           Attr attr_2_5_5 = document.createAttribute("Кварт");
           attr_2_5_5.setValue(CT_ADDR_OFFICE);
           adrRF.setAttributeNode(attr_2_5_5);
       }

       // СвПокуп
       Element svPokup = document.createElement("СвПокуп");
       svScFact.appendChild(svPokup);

       // ИдСв
       Element idSvPokup = document.createElement("ИдСв");
       svPokup.appendChild(idSvPokup);

       // СвЮЛУч
       Element svUlUchPokup = document.createElement("СвЮЛУч");
       idSvPokup.appendChild(svUlUchPokup);

       // СвЮЛУч attributes
       Attr attr_2_6_1 = document.createAttribute("НаимОрг");
       attr_2_6_1.setValue(name);
       svUlUchPokup.setAttributeNode(attr_2_6_1);

       Attr attr_2_6_2 = document.createAttribute("ИННЮЛ");
       attr_2_6_2.setValue(inn);
       svUlUchPokup.setAttributeNode(attr_2_6_2);

       Attr attr_2_6_3 = document.createAttribute("КПП");
       attr_2_6_3.setValue(kpp);
       svUlUchPokup.setAttributeNode(attr_2_6_3);

       // Адрес
       Element addressPokup = document.createElement("Адрес");
       svPokup.appendChild(addressPokup);

       // АдрРФ
       Element addressRF = document.createElement("АдрРФ");
       addressPokup.appendChild(addressRF);

       // АдрРФ attributes
       Attr attr_2_7_1 = document.createAttribute("КодРегион");
       attr_2_7_1.setValue(edoRegion);
       addressRF.setAttributeNode(attr_2_7_1);

       Attr attr_2_7_2 = document.createAttribute("Индекс");
       attr_2_7_2.setValue(edoIndex);
       addressRF.setAttributeNode(attr_2_7_2);

       if (!edoArea.equals("")) {
           Attr attr_2_7_01 = document.createAttribute("Район");
           attr_2_7_01.setValue(edoArea);
           addressRF.setAttributeNode(attr_2_7_01);
       }

       if (!edoCity.equals("")) {
           Attr attr_2_7_02 = document.createAttribute("Город");
           attr_2_7_02.setValue(edoCity);
           addressRF.setAttributeNode(attr_2_7_02);
       }

       if (!edoLocality.equals("")) {
           Attr attr_2_7_03 = document.createAttribute("НаселПункт");
           attr_2_7_03.setValue(edoLocality);
           addressRF.setAttributeNode(attr_2_7_03);
       }

       Attr attr_2_7_3 = document.createAttribute("Улица");
       attr_2_7_3.setValue(edoStreet);
       addressRF.setAttributeNode(attr_2_7_3);

       Attr attr_2_7_4 = document.createAttribute("Дом");
       attr_2_7_4.setValue(edoHouse);
       addressRF.setAttributeNode(attr_2_7_4);

       if (!edoBuilding.equals("")) {
           Attr attr_2_7_04 = document.createAttribute("Корпус");
           attr_2_7_04.setValue(edoBuilding);
           addressRF.setAttributeNode(attr_2_7_04);
       }

       if (!edoOffice.equals("")) {
           Attr attr_2_7_5 = document.createAttribute("Кварт");
           attr_2_7_5.setValue(edoOffice);
           addressRF.setAttributeNode(attr_2_7_5);
       }

       // ДопСвФХЖ1
       Element dopSvFHJ1 = document.createElement("ДопСвФХЖ1");
       svScFact.appendChild(dopSvFHJ1);

       // ДопСвФХЖ1 attributes
       Attr attr_1_3_1 = document.createAttribute("НаимОКВ");
       attr_1_3_1.setValue("Российский рубль");
       dopSvFHJ1.setAttributeNode(attr_1_3_1);

       // ДокПодтвОтгр
       Element docOtgruz = document.createElement("ДокПодтвОтгр");
       svScFact.appendChild(docOtgruz);

       // ДокПодтвОтгр attributes
       Attr attr_1_4_1 = document.createAttribute("НаимДокОтгр");
       attr_1_4_1.setValue("№ п/п 1");
       docOtgruz.setAttributeNode(attr_1_4_1);

       Attr attr_1_4_2 = document.createAttribute("НомДокОтгр");
       attr_1_4_2.setValue(docNumber);
       docOtgruz.setAttributeNode(attr_1_4_2);

       Attr attr_1_4_3 = document.createAttribute("ДатаДокОтгр");
       attr_1_4_3.setValue(docDate);
       docOtgruz.setAttributeNode(attr_1_4_3);

       //ИнфПолФХЖ1
       Element infPolFHJ1 = document.createElement("ИнфПолФХЖ1");
       svScFact.appendChild(infPolFHJ1);

       //ТекстИнф
       Element textInf = document.createElement("ТекстИнф");
       infPolFHJ1.appendChild(textInf);

       //ТекстИнф attributes
       Attr attr_2_8_1 = document.createAttribute("Идентиф");
       attr_2_8_1.setValue("Договор");
       textInf.setAttributeNode(attr_2_8_1);

       Attr attr_2_8_2 = document.createAttribute("Значен");
       attr_2_8_2.setValue("№" + selected.getContractId().getNumber());
       textInf.setAttributeNode(attr_2_8_2);

       //ТекстИнф_2
       Element textInf_2 = document.createElement("ТекстИнф");
       infPolFHJ1.appendChild(textInf_2);

       //ТекстИнф_2 attributes
       Attr attr_2_9_1 = document.createAttribute("Идентиф");
       attr_2_9_1.setValue("Период");
       textInf_2.setAttributeNode(attr_2_9_1);

       Attr attr_2_9_2 = document.createAttribute("Значен");
       attr_2_9_2.setValue(getStringMonth(edoDocPeriod) + " " + edoDocYear);
       textInf_2.setAttributeNode(attr_2_9_2);

       //ТаблСчФакт
       Element tabScFact = document.createElement("ТаблСчФакт");
       doc.appendChild(tabScFact);

       //СведТов
       Element svedTov = document.createElement("СведТов");
       tabScFact.appendChild(svedTov);

       //СведТов attributes
       Attr attr_3_1_1 = document.createAttribute("НомСтр");
       attr_3_1_1.setValue("1");
       svedTov.setAttributeNode(attr_3_1_1);

       Attr attr_3_1_2 = document.createAttribute("НаимТов");
       if (sellerFlag) {
           attr_3_1_2.setValue("Услуги телефонной связи");
       }
       else {
           attr_3_1_2.setValue("Услуги связи");
       }
       svedTov.setAttributeNode(attr_3_1_2);

       Attr attr_3_1_3 = document.createAttribute("ОКЕИ_Тов");
       attr_3_1_3.setValue("362");
       svedTov.setAttributeNode(attr_3_1_3);

       Attr attr_3_1_4 = document.createAttribute("КолТов");
       attr_3_1_4.setValue("1");
       svedTov.setAttributeNode(attr_3_1_4);

       Attr attr_3_1_5 = document.createAttribute("ЦенаТов");
       attr_3_1_5.setValue(edoDocSumWoVat);
       svedTov.setAttributeNode(attr_3_1_5);

       Attr attr_3_1_6 = document.createAttribute("СтТовБезНДС");
       attr_3_1_6.setValue(edoDocSumWoVat);
       svedTov.setAttributeNode(attr_3_1_6);

       Attr attr_3_1_7 = document.createAttribute("НалСт");
       attr_3_1_7.setValue(vatPercent + "%");
       svedTov.setAttributeNode(attr_3_1_7);

       Attr attr_3_1_8 = document.createAttribute("СтТовУчНал");
       attr_3_1_8.setValue(edoDocSumWithVat);
       svedTov.setAttributeNode(attr_3_1_8);

       //Акциз
       Element akciz = document.createElement("Акциз");
       svedTov.appendChild(akciz);

       //БезАкциз
       Element bezAkciz = document.createElement("БезАкциз");
       akciz.appendChild(bezAkciz);
       bezAkciz.setTextContent("без акциза");

       //СумНал
       Element sumNal = document.createElement("СумНал");
       svedTov.appendChild(sumNal);

       //СумНал
       Element sumNalText = document.createElement("СумНал");
       sumNal.appendChild(sumNalText);
       sumNalText.setTextContent(nalog);

       //ДопСведТов
       Element dopSvedTov = document.createElement("ДопСведТов");
       svedTov.appendChild(dopSvedTov);

       // ДопСведТов attributes
       Attr attr_2_10_1 = document.createAttribute("НаимЕдИзм");
       attr_2_10_1.setValue("мес");
       dopSvedTov.setAttributeNode(attr_2_10_1);

       //ВсегоОпл
       Element vsegoOpl = document.createElement("ВсегоОпл");
       tabScFact.appendChild(vsegoOpl);

       //ВсегоОпл attributes
       Attr attr_3_2_1 = document.createAttribute("СтТовБезНДСВсего");
       attr_3_2_1.setValue(edoDocSumWoVat);
       vsegoOpl.setAttributeNode(attr_3_2_1);

       Attr attr_3_2_2 = document.createAttribute("СтТовУчНалВсего");
       attr_3_2_2.setValue(edoDocSumWithVat);
       vsegoOpl.setAttributeNode(attr_3_2_2);

       //СумНалВсего
       Element sumNalVsego = document.createElement("СумНалВсего");
       vsegoOpl.appendChild(sumNalVsego);

       //СумНал
       Element sumNal_2 = document.createElement("СумНал");
       sumNalVsego.appendChild(sumNal_2);
       sumNal_2.setTextContent(nalog);

       //Подписант
       Element podpisant = document.createElement("Подписант");
       doc.appendChild(podpisant);

       //Подписант attributes
       Attr attr_2_11_1 = document.createAttribute("ОснПолн");
       attr_2_11_1.setValue("Должностные обязанности");
       podpisant.setAttributeNode(attr_2_11_1);

       Attr attr_2_11_2 = document.createAttribute("ОблПолн");
       attr_2_11_2.setValue("4");
       podpisant.setAttributeNode(attr_2_11_2);

       Attr attr_2_11_3 = document.createAttribute("Статус");
       attr_2_11_3.setValue("1");
       podpisant.setAttributeNode(attr_2_11_3);

       //ЮЛ
       Element Ul = document.createElement("ЮЛ");
       podpisant.appendChild(Ul);

       //ЮЛ attributes
       Attr attr_3_3_1 = document.createAttribute("ИННЮЛ");
       attr_3_3_1.setValue(CT_INN);
       Ul.setAttributeNode(attr_3_3_1);

       Attr attr_3_3_2 = document.createAttribute("Должн");
       attr_3_3_2.setValue("Генеральный директор");
       Ul.setAttributeNode(attr_3_3_2);

       Attr attr_3_3_3 = document.createAttribute("НаимОрг");
       attr_3_3_3.setValue("ООО &quot;ЦИФРА-ТЕЛЕКОМ&quot;");
       Ul.setAttributeNode(attr_3_3_3);

       //ФИО
       Element fio = document.createElement("ФИО");
       Ul.appendChild(fio);

       //ФИО attributes
       Attr attr_4_1_1 = document.createAttribute("Фамилия");
       attr_4_1_1.setValue("Сеньков");
       fio.setAttributeNode(attr_4_1_1);

       Attr attr_4_1_2 = document.createAttribute("Имя");
       attr_4_1_2.setValue("Григорий");
       fio.setAttributeNode(attr_4_1_2);

       Attr attr_4_1_3 = document.createAttribute("Отчество");
       attr_4_1_3.setValue("Валерьевич");
       fio.setAttributeNode(attr_4_1_3);


       TransformerFactory transformerFactory = TransformerFactory.newInstance();
       transformerFactory.setAttribute("indent-number", 2);
       Transformer transformer = transformerFactory.newTransformer();
       transformer.setOutputProperty(OutputKeys.INDENT, "yes");
       transformer.setOutputProperty(OutputKeys.ENCODING, "windows-1251");
       transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
       document.setXmlStandalone(true);
       DOMSource domSource = new DOMSource(document);
       ByteArrayOutputStream out = new ByteArrayOutputStream();
       transformer.transform(domSource, new StreamResult(out));
       InputStream in = new ByteArrayInputStream(out.toByteArray());
       String fileName = "ON_NSCHFDOPPR_" + edoId + "_2BM-7702798640-770201001-201505260306237557139_" + edoDate;
       xmlFile = new DefaultStreamedContent(in, "application/xml", fileName + ".xml");
   }
 }
