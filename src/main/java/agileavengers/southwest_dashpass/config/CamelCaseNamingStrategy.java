package agileavengers.southwest_dashpass.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class CamelCaseNamingStrategy implements PhysicalNamingStrategy {

    private final PhysicalNamingStrategy delegate = new PhysicalNamingStrategyStandardImpl();

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment context) {
        return delegate.toPhysicalCatalogName(name, context);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment context) {
        return delegate.toPhysicalSchemaName(name, context);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return delegate.toPhysicalTableName(name, context);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
        return delegate.toPhysicalSequenceName(name, context);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        if (name == null) {
            return null;
        }

        // Preserve explicit column names as-is
        String originalName = name.getText();

        // Check if the name is explicitly in snake_case or camelCase
        if (isSnakeCase(originalName) || isCamelCase(originalName)) {
            return Identifier.toIdentifier(originalName);
        }

        // Fallback to the default strategy
        return delegate.toPhysicalColumnName(name, context);
    }

    private boolean isSnakeCase(String name) {
        return name.contains("_");
    }

    private boolean isCamelCase(String name) {
        return name.matches(".*[a-z][A-Z].*");
    }
}
