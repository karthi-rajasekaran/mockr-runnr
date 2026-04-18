package com.mockr.runnr.config;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitOffsetLimitHandler;

public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super(DatabaseVersion.make(3, 0)); // SQLite 3.x
        System.out.println("Instantiating SQLiteDialect");
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new SQLiteIdentityColumnSupport();
    }

    // 👇 Force Hibernate to use LIMIT instead of FETCH FIRST
    @Override
    public LimitHandler getLimitHandler() {
        System.out.println("Get Limit Handler");
        return LimitOffsetLimitHandler.INSTANCE;
    }

    private static class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {

        @Override
        public boolean supportsIdentityColumns() {
            return true;
        }

        @Override
        public String getIdentityColumnString(int type) {
            return "integer primary key autoincrement";
        }

        @Override
        public String getIdentitySelectString(String table, String column, int type) {
            return "select last_insert_rowid()";
        }
    }
}
