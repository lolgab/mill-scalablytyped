package com.github.lolgab.mill.scalablytyped

import mill.testkit.IntegrationTester
import utest.*

object IntegrationTests extends TestSuite {
  val resourceFolder = os.Path(sys.env("MILL_TEST_RESOURCE_DIR"))
  val millExecutable = os.Path(sys.env("MILL_EXECUTABLE_PATH"))
  val testVersion = sys.env("MILL_SCALABLYTYPED_TEST_VERSION")

  def buildTester(folder: String): IntegrationTester = {
    val tester = IntegrationTester(
      daemonMode = false,
      workspaceSourcePath = resourceFolder / folder,
      millExecutable = millExecutable
    )
    tester.modifyFile(
      tester.workspacePath / "build.mill",
      _.replace("::TEST", s"::$testVersion")
    )
    tester
  }

  def npmCi(tester: IntegrationTester): Unit =
    os.proc("npm", "ci").call(cwd = tester.workspacePath)

  def runNode(tester: IntegrationTester, module: String): String = {
    val linked = tester.eval(("show", s"$module.fastLinkJS"))
    assert(linked.isSuccess)
    val dest: String = ujson.read(linked.out)("dest").str
    val dir = os.Path(dest.split(':').last: String)
    os.proc("node", dir / "main.js").call().out.trim()
  }

  def tests: Tests = Tests {
    test("simple") {
      val tester = buildTester("simple")
      npmCi(tester)
      assert(runNode(tester, "module") == "[ [ 1, 3 ], [ 2, 4 ] ]")
    }
    test("package-lock") {
      val tester = buildTester("package-lock")
      def installedLodashTypes() = ujson
        .read(
          os.read(
            tester.workspacePath / "node_modules" / "@types" / "lodash" / "package.json"
          )
        )("version")
        .str
      // no manual `npm ci`: scalablyTypedNpmCi installs package-lock.json
      assert(runNode(tester, "module") == "[ [ 1, 3 ], [ 2, 4 ] ]")
      assert(installedLodashTypes() == "4.14.178")

      // a lock-only bump (package.json unchanged) re-runs `npm ci` and the conversion
      os.copy.over(
        tester.workspacePath / "package-lock.json.next",
        tester.workspacePath / "package-lock.json"
      )
      assert(tester.eval("module.compile").isSuccess)
      assert(installedLodashTypes() == "4.14.182")
    }
    test("std-libs") {
      val tester = buildTester("std-libs")
      npmCi(tester)
      assert(runNode(tester, "module") == "3")
    }
  }
}
