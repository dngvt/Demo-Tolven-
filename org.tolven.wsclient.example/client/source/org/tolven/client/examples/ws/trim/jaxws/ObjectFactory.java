
package org.tolven.client.examples.ws.trim.jaxws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.tolven.client.examples.ws.trim.jaxws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _CreateTolvenMessage_QNAME = new QName("http://tolven.org/trim", "createTolvenMessage");
    private final static QName _LogoutResponse_QNAME = new QName("http://tolven.org/trim", "logoutResponse");
    private final static QName _DisplayInstantiatedTrimResponse_QNAME = new QName("http://tolven.org/trim", "displayInstantiatedTrimResponse");
    private final static QName _CreateTolvenMessageResponse_QNAME = new QName("http://tolven.org/trim", "createTolvenMessageResponse");
    private final static QName _SubmitMessage_QNAME = new QName("http://tolven.org/trim", "submitMessage");
    private final static QName _AddTrimAsPayloadResponse_QNAME = new QName("http://tolven.org/trim", "addTrimAsPayloadResponse");
    private final static QName _SubmitMessageResponse_QNAME = new QName("http://tolven.org/trim", "submitMessageResponse");
    private final static QName _SubmitTrim_QNAME = new QName("http://tolven.org/trim", "submitTrim");
    private final static QName _DisplayInstantiatedTrim_QNAME = new QName("http://tolven.org/trim", "displayInstantiatedTrim");
    private final static QName _Logout_QNAME = new QName("http://tolven.org/trim", "logout");
    private final static QName _Trim_QNAME = new QName("urn:tolven-org:trim:4.0", "trim");
    private final static QName _AddTrimAsPayload_QNAME = new QName("http://tolven.org/trim", "addTrimAsPayload");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tolven.client.examples.ws.trim.jaxws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Compute }
     * 
     */
    public Compute createCompute() {
        return new Compute();
    }

    /**
     * Create an instance of {@link Field }
     * 
     */
    public Field createField() {
        return new Field();
    }

    /**
     * Create an instance of {@link Trim_Type }
     * 
     */
    public Trim_Type createTrim_Type() {
        return new Trim_Type();
    }

    /**
     * Create an instance of {@link Supply }
     * 
     */
    public Supply createSupply() {
        return new Supply();
    }

    /**
     * Create an instance of {@link SearchPhrase }
     * 
     */
    public SearchPhrase createSearchPhrase() {
        return new SearchPhrase();
    }

    /**
     * Create an instance of {@link DataType }
     * 
     */
    public DataType createDataType() {
        return new DataType();
    }

    /**
     * Create an instance of {@link CSSlot }
     * 
     */
    public CSSlot createCSSlot() {
        return new CSSlot();
    }

    /**
     * Create an instance of {@link ED }
     * 
     */
    public ED createED() {
        return new ED();
    }

    /**
     * Create an instance of {@link CESlot }
     * 
     */
    public CESlot createCESlot() {
        return new CESlot();
    }

    /**
     * Create an instance of {@link ActParticipation }
     * 
     */
    public ActParticipation createActParticipation() {
        return new ActParticipation();
    }

    /**
     * Create an instance of {@link DiagnosticImage }
     * 
     */
    public DiagnosticImage createDiagnosticImage() {
        return new DiagnosticImage();
    }

    /**
     * Create an instance of {@link IISlot }
     * 
     */
    public IISlot createIISlot() {
        return new IISlot();
    }

    /**
     * Create an instance of {@link ValueSet }
     * 
     */
    public ValueSet createValueSet() {
        return new ValueSet();
    }

    /**
     * Create an instance of {@link CopyTo }
     * 
     */
    public CopyTo createCopyTo() {
        return new CopyTo();
    }

    /**
     * Create an instance of {@link URGREAL }
     * 
     */
    public URGREAL createURGREAL() {
        return new URGREAL();
    }

    /**
     * Create an instance of {@link URGPQ }
     * 
     */
    public URGPQ createURGPQ() {
        return new URGPQ();
    }

    /**
     * Create an instance of {@link BLSlot }
     * 
     */
    public BLSlot createBLSlot() {
        return new BLSlot();
    }

    /**
     * Create an instance of {@link QualifiedEntity }
     * 
     */
    public QualifiedEntity createQualifiedEntity() {
        return new QualifiedEntity();
    }

    /**
     * Create an instance of {@link Party }
     * 
     */
    public Party createParty() {
        return new Party();
    }

    /**
     * Create an instance of {@link IVLTS }
     * 
     */
    public IVLTS createIVLTS() {
        return new IVLTS();
    }

    /**
     * Create an instance of {@link RoleParticipation }
     * 
     */
    public RoleParticipation createRoleParticipation() {
        return new RoleParticipation();
    }

    /**
     * Create an instance of {@link MO }
     * 
     */
    public MO createMO() {
        return new MO();
    }

    /**
     * Create an instance of {@link TemplateId }
     * 
     */
    public TemplateId createTemplateId() {
        return new TemplateId();
    }

    /**
     * Create an instance of {@link EN }
     * 
     */
    public EN createEN() {
        return new EN();
    }

    /**
     * Create an instance of {@link Access }
     * 
     */
    public Access createAccess() {
        return new Access();
    }

    /**
     * Create an instance of {@link Diet }
     * 
     */
    public Diet createDiet() {
        return new Diet();
    }

    /**
     * Create an instance of {@link ValidateFacet }
     * 
     */
    public ValidateFacet createValidateFacet() {
        return new ValidateFacet();
    }

    /**
     * Create an instance of {@link TSDATETIME }
     * 
     */
    public TSDATETIME createTSDATETIME() {
        return new TSDATETIME();
    }

    /**
     * Create an instance of {@link TSDATETIMEFULL }
     * 
     */
    public TSDATETIMEFULL createTSDATETIMEFULL() {
        return new TSDATETIMEFULL();
    }

    /**
     * Create an instance of {@link INTSlot }
     * 
     */
    public INTSlot createINTSlot() {
        return new INTSlot();
    }

    /**
     * Create an instance of {@link TSDATE }
     * 
     */
    public TSDATE createTSDATE() {
        return new TSDATE();
    }

    /**
     * Create an instance of {@link PIVL }
     * 
     */
    public PIVL createPIVL() {
        return new PIVL();
    }

    /**
     * Create an instance of {@link BindTo }
     * 
     */
    public BindTo createBindTo() {
        return new BindTo();
    }

    /**
     * Create an instance of {@link NewFacet }
     * 
     */
    public NewFacet createNewFacet() {
        return new NewFacet();
    }

    /**
     * Create an instance of {@link ListBind }
     * 
     */
    public ListBind createListBind() {
        return new ListBind();
    }

    /**
     * Create an instance of {@link NonPersonLivingSubject }
     * 
     */
    public NonPersonLivingSubject createNonPersonLivingSubject() {
        return new NonPersonLivingSubject();
    }

    /**
     * Create an instance of {@link Unused }
     * 
     */
    public Unused createUnused() {
        return new Unused();
    }

    /**
     * Create an instance of {@link IVLPQ }
     * 
     */
    public IVLPQ createIVLPQ() {
        return new IVLPQ();
    }

    /**
     * Create an instance of {@link Choice }
     * 
     */
    public Choice createChoice() {
        return new Choice();
    }

    /**
     * Create an instance of {@link INT }
     * 
     */
    public INT createINT() {
        return new INT();
    }

    /**
     * Create an instance of {@link PQSlot }
     * 
     */
    public PQSlot createPQSlot() {
        return new PQSlot();
    }

    /**
     * Create an instance of {@link TEL }
     * 
     */
    public TEL createTEL() {
        return new TEL();
    }

    /**
     * Create an instance of {@link ActRelationship }
     * 
     */
    public ActRelationship createActRelationship() {
        return new ActRelationship();
    }

    /**
     * Create an instance of {@link URL }
     * 
     */
    public URL createURL() {
        return new URL();
    }

    /**
     * Create an instance of {@link GTSSlot }
     * 
     */
    public GTSSlot createGTSSlot() {
        return new GTSSlot();
    }

    /**
     * Create an instance of {@link Place }
     * 
     */
    public Place createPlace() {
        return new Place();
    }

    /**
     * Create an instance of {@link REAL }
     * 
     */
    public REAL createREAL() {
        return new REAL();
    }

    /**
     * Create an instance of {@link Act }
     * 
     */
    public Act createAct() {
        return new Act();
    }

    /**
     * Create an instance of {@link Message }
     * 
     */
    public Message createMessage() {
        return new Message();
    }

    /**
     * Create an instance of {@link PlaceholderBind }
     * 
     */
    public PlaceholderBind createPlaceholderBind() {
        return new PlaceholderBind();
    }

    /**
     * Create an instance of {@link Observation }
     * 
     */
    public Observation createObservation() {
        return new Observation();
    }

    /**
     * Create an instance of {@link Employee }
     * 
     */
    public Employee createEmployee() {
        return new Employee();
    }

    /**
     * Create an instance of {@link LanguageCommunication }
     * 
     */
    public LanguageCommunication createLanguageCommunication() {
        return new LanguageCommunication();
    }

    /**
     * Create an instance of {@link URGINT }
     * 
     */
    public URGINT createURGINT() {
        return new URGINT();
    }

    /**
     * Create an instance of {@link EDSlot }
     * 
     */
    public EDSlot createEDSlot() {
        return new EDSlot();
    }

    /**
     * Create an instance of {@link LicensedEntity }
     * 
     */
    public LicensedEntity createLicensedEntity() {
        return new LicensedEntity();
    }

    /**
     * Create an instance of {@link NullFlavor }
     * 
     */
    public NullFlavor createNullFlavor() {
        return new NullFlavor();
    }

    /**
     * Create an instance of {@link TolvenId }
     * 
     */
    public TolvenId createTolvenId() {
        return new TolvenId();
    }

    /**
     * Create an instance of {@link Role }
     * 
     */
    public Role createRole() {
        return new Role();
    }

    /**
     * Create an instance of {@link TSSlot }
     * 
     */
    public TSSlot createTSSlot() {
        return new TSSlot();
    }

    /**
     * Create an instance of {@link REALSlot }
     * 
     */
    public REALSlot createREALSlot() {
        return new REALSlot();
    }

    /**
     * Create an instance of {@link Person }
     * 
     */
    public Person createPerson() {
        return new Person();
    }

    /**
     * Create an instance of {@link SubstanceAdministration }
     * 
     */
    public SubstanceAdministration createSubstanceAdministration() {
        return new SubstanceAdministration();
    }

    /**
     * Create an instance of {@link IVLREAL }
     * 
     */
    public IVLREAL createIVLREAL() {
        return new IVLREAL();
    }

    /**
     * Create an instance of {@link Organization }
     * 
     */
    public Organization createOrganization() {
        return new Organization();
    }

    /**
     * Create an instance of {@link STSlot }
     * 
     */
    public STSlot createSTSlot() {
        return new STSlot();
    }

    /**
     * Create an instance of {@link TELSlot }
     * 
     */
    public TELSlot createTELSlot() {
        return new TELSlot();
    }

    /**
     * Create an instance of {@link MOSlot }
     * 
     */
    public MOSlot createMOSlot() {
        return new MOSlot();
    }

    /**
     * Create an instance of {@link PublicHealthCase }
     * 
     */
    public PublicHealthCase createPublicHealthCase() {
        return new PublicHealthCase();
    }

    /**
     * Create an instance of {@link TSDATEFULL }
     * 
     */
    public TSDATEFULL createTSDATEFULL() {
        return new TSDATEFULL();
    }

    /**
     * Create an instance of {@link TSBIRTH }
     * 
     */
    public TSBIRTH createTSBIRTH() {
        return new TSBIRTH();
    }

    /**
     * Create an instance of {@link SETCDSlot }
     * 
     */
    public SETCDSlot createSETCDSlot() {
        return new SETCDSlot();
    }

    /**
     * Create an instance of {@link URGTS }
     * 
     */
    public URGTS createURGTS() {
        return new URGTS();
    }

    /**
     * Create an instance of {@link ENSlot }
     * 
     */
    public ENSlot createENSlot() {
        return new ENSlot();
    }

    /**
     * Create an instance of {@link SETRTOSlot }
     * 
     */
    public SETRTOSlot createSETRTOSlot() {
        return new SETRTOSlot();
    }

    /**
     * Create an instance of {@link IVLINTSlot }
     * 
     */
    public IVLINTSlot createIVLINTSlot() {
        return new IVLINTSlot();
    }

    /**
     * Create an instance of {@link II }
     * 
     */
    public II createII() {
        return new II();
    }

    /**
     * Create an instance of {@link SETEDSlot }
     * 
     */
    public SETEDSlot createSETEDSlot() {
        return new SETEDSlot();
    }

    /**
     * Create an instance of {@link IVLINT }
     * 
     */
    public IVLINT createIVLINT() {
        return new IVLINT();
    }

    /**
     * Create an instance of {@link AD }
     * 
     */
    public AD createAD() {
        return new AD();
    }

    /**
     * Create an instance of {@link PQ }
     * 
     */
    public PQ createPQ() {
        return new PQ();
    }

    /**
     * Create an instance of {@link SCSlot }
     * 
     */
    public SCSlot createSCSlot() {
        return new SCSlot();
    }

    /**
     * Create an instance of {@link Application }
     * 
     */
    public Application createApplication() {
        return new Application();
    }

    /**
     * Create an instance of {@link IntegrityCheckType }
     * 
     */
    public IntegrityCheckType createIntegrityCheckType() {
        return new IntegrityCheckType();
    }

    /**
     * Create an instance of {@link Transition }
     * 
     */
    public Transition createTransition() {
        return new Transition();
    }

    /**
     * Create an instance of {@link SETIISlot }
     * 
     */
    public SETIISlot createSETIISlot() {
        return new SETIISlot();
    }

    /**
     * Create an instance of {@link Entity }
     * 
     */
    public Entity createEntity() {
        return new Entity();
    }

    /**
     * Create an instance of {@link Device }
     * 
     */
    public Device createDevice() {
        return new Device();
    }

    /**
     * Create an instance of {@link RTO }
     * 
     */
    public RTO createRTO() {
        return new RTO();
    }

    /**
     * Create an instance of {@link Material }
     * 
     */
    public Material createMaterial() {
        return new Material();
    }

    /**
     * Create an instance of {@link IVLPQSlot }
     * 
     */
    public IVLPQSlot createIVLPQSlot() {
        return new IVLPQSlot();
    }

    /**
     * Create an instance of {@link ObservationValueSlot }
     * 
     */
    public ObservationValueSlot createObservationValueSlot() {
        return new ObservationValueSlot();
    }

    /**
     * Create an instance of {@link Procedure }
     * 
     */
    public Procedure createProcedure() {
        return new Procedure();
    }

    /**
     * Create an instance of {@link TS }
     * 
     */
    public TS createTS() {
        return new TS();
    }

    /**
     * Create an instance of {@link ST }
     * 
     */
    public ST createST() {
        return new ST();
    }

    /**
     * Create an instance of {@link ENXPSlot }
     * 
     */
    public ENXPSlot createENXPSlot() {
        return new ENXPSlot();
    }

    /**
     * Create an instance of {@link ADXPSlot }
     * 
     */
    public ADXPSlot createADXPSlot() {
        return new ADXPSlot();
    }

    /**
     * Create an instance of {@link Container }
     * 
     */
    public Container createContainer() {
        return new Container();
    }

    /**
     * Create an instance of {@link BL }
     * 
     */
    public BL createBL() {
        return new BL();
    }

    /**
     * Create an instance of {@link SC }
     * 
     */
    public SC createSC() {
        return new SC();
    }

    /**
     * Create an instance of {@link CV }
     * 
     */
    public CV createCV() {
        return new CV();
    }

    /**
     * Create an instance of {@link SETPQSlot }
     * 
     */
    public SETPQSlot createSETPQSlot() {
        return new SETPQSlot();
    }

    /**
     * Create an instance of {@link ADSlot }
     * 
     */
    public ADSlot createADSlot() {
        return new ADSlot();
    }

    /**
     * Create an instance of {@link CS }
     * 
     */
    public CS createCS() {
        return new CS();
    }

    /**
     * Create an instance of {@link ManufacturedMaterial }
     * 
     */
    public ManufacturedMaterial createManufacturedMaterial() {
        return new ManufacturedMaterial();
    }

    /**
     * Create an instance of {@link LivingSubject }
     * 
     */
    public LivingSubject createLivingSubject() {
        return new LivingSubject();
    }

    /**
     * Create an instance of {@link Transitions }
     * 
     */
    public Transitions createTransitions() {
        return new Transitions();
    }

    /**
     * Create an instance of {@link IVLTSSlot }
     * 
     */
    public IVLTSSlot createIVLTSSlot() {
        return new IVLTSSlot();
    }

    /**
     * Create an instance of {@link RTOSlot }
     * 
     */
    public RTOSlot createRTOSlot() {
        return new RTOSlot();
    }

    /**
     * Create an instance of {@link LabelFacet }
     * 
     */
    public LabelFacet createLabelFacet() {
        return new LabelFacet();
    }

    /**
     * Create an instance of {@link PatientEncounter }
     * 
     */
    public PatientEncounter createPatientEncounter() {
        return new PatientEncounter();
    }

    /**
     * Create an instance of {@link CE }
     * 
     */
    public CE createCE() {
        return new CE();
    }

    /**
     * Create an instance of {@link IntegrityCheck }
     * 
     */
    public IntegrityCheck createIntegrityCheck() {
        return new IntegrityCheck();
    }

    /**
     * Create an instance of {@link CD }
     * 
     */
    public CD createCD() {
        return new CD();
    }

    /**
     * Create an instance of {@link SETCESlot }
     * 
     */
    public SETCESlot createSETCESlot() {
        return new SETCESlot();
    }

    /**
     * Create an instance of {@link Patient }
     * 
     */
    public Patient createPatient() {
        return new Patient();
    }

    /**
     * Create an instance of {@link CDSlot }
     * 
     */
    public CDSlot createCDSlot() {
        return new CDSlot();
    }

    /**
     * Create an instance of {@link CR }
     * 
     */
    public CR createCR() {
        return new CR();
    }

    /**
     * Create an instance of {@link AddTrimAsPayload }
     * 
     */
    public AddTrimAsPayload createAddTrimAsPayload() {
        return new AddTrimAsPayload();
    }

    /**
     * Create an instance of {@link Logout }
     * 
     */
    public Logout createLogout() {
        return new Logout();
    }

    /**
     * Create an instance of {@link DisplayInstantiatedTrim }
     * 
     */
    public DisplayInstantiatedTrim createDisplayInstantiatedTrim() {
        return new DisplayInstantiatedTrim();
    }

    /**
     * Create an instance of {@link SubmitMessage }
     * 
     */
    public SubmitMessage createSubmitMessage() {
        return new SubmitMessage();
    }

    /**
     * Create an instance of {@link SubmitTrim }
     * 
     */
    public SubmitTrim createSubmitTrim() {
        return new SubmitTrim();
    }

    /**
     * Create an instance of {@link SubmitMessageResponse }
     * 
     */
    public SubmitMessageResponse createSubmitMessageResponse() {
        return new SubmitMessageResponse();
    }

    /**
     * Create an instance of {@link AddTrimAsPayloadResponse }
     * 
     */
    public AddTrimAsPayloadResponse createAddTrimAsPayloadResponse() {
        return new AddTrimAsPayloadResponse();
    }

    /**
     * Create an instance of {@link CreateTolvenMessageResponse }
     * 
     */
    public CreateTolvenMessageResponse createCreateTolvenMessageResponse() {
        return new CreateTolvenMessageResponse();
    }

    /**
     * Create an instance of {@link DisplayInstantiatedTrimResponse }
     * 
     */
    public DisplayInstantiatedTrimResponse createDisplayInstantiatedTrimResponse() {
        return new DisplayInstantiatedTrimResponse();
    }

    /**
     * Create an instance of {@link LogoutResponse }
     * 
     */
    public LogoutResponse createLogoutResponse() {
        return new LogoutResponse();
    }

    /**
     * Create an instance of {@link CreateTolvenMessage }
     * 
     */
    public CreateTolvenMessage createCreateTolvenMessage() {
        return new CreateTolvenMessage();
    }

    /**
     * Create an instance of {@link TolvenMessageWithAttachments }
     * 
     */
    public TolvenMessageWithAttachments createTolvenMessageWithAttachments() {
        return new TolvenMessageWithAttachments();
    }

    /**
     * Create an instance of {@link WebServiceField }
     * 
     */
    public WebServiceField createWebServiceField() {
        return new WebServiceField();
    }

    /**
     * Create an instance of {@link TolvenMessageAttachment }
     * 
     */
    public TolvenMessageAttachment createTolvenMessageAttachment() {
        return new TolvenMessageAttachment();
    }

    /**
     * Create an instance of {@link WebServiceTrim }
     * 
     */
    public WebServiceTrim createWebServiceTrim() {
        return new WebServiceTrim();
    }

    /**
     * Create an instance of {@link TolvenMessage }
     * 
     */
    public TolvenMessage createTolvenMessage() {
        return new TolvenMessage();
    }

    /**
     * Create an instance of {@link TolvenMessageProperty }
     * 
     */
    public TolvenMessageProperty createTolvenMessageProperty() {
        return new TolvenMessageProperty();
    }

    /**
     * Create an instance of {@link Compute.Property }
     * 
     */
    public Compute.Property createComputeProperty() {
        return new Compute.Property();
    }

    /**
     * Create an instance of {@link Compute.Attribute }
     * 
     */
    public Compute.Attribute createComputeAttribute() {
        return new Compute.Attribute();
    }

    /**
     * Create an instance of {@link Field.Values }
     * 
     */
    public Field.Values createFieldValues() {
        return new Field.Values();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateTolvenMessage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/trim", name = "createTolvenMessage")
    public JAXBElement<CreateTolvenMessage> createCreateTolvenMessage(CreateTolvenMessage value) {
        return new JAXBElement<CreateTolvenMessage>(_CreateTolvenMessage_QNAME, CreateTolvenMessage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LogoutResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/trim", name = "logoutResponse")
    public JAXBElement<LogoutResponse> createLogoutResponse(LogoutResponse value) {
        return new JAXBElement<LogoutResponse>(_LogoutResponse_QNAME, LogoutResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DisplayInstantiatedTrimResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/trim", name = "displayInstantiatedTrimResponse")
    public JAXBElement<DisplayInstantiatedTrimResponse> createDisplayInstantiatedTrimResponse(DisplayInstantiatedTrimResponse value) {
        return new JAXBElement<DisplayInstantiatedTrimResponse>(_DisplayInstantiatedTrimResponse_QNAME, DisplayInstantiatedTrimResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateTolvenMessageResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/trim", name = "createTolvenMessageResponse")
    public JAXBElement<CreateTolvenMessageResponse> createCreateTolvenMessageResponse(CreateTolvenMessageResponse value) {
        return new JAXBElement<CreateTolvenMessageResponse>(_CreateTolvenMessageResponse_QNAME, CreateTolvenMessageResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubmitMessage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/trim", name = "submitMessage")
    public JAXBElement<SubmitMessage> createSubmitMessage(SubmitMessage value) {
        return new JAXBElement<SubmitMessage>(_SubmitMessage_QNAME, SubmitMessage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddTrimAsPayloadResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/trim", name = "addTrimAsPayloadResponse")
    public JAXBElement<AddTrimAsPayloadResponse> createAddTrimAsPayloadResponse(AddTrimAsPayloadResponse value) {
        return new JAXBElement<AddTrimAsPayloadResponse>(_AddTrimAsPayloadResponse_QNAME, AddTrimAsPayloadResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubmitMessageResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/trim", name = "submitMessageResponse")
    public JAXBElement<SubmitMessageResponse> createSubmitMessageResponse(SubmitMessageResponse value) {
        return new JAXBElement<SubmitMessageResponse>(_SubmitMessageResponse_QNAME, SubmitMessageResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubmitTrim }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/trim", name = "submitTrim")
    public JAXBElement<SubmitTrim> createSubmitTrim(SubmitTrim value) {
        return new JAXBElement<SubmitTrim>(_SubmitTrim_QNAME, SubmitTrim.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DisplayInstantiatedTrim }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/trim", name = "displayInstantiatedTrim")
    public JAXBElement<DisplayInstantiatedTrim> createDisplayInstantiatedTrim(DisplayInstantiatedTrim value) {
        return new JAXBElement<DisplayInstantiatedTrim>(_DisplayInstantiatedTrim_QNAME, DisplayInstantiatedTrim.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Logout }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/trim", name = "logout")
    public JAXBElement<Logout> createLogout(Logout value) {
        return new JAXBElement<Logout>(_Logout_QNAME, Logout.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Trim_Type }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:tolven-org:trim:4.0", name = "trim")
    public JAXBElement<Trim_Type> createTrim(Trim_Type value) {
        return new JAXBElement<Trim_Type>(_Trim_QNAME, Trim_Type.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddTrimAsPayload }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tolven.org/trim", name = "addTrimAsPayload")
    public JAXBElement<AddTrimAsPayload> createAddTrimAsPayload(AddTrimAsPayload value) {
        return new JAXBElement<AddTrimAsPayload>(_AddTrimAsPayload_QNAME, AddTrimAsPayload.class, null, value);
    }

}
