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

package intellij.haskell.editor.formatter.settings

import com.intellij.lang.Language
import com.intellij.openapi.options.Configurable
import com.intellij.psi.codeStyle.{CodeStyleSettings, CodeStyleSettingsProvider, CustomCodeStyleSettings}
import intellij.haskell.HaskellLanguage
import org.jetbrains.annotations.NotNull

class HaskellCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
  override def getConfigurableDisplayName: String = {
    "Haskell"
  }

  @NotNull
  override def createSettingsPage(settings: CodeStyleSettings, originalSettings: CodeStyleSettings): Configurable = {
    new HaskellCodeStyleConfigurable(settings, originalSettings)
  }

  @NotNull
  override def createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings = {
    new HaskellCodeStyleSettings(settings)
  }

  override def getLanguage: Language = {
    HaskellLanguage.Instance
  }
}