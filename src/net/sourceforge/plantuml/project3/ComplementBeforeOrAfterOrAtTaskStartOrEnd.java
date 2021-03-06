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

import net.sourceforge.plantuml.command.regex.IRegex;
import net.sourceforge.plantuml.command.regex.RegexLeaf;
import net.sourceforge.plantuml.command.regex.RegexResult;

public class ComplementBeforeOrAfterOrAtTaskStartOrEnd implements ComplementPattern {

	public IRegex toRegex(String suffix) {
		return new RegexLeaf("COMPLEMENT" + suffix,
				"(?:at|(\\d+)[%s]+days?[%s]+(before|after))[%s]+\\[([^\\[\\]]+?)\\].?s[%s]+(start|end)");
	}

	public Complement getComplement(GanttDiagram system, RegexResult arg, String suffix) {
		final String code = arg.get("COMPLEMENT" + suffix, 2);
		final String position = arg.get("COMPLEMENT" + suffix, 3);
		final Task task = system.getTask(new TaskCode(code));
		final String days = arg.get("COMPLEMENT" + suffix, 0);
		TaskInstant result = new TaskInstant(task, TaskAttribute.fromString(position));
		if (days != null) {
			int delta = Integer.parseInt(days);
			if ("before".equalsIgnoreCase(arg.get("COMPLEMENT" + suffix, 1))) {
				delta = -delta;
			}
			result = result.withDelta(delta);
		}
		return result;
	}
}
