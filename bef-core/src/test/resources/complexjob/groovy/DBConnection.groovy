import java.sql.Connection
import java.sql.DriverManager

public class DBConnection {
    DBType dbType;
    String url;
    String username;
    String password;

    Connection connect() throws Exception {
        Class.forName(dbType.driverClassName);
        return DriverManager.getConnection(url, username, password);
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        DBConnection that = (DBConnection) o

        if (dbType != that.dbType) return false
        if (password != that.password) return false
        if (url != that.url) return false
        if (username != that.username) return false

        return true
    }

    int hashCode() {
        int result
        result = (dbType != null ? dbType.hashCode() : 0)
        result = 31 * result + (url != null ? url.hashCode() : 0)
        result = 31 * result + (username != null ? username.hashCode() : 0)
        result = 31 * result + (password != null ? password.hashCode() : 0)
        return result
    }

    @Override
    public String toString() {
        return "DBConnection{" +
                "dbType=" + dbType +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}