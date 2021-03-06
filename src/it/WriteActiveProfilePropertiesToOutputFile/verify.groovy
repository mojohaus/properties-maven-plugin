import junit.framework.Assert;

File target = new File(basedir, "target/itresults");

File f = new File(target, "WriteActiveProfilePropertiesToOutputFile");
def out = f.text
Assert.assertTrue("File " + f.absolutePath + " exists", f.exists());
Assert.assertNotSame("File is empty", 0, f.length());
Assert.assertTrue("Bob is a dog", out.contains("bob=dog"))
Assert.assertFalse("Bob is not a cat", out.contains("bob=cat"))