import Logic.Exceptions.ServiceException;

import java.sql.Connection;

public final class TestScene {

    private static final String INTERN_REGISTRATION_NUMBER = "S20000001";
    private static final String INTERN_FIRST_NAME = "Ana";
    private static final String INTERN_LAST_NAME = "García";
    private static final String INTERN_EMAIL = "ana.garcia@uv.mx";

    private static final String PROFESSOR_REGISTRATION_NUMBER = "P20000002";
    private static final String PROFESSOR_FIRST_NAME = "Elena";
    private static final String PROFESSOR_LAST_NAME = "Torres";
    private static final String PROFESSOR_EMAIL = "elena.torres@uv.mx";

    private static final String COORDINATOR_REGISTRATION_NUMBER = "P20000003";
    private static final String COORDINATOR_FIRST_NAME = "Carlos";
    private static final String COORDINATOR_LAST_NAME = "Ramírez";
    private static final String COORDINATOR_EMAIL = "carlos.ramirez@uv.mx";

    private final int idIntern;
    private final int idProfessor;
    private final int idCoordinator;
    private final int idOrganization;
    private final int idTechnical;
    private final int idProject;
    private final String nrc;

    private TestScene(SceneBuilder builder) {
        this.idIntern = builder.idIntern;
        this.idProfessor = builder.idProfessor;
        this.idCoordinator = builder.idCoordinator;
        this.idOrganization = builder.idOrganization;
        this.idTechnical = builder.idTechnical;
        this.idProject = builder.idProject;
        this.nrc = builder.nrc;
    }

    public static TestScene createFullScene(Connection connection) throws ServiceException {
        SceneBuilder builder = new SceneBuilder();
        builder.idIntern = persistIntern(connection);
        builder.idProfessor = persistProfessor(connection);
        builder.idCoordinator = persistCoordinator(connection);
        builder.idOrganization = persistOrganization(connection);
        builder.idTechnical = persistTechnical(connection, builder.idOrganization);
        builder.nrc = persistEducationalExperience(connection, builder.idProfessor);
        builder.idProject = persistProject(connection, builder);
        return new TestScene(builder);
    }

    public int getInternId() {
        return idIntern;
    }

    public int getProfessorId() {
        return idProfessor;
    }

    public int getCoordinatorId() {
        return idCoordinator;
    }

    public int getOrganizationId() {
        return idOrganization;
    }

    public int getTechnicalId() {
        return idTechnical;
    }

    public int getProjectId() {
        return idProject;
    }

    public String getNrc() {
        return nrc;
    }

    private static int persistIntern(Connection connection) throws ServiceException {
        int internId = new UserTestDataBuilder()
                .withRegistrationNumber(INTERN_REGISTRATION_NUMBER)
                .withFirstName(INTERN_FIRST_NAME)
                .withLastName(INTERN_LAST_NAME)
                .withEmail(INTERN_EMAIL)
                .withRole(TestConstants.ROLE_INTERN)
                .persist(connection);
        new InternTestDataBuilder().withUserId(internId).persist(connection);
        return internId;
    }

    private static int persistProfessor(Connection connection) throws ServiceException {
        int professorId = new UserTestDataBuilder()
                .withRegistrationNumber(PROFESSOR_REGISTRATION_NUMBER)
                .withFirstName(PROFESSOR_FIRST_NAME)
                .withLastName(PROFESSOR_LAST_NAME)
                .withEmail(PROFESSOR_EMAIL)
                .withRole(TestConstants.ROLE_PROFESSOR)
                .persist(connection);
        new ProfessorTestDataBuilder().withUserId(professorId).persist(connection);
        return professorId;
    }

    private static int persistCoordinator(Connection connection) throws ServiceException {
        int coordinatorId = new UserTestDataBuilder()
                .withRegistrationNumber(COORDINATOR_REGISTRATION_NUMBER)
                .withFirstName(COORDINATOR_FIRST_NAME)
                .withLastName(COORDINATOR_LAST_NAME)
                .withEmail(COORDINATOR_EMAIL)
                .withRole(TestConstants.ROLE_COORDINATOR)
                .persist(connection);
        new CoordinatorTestDataBuilder().withUserId(coordinatorId).persist(connection);
        return coordinatorId;
    }

    private static int persistOrganization(Connection connection) throws ServiceException {
        return new OrganizationTestDataBuilder().persist(connection);
    }

    private static int persistTechnical(Connection connection, int idOrganization) throws ServiceException {
        return new TechnicalResponsibleTestDataBuilder()
                .withOrganizationId(idOrganization)
                .persist(connection);
    }

    private static String persistEducationalExperience(Connection connection,
                                                       int idProfessor) throws ServiceException {
        new EducationalExperienceTestDataBuilder()
                .withProfessorId(idProfessor)
                .persist(connection);
        return TestConstants.DEFAULT_NRC;
    }

    private static int persistProject(Connection connection, SceneBuilder builder) throws ServiceException {
        return new ProjectTestDataBuilder()
                .withOrganizationId(builder.idOrganization)
                .withTechnicalId(builder.idTechnical)
                .withProfessorId(builder.idProfessor)
                .withNrc(builder.nrc)
                .persist(connection);
    }

    private static final class SceneBuilder {
        private int idIntern;
        private int idProfessor;
        private int idCoordinator;
        private int idOrganization;
        private int idTechnical;
        private int idProject;
        private String nrc;
    }
}
