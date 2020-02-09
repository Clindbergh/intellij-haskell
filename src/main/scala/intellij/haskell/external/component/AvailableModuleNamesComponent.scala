/*
 * Copyright 2014-2019 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package intellij.haskell.external.component

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.{IndexNotReadyException, Project}
import com.intellij.psi.search.{FileTypeIndex, GlobalSearchScope}
import intellij.haskell.external.component.HaskellComponentsManager.StackComponentInfo
import intellij.haskell.external.repl.StackRepl.{BenchmarkType, TestSuiteType}
import intellij.haskell.psi.HaskellPsiUtil
import intellij.haskell.util.{ApplicationUtil, HaskellFileUtil, ScalaFutureUtil}
import intellij.haskell.{HaskellFileType, HaskellNotificationGroup}

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

private[component] object AvailableModuleNamesComponent {

  private final val TestStanzaTypes = Seq(TestSuiteType, BenchmarkType)

  private case class Key(stackComponentInfo: StackComponentInfo)

  private final val Cache: AsyncLoadingCache[Key, Iterable[String]] = Scaffeine().refreshAfterWrite(10.seconds).buildAsync((k: Key) => findAvailableProjectModuleNamesWithIndex(k.stackComponentInfo))

  def findAvailableModuleNamesWithIndex(stackComponentInfo: StackComponentInfo): Iterable[String] = {
    // A module can be a project module AND library module
    findAvailableLibraryModuleNames(stackComponentInfo) ++ findAvailableProjectModuleNames(stackComponentInfo)
  }

  def findAvailableModuleLibraryModuleNamesWithIndex(module: Module): Iterable[String] = {
    findModuleNamesInModule(module.getProject, module, Seq.empty, includeTests = false)
  }

  def findAvailableProjectModuleNames(stackComponentInfo: StackComponentInfo): Iterable[String] = {
    val key = Key(stackComponentInfo)
    ScalaFutureUtil.waitForValue(stackComponentInfo.module.getProject, Cache.get(key), s"getting project module names for ${key.stackComponentInfo.packageName}", 1.second) match {
      case Some(files) => files
      case _ =>
        Cache.synchronous().invalidate(key)
        Iterable()
    }
  }

  private def findAvailableProjectModuleNamesWithIndex(stackComponentInfo: StackComponentInfo): Iterable[String] = {
    val projectModulePackageNames = HaskellComponentsManager.findProjectModulePackageNames(stackComponentInfo.module.getProject)
    val libraryProjectModules = projectModulePackageNames.filter { case (_, n) => stackComponentInfo.buildDepends.contains(n) }.map(_._1)
    findModuleNamesInModule(stackComponentInfo.module.getProject, stackComponentInfo.module, libraryProjectModules, TestStanzaTypes.contains(stackComponentInfo.stanzaType))
  }

  private def findAvailableLibraryModuleNames(stackComponentInfo: StackComponentInfo): Iterable[String] = {
    HaskellComponentsManager.findStackComponentGlobalInfo(stackComponentInfo).map(_.packageInfos.flatMap(_.exposedModuleNames)).getOrElse(Iterable())
  }

  private def findModuleNamesInModule(project: Project, currentModule: Module, modules: Seq[Module], includeTests: Boolean): Iterable[String] = {
    for {
      vf <- findHaskellFiles(project, currentModule, modules, includeTests)
      hf <- HaskellFileUtil.convertToHaskellFileInReadAction(project, vf).toSeq
      mn <- HaskellPsiUtil.findModuleName(hf)
    } yield mn
  }

  private def findHaskellFiles(project: Project, currentModule: Module, projectModules: Seq[Module], includeTests: Boolean) = {
    ApplicationUtil.scheduleInReadActionWithWriteActionPriority(project, {
      try {
        val projectModulesScope = projectModules.foldLeft(GlobalSearchScope.EMPTY_SCOPE)({ case (x, y) => x.uniteWith(y.getModuleScope(false)) })
        val searchScope = currentModule.getModuleScope(includeTests).uniteWith(projectModulesScope)
        FileTypeIndex.getFiles(HaskellFileType.Instance, searchScope).asScala
      } catch {
        case _: IndexNotReadyException =>
          HaskellNotificationGroup.logInfoEvent(project, s"Index not ready while findHaskellFiles for module ${currentModule.getName} ")
          Iterable()
      }
    }, s"find Haskell files for module ${currentModule.getName}").toOption.toIterable.flatten
  }


}


