/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding.validation;

import org.eclipse.core.internal.databinding.conversion.NumberToShortConverter;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;

/**
 * Validates if a Number can fit in a Short.
 * <p>
 * Class is thread safe.
 * </p>
 *
 * @since 1.0
 */
public class NumberToShortValidator extends NumberToNumberValidator {
	private static final Short MIN = Short.valueOf(Short.MIN_VALUE);
	private static final Short MAX = Short.valueOf(Short.MAX_VALUE);

	public NumberToShortValidator(NumberToShortConverter converter) {
		super(converter, MIN, MAX);
	}

	@Override
	protected boolean inRange(Number number) {
		return StringToNumberParser.inShortRange(number);
	}
}
