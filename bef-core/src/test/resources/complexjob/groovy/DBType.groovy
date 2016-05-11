public enum DBType {
    SQLServer("TODO"),
    Oracle("oracle.jdbc.driver.OracleDriver"),
    Postgres("TODO"),
    Hana("TODO")

    final String driverClassName;

    private DBType(String driverClassName) {
        this.driverClassName = driverClassName;
    }

}