
package org.binatel.bill.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/* Entity c jpql-запросами */ 

@Entity
@Table(name = "contract_client")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ContractClient.findAll", query = "SELECT c FROM ContractClient c"),
    @NamedQuery(name = "ContractClient.findById", query = "SELECT c FROM ContractClient c WHERE c.id = :id"),
    @NamedQuery(name = "ContractClient.findByNumber", query = "SELECT c FROM ContractClient c WHERE c.number = :number"),
    @NamedQuery(name = "ContractClient.findByDateBeginTs", query = "SELECT c FROM ContractClient c WHERE c.dateBeginTs = :dateBeginTs"),
    @NamedQuery(name = "ContractClient.findByDateEndTs", query = "SELECT c FROM ContractClient c WHERE c.dateEndTs = :dateEndTs"),
    @NamedQuery(name = "ContractClient.findByBalance", query = "SELECT c FROM ContractClient c WHERE c.balance = :balance"),
    @NamedQuery(name = "ContractClient.findByCredit", query = "SELECT c FROM ContractClient c WHERE c.credit = :credit"),
    @NamedQuery(name = "ContractClient.findByAgentAbonPercent", query = "SELECT c FROM ContractClient c WHERE c.agentAbonPercent = :agentAbonPercent"),
    @NamedQuery(name = "ContractClient.findByAgentTrafPercent", query = "SELECT c FROM ContractClient c WHERE c.agentTrafPercent = :agentTrafPercent"),
    @NamedQuery(name = "ContractClient.findByAgentTrafMaxpay", query = "SELECT c FROM ContractClient c WHERE c.agentTrafMaxpay = :agentTrafMaxpay"), 
    @NamedQuery(name = "ContractClient.findByIsAvans", query = "SELECT c FROM ContractClient c WHERE c.isAvans = :isAvans"),
    @NamedQuery(name = "ContractClient.findByArchive", query = "SELECT c FROM ContractClient c WHERE c.archive = :archive"),
    @NamedQuery(name = "ContractClient.findByNoticeBalance", query = "SELECT c FROM ContractClient c WHERE c.noticeBalance = :noticeBalance"),
    @NamedQuery(name = "ContractClient.findByNoticeBalanceBorder", query = "SELECT c FROM ContractClient c WHERE c.noticeBalanceBorder = :noticeBalanceBorder"),
    @NamedQuery(name = "ContractClient.findByCompany", query = "SELECT c FROM ContractClient c WHERE c.companyId = :company"),
    @NamedQuery(name = "ContractClient.findByTlfGroupOperId", query = "SELECT c FROM ContractClient c WHERE c.tlfGroupId = :tlfGroupId OR c.tlfGroupIdCli = :tlfGroupId"),
    @NamedQuery(name = "ContractClient.updateContractBalance", query = "UPDATE ContractClient c SET c.balance = c.balance - :amount WHERE c.id = :contractId"),
    @NamedQuery(name = "ContractClient.addContractBalance", query = "UPDATE ContractClient c SET c.balance = c.balance + :amount WHERE c.id = :contractId"),
    @NamedQuery(name = "ContractClient.findByAgent", query = "SELECT c FROM ContractClient c WHERE c.agentId = :agentId")
})
public class ContractClient implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "number")
    private String number;
    @Basic(optional = false)
    @NotNull
    @Column(name = "agent_number")
    private String agentNumber;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_begin_ts")
    private long dateBeginTs;
    @Column(name = "date_end_ts")
    private long dateEndTs;
    @Basic(optional = false)
    @NotNull
    @Column(name = "balance")
    private double balance;
    @Basic(optional = false)
    @NotNull
    @Column(name = "credit")
    private double credit;
    @Basic(optional = false)
    @NotNull
    @Column(name = "agent_date_end")
    private long agentDateEnd;
    @Basic(optional = false)
    @NotNull
    @Column(name = "agent_abon_percent")
    private int agentAbonPercent;
    @Basic(optional = false)
    @NotNull
    @Column(name = "agent_traf_percent")
    private int agentTrafPercent;
    @Basic(optional = false)
    @NotNull
    @Column(name = "is_avans")
    private short isAvans;
    @Basic(optional = false)
    @NotNull
    @Column(name = "is_800")
    private short is800;
    @Basic(optional = false)
    @NotNull
    @Column(name = "archive")
    private short archive;
    @Basic(optional = false)
    @NotNull
    @Column(name = "notice_balance")
    private short noticeBalance;
    @Basic(optional = false)
    @NotNull
    @Column(name = "notice_balance_border")
    private double noticeBalanceBorder;
    @Basic(optional = false)
    @NotNull
    @Column(name = "agent_traf_maxpay")
    private double agentTrafMaxpay;
    @Basic(optional = false)
    @NotNull
    @Column(name = "doc_orig_received")
    private short docOrigRecieved;

    @JoinColumn(name = "company_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private CompanyClient companyId;
    @JoinColumn(name = "tlf_group_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private TlfGroupOper tlfGroupId;
    @JoinColumn(name = "tlf_group_id_cli", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private TlfGroupOper tlfGroupIdCli;
    @JoinColumn(name = "tarif_in_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private TlfTarifClient tarifInId;
    @JoinColumn(name = "tarif_out_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private TlfTarifClient tarifOutId;
    @JoinColumn(name = "lang_msg", referencedColumnName = "id")
    @ManyToOne
    private LangMsg langMsg;
    @JoinColumn(name = "agent_id", referencedColumnName = "id")
    @ManyToOne
    private CompanyAgent agentId;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractId")
    private List<TlfCdrRadiusClient> tlfCdrRadiusClientList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractId")
    private  List<TlfNumbersLinkClient> tlfNumbersLinkClientList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractId")
    private List<IpAddressesLinkClient> ipAddressesLinkClientList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractId")
    private List<ServiceLinkClient> serviceLinkClientList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractId")
    private List<TlfAllowClidClient> tlfAllowClidClientList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractId")
    private List<AccountingServicesClient> accountingServicesClientList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractId")
    private List<TlfOtherNumbersLinkClient> tlfOtherNumbersLinkClientList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tpContractId")
    private List<ClientContractTrustPayment> clientContractTrustPaymentList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractId")
    private List<ClientAccountingDocs> clientAccountingDocsList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractId")
    private List<ContractClientFiles> contractClientFilesList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractId")
    private List<AccountingData4ExportTo1C> accountingData4ExportTo1CList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contractId")
    private List<AccountingData4DocsClient> accountingData4DocsClientList;


    public ContractClient() {
    }

    public ContractClient(Integer id) {
        this.id = id;
    }

    public ContractClient(Integer id, String number, String agentNumber, long dateBeginTs, double balance, double credit, long agentDateEnd, int agentAbonPercent, int agentTrafPercent, short isAvans, short is800 ,short archive, short noticeBalance, double noticeBalanceBorder, double agentTrafMaxpay, short docOrigRecieved) {
        this.id = id;
        this.number = number;
        this.agentNumber = agentNumber;
        this.dateBeginTs = dateBeginTs;
        this.balance = balance;
        this.credit = credit;
        this.agentDateEnd = agentDateEnd;
        this.agentAbonPercent = agentAbonPercent;
        this.agentTrafPercent = agentTrafPercent;
        this.isAvans = isAvans;
        this.is800 = is800;
        this.archive = archive;
        this.noticeBalance = noticeBalance;
        this.noticeBalanceBorder = noticeBalanceBorder;
        this.agentTrafMaxpay = agentTrafMaxpay;
        this.docOrigRecieved = docOrigRecieved;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAgentNumber() {
        return agentNumber;
    }

    public void setAgentNumber(String agentNumber) {
        this.agentNumber = agentNumber;
    }
    
    public long getDateBeginTs() {
        return dateBeginTs;
    }

    public void setDateBeginTs(long dateBeginTs) {
        this.dateBeginTs = dateBeginTs;
    }

    public long getDateEndTs() {
        return dateEndTs;
    }

    public void setDateEndTs(long dateEndTs) {
        this.dateEndTs = dateEndTs;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public long getAgentDateEnd() {
        return agentDateEnd;
    }

    public void setAgentDateEnd(long agentDateEnd) {
        this.agentDateEnd = agentDateEnd;
    }

    public int getAgentAbonPercent() {
        return agentAbonPercent;
    }

    public void setAgentAbonPercent(int agentAbonPercent) {
        this.agentAbonPercent = agentAbonPercent;
    }

    public int getAgentTrafPercent() {
        return agentTrafPercent;
    }

    public void setAgentTrafPercent(int agentTrafPercent) {
        this.agentTrafPercent = agentTrafPercent;
    }

    public short getIsAvans() {
        return isAvans;
    }

    public void setIsAvans(short isAvans) {
        this.isAvans = isAvans;
    }

    public short getIs800() {
        return is800;
    }

    public void setIs800(short is800) {
        this.is800 = is800;
    }
    
    public short getArchive() {
        return archive;
    }

    public void setArchive(short archive) {
        this.archive = archive;
    }

    public CompanyClient getCompanyId() {
        return companyId;
    }

    public short getNoticeBalance() {
        return noticeBalance;
    }

    public void setNoticeBalance(short noticeBalance) {
        this.noticeBalance = noticeBalance;
    }

    public double getNoticeBalanceBorder() {
        return noticeBalanceBorder;
    }

    public void setNoticeBalanceBorder(double noticeBalanceBorder) {
        this.noticeBalanceBorder = noticeBalanceBorder;
    }

    public double getAgentTrafMaxpay() {
        return agentTrafMaxpay;
    }

    public void setAgentTrafMaxpay(double agentTrafMaxpay) {
        this.agentTrafMaxpay = agentTrafMaxpay;
    }
        
    public void setCompanyId(CompanyClient companyId) {
        this.companyId = companyId;
    }

    public TlfGroupOper getTlfGroupId() {
        return tlfGroupId;
    }

    public void setTlfGroupId(TlfGroupOper tlfGroupId) {
        this.tlfGroupId = tlfGroupId;
    }

    public TlfGroupOper getTlfGroupIdCli() {
        return tlfGroupIdCli;
    }

    public void setTlfGroupIdCli(TlfGroupOper tlfGroupIdCli) {
        this.tlfGroupIdCli = tlfGroupIdCli;
    }
    
    public TlfTarifClient getTarifInId() {
        return tarifInId;
    }

    public void setTarifInId(TlfTarifClient tarifInId) {
        this.tarifInId = tarifInId;
    }

    public TlfTarifClient getTarifOutId() {
        return tarifOutId;
    }

    public void setTarifOutId(TlfTarifClient tarifOutId) {
        this.tarifOutId = tarifOutId;
    }

    public LangMsg getLangMsg() {
        return langMsg;
    }

    public void setLangMsg(LangMsg langMsg) {
        this.langMsg = langMsg;
    }

    public short getDocOrigRecieved() {
        return docOrigRecieved;
    }

    public void setDocOrigRecieved(short docOrigRecieved) {
        this.docOrigRecieved = docOrigRecieved;
    }

    public CompanyAgent getAgentId() {
        return agentId;
    }

    public void setAgentId(CompanyAgent agentId) {
        this.agentId = agentId;
    }

    @XmlTransient
    public List<TlfCdrRadiusClient> getTlfCdrRadiusClientList() {
        return tlfCdrRadiusClientList;
    }

    public void setTlfCdrRadiusClientList(List<TlfCdrRadiusClient> tlfCdrRadiusClientList) {
        this.tlfCdrRadiusClientList = tlfCdrRadiusClientList;
    }

    @XmlTransient
    public List<TlfNumbersLinkClient> getTlfNumbersLinkClientList() {
        return  tlfNumbersLinkClientList;
    }

    public void setTlfNumbersLinkClientList(List<TlfNumbersLinkClient> tlfNumbersLinkClientList) {
        this.tlfNumbersLinkClientList = tlfNumbersLinkClientList;
    }
    @XmlTransient
    public List<IpAddressesLinkClient> getIpAddressesLinkClientList() {
        return ipAddressesLinkClientList;
    }

    public void setIpAddressesLinkClientList(List<IpAddressesLinkClient> ipAddressesLinkClientList) {
        this.ipAddressesLinkClientList = ipAddressesLinkClientList;
    }

    @XmlTransient
    public List<ServiceLinkClient> getServiceLinkClientList() {
        return serviceLinkClientList;
    }

    public void setServiceLinkClientList(List<ServiceLinkClient> serviceLinkClientList) {
        this.serviceLinkClientList = serviceLinkClientList;
    }

    @XmlTransient
    public List<TlfAllowClidClient> getTlfAllowClidClientList() {
        return tlfAllowClidClientList;
    }

    public void setTlfAllowClidClientList(List<TlfAllowClidClient> tlfAllowClidClientList) {
        this.tlfAllowClidClientList = tlfAllowClidClientList;
    }

    @XmlTransient
    public List<AccountingServicesClient> getAccountingServicesClientList() {
        return accountingServicesClientList;
    }

    public void setAccountingServicesClientList(List<AccountingServicesClient> accountingServicesClientList) {
        this.accountingServicesClientList = accountingServicesClientList;
    }

    @XmlTransient
    public List<TlfOtherNumbersLinkClient> getTlfOtherNumbersLinkClientList() {
        return tlfOtherNumbersLinkClientList;
    }

    public void setTlfOtherNumbersLinkClientList(List<TlfOtherNumbersLinkClient> tlfOtherNumbersLinkClientList) {
        this.tlfOtherNumbersLinkClientList = tlfOtherNumbersLinkClientList;
    }

    @XmlTransient
    public List<ClientContractTrustPayment> getClientContractTrustPaymentList() {
        return clientContractTrustPaymentList;
    }

    public void setClientContractTrustPaymentList(List<ClientContractTrustPayment> clientContractTrustPaymentList) {
        this.clientContractTrustPaymentList = clientContractTrustPaymentList;
    }

    @XmlTransient
    public List<ClientAccountingDocs> getClientAccountingDocsList() {
        return clientAccountingDocsList;
    }

    public void setClientAccountingDocsList(List<ClientAccountingDocs> clientAccountingDocsList) {
        this.clientAccountingDocsList = clientAccountingDocsList;
    }

    @XmlTransient
    public List<ContractClientFiles> getContractClientFilesList() {
        return contractClientFilesList;
    }

    public void setContractClientFilesList(List<ContractClientFiles> contractClientFilesList) {
        this.contractClientFilesList = contractClientFilesList;
    }

    @XmlTransient
    public List<AccountingData4ExportTo1C> getAccountingData4ExportTo1CList() {
        return accountingData4ExportTo1CList;
    }

    public void setAccountingData4ExportTo1CList(List<AccountingData4ExportTo1C> accountingData4ExportTo1CList) {
        this.accountingData4ExportTo1CList = accountingData4ExportTo1CList;
    }

    @XmlTransient
    public List<AccountingData4DocsClient> getAccountingData4DocsClientList() {
        return accountingData4DocsClientList;
    }

    public void setAccountingData4DocsClientList(List<AccountingData4DocsClient> accountingData4DocsClientList) {
        this.accountingData4DocsClientList = accountingData4DocsClientList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ContractClient)) {
            return false;
        }
        ContractClient other = (ContractClient) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "binatel.bill.entities.ContractClient[ id=" + id + " ]";
    }
    
}
