import sbt._

trait Defaults {
  def androidPlatformName = "android-2.1"
}
class LocationAlarm(info: ProjectInfo) extends ParentProject(info) {
  override def shouldCheckOutputDirectories = false
  override def updateAction = task { None }

  lazy val main  = project(".", "LocationAlarm", new MainProject(_))
  lazy val tests = project("tests",  "tests", new TestProject(_), main)

  class MainProject(info: ProjectInfo) extends AndroidProject(info) with Defaults with MarketPublish with TypedResources{
    val keyalias  = "change-me"
    val scalatest = "org.scalatest" % "scalatest" % "1.0" % "test"
    override def addonsPath = androidSdkPath / "add-ons" / "google_apis-7_r01" / "libs"
  }

  class TestProject(info: ProjectInfo) extends AndroidTestProject(info) with Defaults
}
