/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2017, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 * 
 *
 */
package net.sourceforge.plantuml.project3;

import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.plantuml.AbstractPSystem;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SpriteContainerEmpty;
import net.sourceforge.plantuml.core.DiagramDescription;
import net.sourceforge.plantuml.core.DiagramDescriptionImpl;
import net.sourceforge.plantuml.core.ImageData;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.HorizontalAlignment;
import net.sourceforge.plantuml.graphic.HtmlColor;
import net.sourceforge.plantuml.graphic.HtmlColorSetSimple;
import net.sourceforge.plantuml.graphic.HtmlColorUtils;
import net.sourceforge.plantuml.graphic.IHtmlColorSet;
import net.sourceforge.plantuml.graphic.TextBlock;
import net.sourceforge.plantuml.graphic.UDrawable;
import net.sourceforge.plantuml.ugraphic.ColorMapperIdentity;
import net.sourceforge.plantuml.ugraphic.ImageBuilder;
import net.sourceforge.plantuml.ugraphic.UChangeColor;
import net.sourceforge.plantuml.ugraphic.UFont;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.ULine;
import net.sourceforge.plantuml.ugraphic.UTranslate;

public class GanttDiagram extends AbstractPSystem {

	private final Map<TaskCode, Task> tasks = new LinkedHashMap<TaskCode, Task>();
	private final List<GanttConstraint> constraints = new ArrayList<GanttConstraint>();

	public DiagramDescription getDescription() {
		return new DiagramDescriptionImpl("(Project)", getClass());
	}

	@Override
	protected ImageData exportDiagramNow(OutputStream os, int index, FileFormatOption fileFormatOption)
			throws IOException {
		final double dpiFactor = 1;
		final double margin = 10;

		// public ImageBuilder(ColorMapper colorMapper, double dpiFactor, HtmlColor mybackcolor, String metadata,
		// String warningOrError, double margin1, double margin2, Animation animation, boolean useHandwritten) {

		sortTasks();

		final ImageBuilder imageBuilder = new ImageBuilder(new ColorMapperIdentity(), 1, null, "", "", 0, 0, null,
				false);
		imageBuilder.setUDrawable(getUDrawable());

		return imageBuilder.writeImageTOBEMOVED(fileFormatOption, os);
	}

	private void sortTasks() {
		System.err.println("SORTING TASKS!");
		final TaskCodeSimpleOrder order = getCanonicalOrder(1);
		final List<Task> list = new ArrayList<Task>(tasks.values());
		Collections.sort(list, new Comparator<Task>() {
			public int compare(Task task1, Task task2) {
				return order.compare(task1.getCode(), task2.getCode());
			}
		});
		tasks.clear();
		for (Task task : list) {
			tasks.put(task.getCode(), task);
		}
	}

	private UDrawable getUDrawable() {
		return new UDrawable() {
			public void drawU(UGraphic ug) {
				final TimeScale timeScale = new TimeScale();
				drawInternal(ug, timeScale);
				drawConstraints(ug, timeScale);
			}
		};
	}

	private void drawConstraints(final UGraphic ug, TimeScale timeScale) {
		for (GanttConstraint constraint : constraints) {
			constraint.getUDrawable(timeScale).drawU(ug);
		}

	}

	private void drawInternal(final UGraphic ug, TimeScale timeScale) {

		// System.err.println("==============");
		// for (Task task : tasks.values()) {
		// System.err.println("task=" + task + " " + ((TaskImpl) task).debug());
		// }
		// System.err.println("==============");

		Instant min = tasks.values().iterator().next().getStart();
		Instant max = tasks.values().iterator().next().getEnd();
		for (Task task : tasks.values()) {
			final Instant start = task.getStart();
			final Instant end = task.getEnd();
			if (min.compareTo(start) > 0) {
				min = start;
			}
			if (max.compareTo(end) < 0) {
				max = end;
			}
		}

		final double header = 16;
		double y = header;
		for (Task task : tasks.values()) {
			final TaskDraw draw = new TaskDraw(task, timeScale, y);
			task.setTaskDraw(draw);
			y += draw.getHeight();
		}

		ULine vbar = new ULine(0, y);
		final double xmin = timeScale.getPixel(min);
		final double xmax = timeScale.getPixel(max.increment());
		ug.apply(new UChangeColor(HtmlColorUtils.LIGHT_GRAY)).draw(new ULine(xmax - xmin, 0));
		ug.apply(new UChangeColor(HtmlColorUtils.LIGHT_GRAY)).apply(new UTranslate(0, header - 3))
				.draw(new ULine(xmax - xmin, 0));

		for (Instant i = min; i.compareTo(max.increment()) <= 0; i = i.increment()) {
			final TextBlock num = Display.getWithNewlines(i.toShortString()).create(getFontConfiguration(),
					HorizontalAlignment.LEFT, new SpriteContainerEmpty());
			final double x1 = timeScale.getPixel(i);
			final double x2 = timeScale.getPixel(i.increment());
			final double width = num.calculateDimension(ug.getStringBounder()).getWidth();
			final double delta = (x2 - x1) - width;
			if (i.compareTo(max.increment()) < 0) {
				num.drawU(ug.apply(new UTranslate(x1 + delta / 2, 0)));
			}
			ug.apply(new UChangeColor(HtmlColorUtils.LIGHT_GRAY)).apply(new UTranslate(x1, 0)).draw(vbar);
		}

		for (Task task : tasks.values()) {
			final TaskDraw draw = task.getTaskDraw();
			draw.drawU(ug.apply(new UTranslate(0, draw.getY())));
			draw.getTitle().drawU(
					ug.apply(new UTranslate(timeScale.getPixel(task.getStart().increment()), draw.getY())));
		}

	}

	private FontConfiguration getFontConfiguration() {
		final UFont font = new UFont("Serif", Font.PLAIN, 10);
		return new FontConfiguration(font, HtmlColorUtils.LIGHT_GRAY, HtmlColorUtils.LIGHT_GRAY, false);
	}

	public Task getTask(TaskCode code) {
		Task result = tasks.get(code);
		if (result == null) {
			result = new TaskImpl(code);
			tasks.put(code, result);
		}
		return result;
	}

	private TaskCodeSimpleOrder getCanonicalOrder(int hierarchyHeader) {
		final List<TaskCode> codes = new ArrayList<TaskCode>();
		for (TaskCode code : tasks.keySet()) {
			if (code.getHierarchySize() >= hierarchyHeader) {
				codes.add(code.truncateHierarchy(hierarchyHeader));
			}
		}
		return new TaskCodeSimpleOrder(codes, hierarchyHeader);
	}

	private int getMaxHierarchySize() {
		int max = Integer.MIN_VALUE;
		for (TaskCode code : tasks.keySet()) {
			max = Math.max(max, code.getHierarchySize());
		}
		return max;
	}

	public void addContraint(GanttConstraint constraint) {
		constraints.add(constraint);
	}

	private final IHtmlColorSet colorSet = new HtmlColorSetSimple();

	public IHtmlColorSet getIHtmlColorSet() {
		return colorSet;
	}

}
