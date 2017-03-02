import junit.framework.Assert;

def props = System.getProperties()

Assert.assertNull("No random prop", props["RandomProperty"] )
