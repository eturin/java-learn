package ture.app.native_sql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Arrays;

// Создаем класс AppLocks для работы с sql-запросами напрямую
@Repository
public class AppLocks  {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final PreparedStatementCreator lockStatementCreator = connection ->
            connection.prepareStatement("SELECT pg_advisory_xact_lock(hashtext(?))");
    private final PreparedStatementCreator tryLockStatementCreator = connection ->
            connection.prepareStatement("SELECT pg_try_advisory_xact_lock(hashtext(?))");

    private void lck(String resource) {
        jdbcTemplate.execute(lockStatementCreator, (PreparedStatement ps) -> {
            ps.setString(1, resource);
            ps.execute();
            return null;
        });
    }

    // формирование блокируемой строки на основе объекта
    public String getLockResource(Object obj) {
        var clazz = obj.getClass();
        StringBuilder sb = new StringBuilder(clazz.getSimpleName());
        try {
            var field = clazz.getDeclaredField("Id");
            field.setAccessible(true);
            sb.append(String.format(": %s",field.get(obj)));
        } catch (NoSuchFieldException | IllegalAccessException e) {

        }
        return sb.toString();
    }

    // блокировк по строке
    public void lock(String resource) {
        this.lck(resource);
    }

    // блокировк по объекту
    public void lock(Object object) {
        var resource = getLockResource(object);
        this.lck(resource);
    }

    // блокировка по массиву объектов
    public void lock(Object[] m) {
        Arrays.stream(m)
              .map(this::getLockResource)
              .sorted()
              .forEach(this::lck);
    }

    Boolean tryLock(String resource) {
        return jdbcTemplate.query(connection -> {
            var ps = tryLockStatementCreator.createPreparedStatement(connection);
            ps.setString(1, resource);
            return ps;
        }, rs -> {
            // ResultSetExtractor
            return rs.next() ? rs.getBoolean(1) : Boolean.FALSE;
        });
    }
}
