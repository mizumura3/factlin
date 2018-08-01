# factlin

factlin is Kotlin test fixture generator from existing database schema.

This gradle plugin generate fixture factory class and insert helper method (currentlly support only  [Ninja-Squad/DbSetup](https://github.com/Ninja-Squad/DbSetup))

## example

From this database schema

```sql
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  name VARCHAR(256) NOT NULL,
  job VARCHAR(256) NOT NULL DEFAULT 'engineer',
  status VARCHAR(256) NOT NULL DEFAULT 'ACTIVE',
  age INTEGER NOT NULL DEFAULT 30,
  nick_name VARCHAR(256)
);

COMMENT ON TABLE users IS 'user table';
COMMENT ON COLUMN users.id IS 'primary key';
COMMENT ON COLUMN users.name IS 'name';
COMMENT ON COLUMN users.job IS 'job name';
COMMENT ON COLUMN users.status IS 'activate status';
COMMENT ON COLUMN users.age IS 'age';
COMMENT ON COLUMN users.nick_name IS 'nick name';
```

Generated Kotlin code is like this

```kotlin
data class UsersFixture (
    val id: Int = 0, // primary key
    val name: String = "", // name
    val job: String = "", // job name
    val status: String = "", // activate status
    val age: Int = 0, // age
    val nick_name: String? = null // nick name
)

fun DbSetupBuilder.insertUsersFixture(f: UsersFixture) {
    insertInto("users") {
        mappedValues(
                "id" to f.id,
                "name" to f.name,
                "job" to f.job,
                "status" to f.status,
                "age" to f.age,
                "nick_name" to f.nick_name
        )
    }
}
```

Use in your test (with Ninja-Squad/DbSetup)

```kotlin
dbSetup(dest) {
    deleteAllFrom(listOf("users")) 
    
    // using generated codes. this codes insert datas to your database
    insertUsersFixture(UsersFixture(name = "foo"))
    insertUsersFixture(UsersFixture(name = "bar"))
}.launch()
```

## benefit

- type and null safe db test fixture generation (no more excel and insert into...)
- help to generate similar test fixtures from another fixture (generated fixture class is Kotlin data class)

## supported databases

- PostgreSQL
- MariaDB

## how to use

create tables in your db

```sql
CREATE TABLE ...
```

build.gradle

```gradle
buildscript {
    repositories {
        maven { url "https://dl.bintray.com/maeharin/factlin" }
    }
    dependencies {
        classpath 'com.maeharin:factlin:0.0.2'
        classpath "org.postgresql:postgresql:42.1.4"
    }
}

apply plugin: 'kotlin'

// factlin config
apply plugin: 'factlin'
factlin {
    dbUrl = "jdbc:postgresql://{DB_HOST}/{DB_NAME}"
    dbUser = "{DB_USER}"
    dbPassword = "{DB_PASS}"
    dbDialect = "postgres"
}

repositories {
    mavenCentral()
}
dependencies {
    compile "org.postgresql:postgresql:42.1.4"
    testCompile 'com.ninja-squad:DbSetup-kotlin:2.1.0' // generated code depends on dbsetup
}
```

generate codes

```
./gradlew factlin
```

use generated codes for your db connection test (ex: JUnit)

```kotlin
class SampleTest {
    val dest = DriverManagerDestination("jdbc:postgresql://{DB_HOST}/{DB_NAME}", "DB_USER", "DB_PASS")

    init {
        Class.forName("org.postgresql.Driver")
    }

    @Test
    fun testInsertUser() {
        dbSetup(dest) {
            deleteAllFrom(listOf("users"))
            insertUsersFixture(UsersFixture())
        }.launch()
        
        val stmt = dest.connection.createStatement()
        val rs = stmt.executeQuery("select * from users")
        
        assertTrue(rs.next())
        assertEquals("", rs.getString("name"))
    }
}
```

run test

```
./gradlew test
```

done!

## customize

you can customize default behavior at build.gradle like this:

```gradle
apply plugin: 'factlin'
factlin {
    dbUrl = "jdbc:postgresql://{DB_HOST}/{DB_NAME}"
    
    dbUser = "{DB_USER}"
    
    dbPassword = "{DB_PASS}"
    
    // postgres or mariadb
    dbDialect = "postgres"
    
    // output directory of generaged code
    // defult: src/test/kotlin/com/maeharin/factlin/fixtures
    fixtureOutputDir = "src/test/kotlin/com/example/myapp/fixtures"
    
    // package name of generated code
    // default: com.maeharin.factlin.fixtures
    fixturePackageName = "com.example.myapp.fixtures"
    
    // custom template(FreeMarker) path
    // default: nothing (use factlin default template file)
    // https://github.com/maeharin/factlin/blob/master/src/main/resources/factlin/class.ftl
    fixtureTemplatePath = "src/test/resources/factlin/class_template.ftl"
    
    // exclude table names
    // default: nothing
    excludeTables = ["payment_p2007_01", "payment_p2007_02", "payment_p2007_03", "payment_p2007_04", "payment_p2007_05", "payment_p2007_06"]
    
    // include table names
    // default: all tables
    includeTables = ["users", "film"]
    
    // true if delete output directory before code generation 
    // * recommended to be true for ease of maintenance
    // default: false
    cleanOutputDir = true
    
    // override specific default value
    // format: [tableName, columnName, defaultValue]
    // default: nothing
    customDefaultValues = [
        ["users", "job", "\"engineer\""],
        ["users", "status", "\"ACTIVE\""],
    ]
    
    // override database column type to kotlin type mapping
    // format: [databaseColumnType, KotlinType]
    // see default mapper: https://github.com/maeharin/factlin/blob/master/src/main/kotlin/com/maeharin/factlin/core/kclassbuilder/KClassBuilder.kt#L59
    customTypeMapper = [
        "year": "SHORT", // treat custom type [year] as SHORT
    ]
}
```

sample project

- [postgres](./sample-postgres)
- [mariadb](./sample-mariadb)

## how to develop

unit test

```
./gradlew test
```

integration test

```
docker-compose up
./init-db.sh
./integration-test.sh
```

## publish

```
// .env is secret
source .env
./gradlew build bintrayUpload --info
```
