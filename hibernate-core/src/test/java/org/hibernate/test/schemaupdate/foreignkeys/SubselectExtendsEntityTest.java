/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.schemaupdate.foreignkeys;

import org.hibernate.annotations.Subselect;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseUnitTestCase;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Mohsen Kashi
 */
@TestForIssue(jiraKey = "HHH-11185")
public class SubselectExtendsEntityTest extends BaseUnitTestCase {
    private File output;
    private StandardServiceRegistry ssr;
    private MetadataImplementor metadata;

    @Before
    public void setUp() throws IOException {
        output = File.createTempFile("update_script", ".sql");
        output.deleteOnExit();
        ssr = new StandardServiceRegistryBuilder().build();
    }

    @After
    public void tearsDown() {
        StandardServiceRegistryBuilder.destroy(ssr);
    }

    @Test
    public void testForeignKeyHasCorrectName() throws Exception {
        createSchema(new Class[]{FooBaba.class, FooBache.class});
        final List<String> sqlLines = Files.readAllLines(output.toPath(), Charset.defaultCharset());
        Assert.assertFalse("DTYPE field should not be added", sqlLines.get(3).contains("DTYPE"));
    }


    private void createSchema(Class[] annotatedClasses) {
        final MetadataSources metadataSources = new MetadataSources(ssr);

        for (Class c : annotatedClasses) {
            metadataSources.addAnnotatedClass(c);
        }
        metadata = (MetadataImplementor) metadataSources.buildMetadata();
        metadata.validate();
        new SchemaExport()
                .setHaltOnError(true)
                .setOutputFile(output.getAbsolutePath())
                .setFormat(false)
                .create(EnumSet.of(TargetType.SCRIPT), metadata);
    }

    @Entity
    @Table(name = "FOO_BABA")
    public static class FooBaba {
        @Column(name = "FOO_ID")
        @Id
        @GeneratedValue
        private Long id;

        @Column(name = "FOO_NAME")
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Entity
    @Subselect("select F.* ,F.FOO_ID || F.FOO_NAME as FOO_ID_NAME from FOO_BABA F")
    public static class FooBache extends FooBaba {
        @Column(name = "FOO_ID_NAME")
        public String fooIdName;

        public String getFooIdName() {
            return fooIdName;
        }

        public void setFooIdName(String fooIdName) {
            this.fooIdName = fooIdName;
        }
    }

}
