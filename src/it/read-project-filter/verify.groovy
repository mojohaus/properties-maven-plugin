File prop = new File( basedir, 'target/classes/project.properties' )
assert prop.exists()

Properties written = new Properties()
prop.withInputStream { written.load( it ) }
assert written.getProperty( 'it.wanted' ) == 'yes'
assert !written.containsKey( 'it.unwanted' ) // excluded
assert !written.containsKey( 'it.other' ) // not included
