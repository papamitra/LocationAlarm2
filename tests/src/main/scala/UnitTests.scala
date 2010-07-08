package org.papamitra.locationalarm.tests

import junit.framework.Assert._
import _root_.android.test.AndroidTestCase

class UnitTests extends AndroidTestCase {
  def testPackageIsCorrect {
    assertEquals("org.papamitra.locationalarm", getContext.getPackageName)
  }
}