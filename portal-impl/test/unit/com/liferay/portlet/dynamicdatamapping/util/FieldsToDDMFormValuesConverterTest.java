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

package com.liferay.portlet.dynamicdatamapping.util;

import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portlet.dynamicdatamapping.BaseDDMTestCase;
import com.liferay.portlet.dynamicdatamapping.model.DDMForm;
import com.liferay.portlet.dynamicdatamapping.model.DDMFormField;
import com.liferay.portlet.dynamicdatamapping.model.DDMStructure;
import com.liferay.portlet.dynamicdatamapping.model.Value;
import com.liferay.portlet.dynamicdatamapping.service.DDMStructureLocalServiceUtil;
import com.liferay.portlet.dynamicdatamapping.storage.DDMFormFieldValue;
import com.liferay.portlet.dynamicdatamapping.storage.DDMFormValues;
import com.liferay.portlet.dynamicdatamapping.storage.Field;
import com.liferay.portlet.dynamicdatamapping.storage.Fields;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.powermock.core.classloader.annotations.PrepareForTest;

/**
 * @author Marcellus Tavares
 */
@PrepareForTest({DDMStructureLocalServiceUtil.class, LocaleUtil.class})
public class FieldsToDDMFormValuesConverterTest extends BaseDDMTestCase {

	@Before
	public void setUp() throws Exception {
		setUpDDMFormJSONDeserializerUtil();
		setUpDDMFormJSONSerializerUtil();
		setUpDDMStructureLocalServiceUtil();
		setUpDDMUtil();
		setUpFieldsToDDMFormValuesConverterUtil();
		setUpHtmlUtil();
		setUpJSONFactoryUtil();
		setUpLocaleUtil();
		setUpPropsUtil();
		setUpSAXReaderUtil();
	}

	@Test
	public void testConversionWithFieldsDisplayNotAvailable() throws Exception {
		DDMForm ddmForm = createDDMForm();

		addDDMFormFields(
			ddmForm, createTextDDMFormField("Metadata1"),
			createTextDDMFormField("Metadata2"));

		DDMStructure ddmStructure = createStructure("Test Structure", ddmForm);

		Field metadata1Field = createField(
			ddmStructure.getStructureId(), "Metadata1",
			createValuesList("Metadata 1 Value"),
			createValuesList("Metadata 1 Valor"));
		Field metadata2Field = createField(
			ddmStructure.getStructureId(), "Metadata2",
			createValuesList("Metadata 2 Value"),
			createValuesList("Metadata 2 Valor"));

		Fields fields = createFields(metadata1Field, metadata2Field);

		DDMFormValues ddmFormValues =
			FieldsToDDMFormValuesConverterUtil.convert(ddmStructure, fields);

		List<DDMFormFieldValue> ddmFormFieldValues =
			ddmFormValues.getDDMFormFieldValues();

		testDDMFormFieldValue(
			"Metadata 1 Value", "Metadata 1 Valor", ddmFormFieldValues.get(0));
		testDDMFormFieldValue(
			"Metadata 2 Value", "Metadata 2 Valor", ddmFormFieldValues.get(1));
	}

	@Test
	public void testConversionWithNestedFields() throws Exception {
		DDMForm ddmForm = createDDMForm();

		DDMFormField ddmFormField = createTextDDMFormField("Name");

		addNestedTextDDMFormFields(ddmFormField, "Phone");

		addDDMFormFields(ddmForm, ddmFormField);

		DDMStructure ddmStructure = createStructure("Test Structure", ddmForm);

		Field nameField = createField(
			ddmStructure.getStructureId(), "Name",
			createValuesList("Paul", "Joe"),
			createValuesList("Paulo", "Joao"));

		Field phoneField = createField(
			ddmStructure.getStructureId(), "Phone",
			createValuesList(
				"Paul's Phone 1", "Paul's Phone 2", "Joe's Phone 1",
				"Joe's Phone 2", "Joe's Phone 3"),
			createValuesList(
				"Telefone de Paulo 1", "Telefone de Paulo 2",
				"Telefone de Joao 1", "Telefone de Joao 2",
				"Telefone de Joao 3"));

		Field fieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Name_INSTANCE_rztm,Phone_INSTANCE_ovho,Phone_INSTANCE_krvx," +
			"Name_INSTANCE_rght,Phone_INSTANCE_latb,Phone_INSTANCE_jewp," +
			"Phone_INSTANCE_mkar");

		Fields fields = createFields(nameField, phoneField, fieldsDisplayField);

		DDMFormValues ddmFormValues =
			FieldsToDDMFormValuesConverterUtil.convert(ddmStructure, fields);

		List<DDMFormFieldValue> ddmFormFieldValues =
			ddmFormValues.getDDMFormFieldValues();

		Assert.assertEquals(2, ddmFormFieldValues.size());

		DDMFormFieldValue paulDDMFormFieldValue = ddmFormFieldValues.get(0);

		testDDMFormFieldValue("rztm", "Paul", "Paulo", paulDDMFormFieldValue);

		List<DDMFormFieldValue> paulNestedDDMFormFieldValues =
			paulDDMFormFieldValue.getNestedDDMFormFieldValues();

		Assert.assertEquals(2, paulNestedDDMFormFieldValues.size());

		testDDMFormFieldValue(
			"ovho", "Paul's Phone 1", "Telefone de Paulo 1",
			paulNestedDDMFormFieldValues.get(0));

		testDDMFormFieldValue(
			"krvx", "Paul's Phone 2", "Telefone de Paulo 2",
			paulNestedDDMFormFieldValues.get(1));

		DDMFormFieldValue joeDDMFormFieldValue = ddmFormFieldValues.get(1);

		testDDMFormFieldValue("rght", "Joe", "Joao", joeDDMFormFieldValue);

		List<DDMFormFieldValue> joeNestedDDMFormFieldValues =
			joeDDMFormFieldValue.getNestedDDMFormFieldValues();

		Assert.assertEquals(3, joeNestedDDMFormFieldValues.size());

		testDDMFormFieldValue(
			"latb", "Joe's Phone 1", "Telefone de Joao 1",
			joeNestedDDMFormFieldValues.get(0));
		testDDMFormFieldValue(
			"jewp", "Joe's Phone 2", "Telefone de Joao 2",
			joeNestedDDMFormFieldValues.get(1));
		testDDMFormFieldValue(
			"mkar", "Joe's Phone 3", "Telefone de Joao 3",
			joeNestedDDMFormFieldValues.get(2));
	}

	@Test
	public void testConversionWithRepeatableField() throws Exception {
		DDMForm ddmForm = createDDMForm();

		addDDMFormFields(
			ddmForm, createTextDDMFormField("Name", "", true, true, false));

		DDMStructure ddmStructure = createStructure("Test Structure", ddmForm);

		Field nameField = createField(
			ddmStructure.getStructureId(), "Name",
			createValuesList("Name 1", "Name 2", "Name 3"),
			createValuesList("Nome 1", "Nome 2", "Nome 3"));

		Field fieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Name_INSTANCE_rztm,Name_INSTANCE_ovho,Name_INSTANCE_iubr");

		Fields fields = createFields(nameField, fieldsDisplayField);

		DDMFormValues ddmFormValues =
			FieldsToDDMFormValuesConverterUtil.convert(ddmStructure, fields);

		List<DDMFormFieldValue> ddmFormFieldValues =
			ddmFormValues.getDDMFormFieldValues();

		Assert.assertEquals(3, ddmFormFieldValues.size());

		testDDMFormFieldValue(
			"rztm", "Name 1", "Nome 1", ddmFormFieldValues.get(0));
		testDDMFormFieldValue(
			"ovho", "Name 2", "Nome 2", ddmFormFieldValues.get(1));
		testDDMFormFieldValue(
			"iubr", "Name 3", "Nome 3", ddmFormFieldValues.get(2));
	}

	@Test
	public void testConversionWithSimpleField() throws Exception {
		DDMForm ddmForm = createDDMForm();

		addDDMFormFields(
			ddmForm, createTextDDMFormField("Title"),
			createTextDDMFormField("Content"));

		DDMStructure ddmStructure = createStructure("Test Structure", ddmForm);

		Field titleField = createField(
			ddmStructure.getStructureId(), "Title",
			createValuesList("Title Example"),
			createValuesList("Titulo Exemplo"));

		Field contentField = createField(
			ddmStructure.getStructureId(), "Content",
			createValuesList("Content Example"),
			createValuesList("Conteudo Exemplo"));

		Field fieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Title_INSTANCE_rztm,Content_INSTANCE_ovho");

		Fields fields = createFields(
			titleField, contentField, fieldsDisplayField);

		DDMFormValues ddmFormValues =
			FieldsToDDMFormValuesConverterUtil.convert(ddmStructure, fields);

		List<DDMFormFieldValue> ddmFormFieldValues =
			ddmFormValues.getDDMFormFieldValues();

		Assert.assertEquals(2, ddmFormFieldValues.size());

		testDDMFormFieldValue(
			"rztm", "Title Example", "Titulo Exemplo",
			ddmFormFieldValues.get(0));
		testDDMFormFieldValue(
			"ovho", "Content Example", "Conteudo Exemplo",
			ddmFormFieldValues.get(1));
	}

	protected void setUpDDMUtil() {
		DDMUtil ddmUtil = new DDMUtil();

		ddmUtil.setDDM(new DDMImpl());
	}

	protected void setUpFieldsToDDMFormValuesConverterUtil() {
		FieldsToDDMFormValuesConverterUtil fieldsToDDMFormValuesConverterUtil =
			new FieldsToDDMFormValuesConverterUtil();

		fieldsToDDMFormValuesConverterUtil.setFieldsToDDMFormValuesConverter(
			new FieldsToDDMFormValuesConverterImpl());
	}

	protected void testDDMFormFieldValue(
		String expectedEnValue, String expectedPtValue,
		DDMFormFieldValue ddmFormFieldValue) {

		Value value = ddmFormFieldValue.getValue();

		Assert.assertEquals(expectedEnValue, value.getString(LocaleUtil.US));
		Assert.assertEquals(
			expectedPtValue, value.getString(LocaleUtil.BRAZIL));
	}

	protected void testDDMFormFieldValue(
		String expectedInstanceId, String expectedEnValue,
		String expectedPtValue, DDMFormFieldValue ddmFormFieldValue) {

		Assert.assertEquals(
			expectedInstanceId, ddmFormFieldValue.getInstanceId());

		testDDMFormFieldValue(
			expectedEnValue, expectedPtValue, ddmFormFieldValue);
	}

}