/*
 * Copyright 2020, 2021, 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package messy.msgio;

/**
 * Contains application state.
 *
 * @author Marco Schmidt
 */
public final class AppState
{
  private static boolean active = true;
  private static boolean mainFinished;

  private AppState()
  {
  }

  public static boolean isActive()
  {
    return active;
  }

  public static void setActive(boolean newVaue)
  {
    active = newVaue;
  }

  public static boolean isMainFinished()
  {
    return mainFinished;
  }

  public static void setMainFinished(boolean mainFinished)
  {
    AppState.mainFinished = mainFinished;
  }
}
