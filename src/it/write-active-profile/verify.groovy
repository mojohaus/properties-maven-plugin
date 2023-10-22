import java.nio.file.Files

File prop = new File(basedir, 'target/classes/project.properties')
assert prop.exists()

def properties = new Properties()
try (InputStream input = Files.newInputStream(prop.toPath())) {
    properties.load(input)
}

def expected = [
        'project-properties-test1' : 'value21',
        'project-properties-test2' : 'value22',
        'settings-properties-test1': 'value1',
        'settings-properties-test2': 'value2'
]

assert properties.equals(expected)