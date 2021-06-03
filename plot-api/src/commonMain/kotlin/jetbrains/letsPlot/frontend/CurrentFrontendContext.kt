/*
 * Copyright (c) 2021. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package jetbrains.letsPlot.frontend

expect object CurrentFrontendContext {
    fun display(plotSpecRaw: MutableMap<String, Any>)
}