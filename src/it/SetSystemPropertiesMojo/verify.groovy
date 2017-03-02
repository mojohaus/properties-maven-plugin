import junit.framework.Assert;

def props = System.getProperties()
println props
Assert.assertEquals("random prop", "RANDOMVALUE", props["RandomProperty"] )
