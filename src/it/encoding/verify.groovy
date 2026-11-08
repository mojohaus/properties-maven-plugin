File prop = new File( basedir, 'target/classes/project.properties' )
assert prop.exists()

// read as UTF-8 from utf8.properties and written as UTF-8 again, not mangled by ISO-8859-1
String content = prop.getText( 'UTF-8' )
assert content.contains( 'greeting=Grüße €' )
