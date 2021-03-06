/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.configuration.admin.web.util;

import com.liferay.configuration.admin.web.model.ConfigurationModel;
import com.liferay.portlet.dynamicdatamapping.model.DDMForm;
import com.liferay.portlet.dynamicdatamapping.model.DDMFormField;
import com.liferay.portlet.dynamicdatamapping.model.Value;
import com.liferay.portlet.dynamicdatamapping.storage.DDMFormFieldValue;
import com.liferay.portlet.dynamicdatamapping.storage.DDMFormValues;
import com.liferay.portlet.dynamicdatamapping.storage.FieldConstants;

import java.io.Serializable;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.osgi.service.metatype.AttributeDefinition;

/**
 * @author Kamesh Sampath
 * @author Raymond Augé
 * @author Marcellus Tavares
 */
public class DDMFormValuesToPropertiesConverter {

	public DDMFormValuesToPropertiesConverter(
		ConfigurationModel configurationModel, DDMFormValues ddmFormValues,
		Locale locale) {

		DDMForm ddmForm = ddmFormValues.getDDMForm();

		_configurationModel = configurationModel;
		_ddmFormFieldsMap = ddmForm.getDDMFormFieldsMap(false);
		_ddmFormFieldValuesMap = ddmFormValues.getDDMFormFieldValuesMap();
		_locale = locale;
	}

	public Dictionary<String, Object> getProperties() {
		Dictionary<String, Object> properties = new Hashtable<>();

		AttributeDefinition[] attributeDefinitions =
			_configurationModel.getAttributeDefinitions(ConfigurationModel.ALL);

		for (AttributeDefinition attributeDefinition : attributeDefinitions) {
			Object value = null;

			List<DDMFormFieldValue> ddmFormFieldValues =
				_ddmFormFieldValuesMap.get(attributeDefinition.getID());

			if (attributeDefinition.getCardinality() == 0) {
				value = toSimpleValue(ddmFormFieldValues.get(0));
			}
			else if (attributeDefinition.getCardinality() > 0) {
				value = toArrayValue(ddmFormFieldValues);
			}
			else if (attributeDefinition.getCardinality() < 0) {
				value = toVectorValue(ddmFormFieldValues);
			}

			properties.put(attributeDefinition.getID(), value);
		}

		return properties;
	}

	protected String getDDMFormFieldDataType(String fieldName) {
		DDMFormField ddmFormField = _ddmFormFieldsMap.get(fieldName);

		return ddmFormField.getDataType();
	}

	protected Serializable toArrayValue(
		List<DDMFormFieldValue> ddmFormFieldValues) {

		DDMFormFieldValue ddmFormFieldValue = ddmFormFieldValues.get(0);

		String dataType = getDDMFormFieldDataType(ddmFormFieldValue.getName());

		Vector<Serializable> values = toVectorValue(ddmFormFieldValues);

		return FieldConstants.getSerializable(dataType, values);
	}

	protected Serializable toSimpleValue(DDMFormFieldValue ddmFormFieldValue) {
		String dataType = getDDMFormFieldDataType(ddmFormFieldValue.getName());

		Value value = ddmFormFieldValue.getValue();

		return FieldConstants.getSerializable(
			dataType, value.getString(_locale));
	}

	protected Vector<Serializable> toVectorValue(
		List<DDMFormFieldValue> ddmFormFieldValues) {

		Vector<Serializable> values = new Vector<>();

		for (DDMFormFieldValue ddmFormFieldValue : ddmFormFieldValues) {
			values.add(toSimpleValue(ddmFormFieldValue));
		}

		return values;
	}

	private final ConfigurationModel _configurationModel;
	private final Map<String, DDMFormField> _ddmFormFieldsMap;
	private final Map<String, List<DDMFormFieldValue>> _ddmFormFieldValuesMap;
	private final Locale _locale;

}