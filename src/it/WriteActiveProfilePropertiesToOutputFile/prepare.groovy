import junit.framework.Assert;

File target = new File(basedir, "target/itresults");
if (!target.exists()) {
  target.mkdirs();
  Assert.assertTrue("Folder " + target.absolutePath + " must exist", target.exists());
}

File f = new File(target, "WriteActiveProfilePropertiesToOutputFile");
if (f.exists()) {
  f.deleteDir();
}

Assert.assertFalse("File " + f.absolutePath + " must exist", f.exists());
