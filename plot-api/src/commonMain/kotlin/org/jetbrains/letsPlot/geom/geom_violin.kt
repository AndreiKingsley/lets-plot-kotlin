/*
 * Copyright (c) 2022. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.geom

import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.intern.GeomKind
import org.jetbrains.letsPlot.intern.Layer
import org.jetbrains.letsPlot.intern.layer.*
import org.jetbrains.letsPlot.intern.layer.geom.ViolinAesthetics
import org.jetbrains.letsPlot.intern.layer.geom.ViolinMapping
import org.jetbrains.letsPlot.intern.layer.geom.ViolinParameters
import org.jetbrains.letsPlot.intern.layer.stat.YDensityStatAesthetics
import org.jetbrains.letsPlot.intern.layer.stat.YDensityStatParameters
import org.jetbrains.letsPlot.pos.positionDodge
import org.jetbrains.letsPlot.tooltips.TooltipOptions

@Suppress("ClassName", "SpellCheckingInspection")
/**
 * A violin plot is a mirrored density plot with an additional grouping as for a boxplot.
 *
 * ## Notes
 *
 * Computed variables:
 *
 * - ..violinwidth.. : density scaled for the violin plot, according to area, counts or to a constant maximum width (mapped by default).
 * - ..density.. : density estimate.
 * - ..count.. : density * number of points.
 * - ..scaled.. : density estimate, scaled to maximum of 1.
 * - ..quantile.. : quantile estimate.
 *
 * To hide axis tooltips, set "blank" or the result of `elementBlank()`
 * to the `axisTooltip`, `axisTooltipX` or `axisTooltipY` parameter of the `theme()`.
 *
 * ## Examples
 *
 * - [geom_violin.ipynb](https://nbviewer.org/github/JetBrains/lets-plot-docs/blob/master/source/kotlin_examples/cookbook/geom_violin.ipynb)
 *
 * - [violin_tails_cutoff.ipynb](https://nbviewer.org/github/JetBrains/lets-plot-docs/blob/master/source/kotlin_examples/cookbook/violin_tails_cutoff.ipynb)
 *
 * - [violin_show_half.ipynb](https://nbviewer.org/github/JetBrains/lets-plot-docs/blob/master/source/kotlin_examples/cookbook/violin_show_half.ipynb)
 *
 * - [quantile_parameters.ipynb](https://nbviewer.org/github/JetBrains/lets-plot-docs/blob/master/source/kotlin_examples/cookbook/quantile_parameters.ipynb)
 *
 * @param data The data to be displayed in this layer. If null, the default, the data
 *  is inherited from the plot data as specified in the call to [letsPlot][org.jetbrains.letsPlot.letsPlot].
 * @param stat default = `Stat.yDensity()`. The statistical transformation to use on the data for this layer.
 *  Supported transformations: `Stat.identity`, `Stat.bin()`, `Stat.count()`, etc. see [Stat][org.jetbrains.letsPlot.Stat].
 * @param position default = `positionDodge()`. Position adjustment: `positionIdentity`, `positionStack()`, `positionDodge()`, etc. see
 *  [Position](https://lets-plot.org/kotlin/-lets--plot--kotlin/org.jetbrains.letsPlot.pos/).
 * @param showLegend default = true.
 *  false - do not show legend for this layer.
 * @param inheritAes default = true.
 *  false - do not combine the layer aesthetic mappings with the plot shared mappings.
 * @param manualKey String or result of the call to the `layerKey()` function.
 *  The key to show in the manual legend. Specifies the text for the legend label or advanced settings using the `layerKey()` function.
 * @param sampling Result of the call to the `samplingXxx()` function.
 *  To prevent any sampling for this layer pass value `samplingNone`.
 *  For more info see [sampling.html](https://lets-plot.org/kotlin/sampling.html).
 * @param tooltips Result of the call to the `layerTooltips()` function.
 *  Specifies appearance, style and content.
 *  Set `tooltips = tooltipsNone` to hide tooltips from the layer.
 * @param orientation Specifies the axis that the layer's stat and geom should run along.
 *  The default value (`null`) automatically determines the orientation based on the aesthetic mapping.
 *  If the automatic detection doesn't work, it can be set explicitly by specifying the "x" or "y" orientation.
 * @param quantiles Draw horizontal lines at the given quantiles of the density estimate.
 * @param quantileLines default = false.
 *  Show the quantile lines.
 * @param showHalf default = 0.
 *  - If -1 then it's drawing only half of each violin.
 *  - If 1 then it's drawing other half.
 *  - If 0 then violins looking as usual.
 * @param x X-axis coordinates.
 * @param y Y-axis coordinates.
 * @param violinWidth Density scaled for the violin plot, according to area, counts or to a constant maximum width.
 * @param alpha Transparency level of a layer. Understands numbers between 0 and 1.
 * @param color Color of the geometry.
 *  For more info see: [aesthetics.html#color-and-fill](https://lets-plot.org/kotlin/aesthetics.html#color-and-fill).
 * @param fill Fill color.
 *  For more info see: [aesthetics.html#color-and-fill](https://lets-plot.org/kotlin/aesthetics.html#color-and-fill).
 * @param linetype Type of the line of border.
 *  Accept codes or names (0 = "blank", 1 = "solid", 2 = "dashed", 3 = "dotted", 4 = "dotdash", 5 = "longdash", 6 = "twodash"),
 *  a hex string (up to 8 digits for dash-gap lengths),
 *  or a pattern `offset to listOf(dash, gap, ...)` / `listOf(dash, gap, ...)`.
 *  For more info see: [aesthetics.html#line-types](https://lets-plot.org/kotlin/aesthetics.html#line-types).
 * @param size Lines width.
 *  Defines line width.
 * @param width Width of violin bounding box.
 * @param weight Used by `Stat.yDensity()` stat to compute weighted density.
 * @param scale default = "area".
 *  - If "area", all violins have the same area.
 *  - If "count", areas are scaled proportionally to the number of observations.
 *  - If "width", all violins have the same maximum width.
 * @param tailsCutoff default = 3.0.
 *  Extends domain of each violin on `tailsCutoff * bw` if `trim = false`.
 * @param bw String or Double.
 *  The method (or exact value) of bandwidth. Either a string (choose among "nrd0" and "nrd") or a double.
 * @param kernel The kernel we use to calculate the density function. Choose among "gaussian", "cosine", "optcosine",
 *  "rectangular" (or "uniform"), "triangular", "biweight" (or "quartic"), "epanechikov" (or "parabolic").
 * @param n The number of sampled points for plotting the function.
 * @param trim default = true.
 *  Trims the tails of the violins to the range of the data.
 * @param adjust Adjusts the value of bandwidth by multiplying it. Changes how smooth the frequency curve is.
 * @param fullScanMax Maximum size of data to use density computation with "full scan".
 *  For bigger data, less accurate but more efficient density computation is applied.
 * @param colorBy default = "color" ("fill", "color", "paint_a", "paint_b", "paint_c").
 *  Defines the color aesthetic for the geometry.
 * @param fillBy default = "fill" ("fill", "color", "paint_a", "paint_b", "paint_c").
 *  Defines the fill aesthetic for the geometry.
 * @param mapping Set of aesthetic mappings.
 *  Aesthetic mappings describe the way that variables in the data are
 *  mapped to plot "aesthetics".
 */
class geomViolin(
    data: Map<*, *>? = null,
    stat: StatOptions = Stat.yDensity(),
    position: PosOptions = positionDodge(),
    showLegend: Boolean = true,
    inheritAes: Boolean? = null,
    manualKey: Any? = null,
    sampling: SamplingOptions? = null,
    tooltips: TooltipOptions? = null,
    orientation: String? = null,
    override val x: Number? = null,
    override val y: Number? = null,
    override val violinWidth: Number? = null,
    override val alpha: Number? = null,
    override val color: Any? = null,
    override val fill: Any? = null,
    override val linetype: Any? = null,
    override val size: Number? = null,
    override val width: Number? = null,
    override val weight: Number? = null,
    override val scale: String? = null,
    override val tailsCutoff: Number? = null,
    override val bw: Any? = null,
    override val kernel: String? = null,
    override val n: Int? = null,
    override val trim: Boolean? = null,
    override val adjust: Number? = null,
    override val fullScanMax: Int? = null,
    override val quantiles: List<Number>? = null,
    override val quantileLines: Boolean? = null,
    override val showHalf: Number? = null,
    override val colorBy: String? = null,
    override val fillBy: String? = null,
    mapping: ViolinMapping.() -> Unit = {}
) : ViolinAesthetics,
    ViolinParameters,
    YDensityStatAesthetics,
    YDensityStatParameters,
    WithColorOption,
    WithFillOption,
    Layer(
        mapping = ViolinMapping().apply(mapping).seal(),
        data = data,
        geom = GeomOptions(GeomKind.VIOLIN),
        stat = stat,
        position = position,
        showLegend = showLegend,
        inheritAes = inheritAes,
        manualKey = manualKey,
        sampling = sampling,
        tooltips = tooltips,
        orientation = orientation
    ) {

    override fun seal() = super<ViolinAesthetics>.seal() +
            super<ViolinParameters>.seal() +
            super<YDensityStatAesthetics>.seal() +
            super<YDensityStatParameters>.seal() +
            super<WithColorOption>.seal() +
            super<WithFillOption>.seal()
}
