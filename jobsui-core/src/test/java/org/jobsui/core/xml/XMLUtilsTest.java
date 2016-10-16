package org.jobsui.core.xml;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by enrico on 10/16/16.
 */
public class XMLUtilsTest {

    @Test
    public void indentation_of_first_non_empty_line_is_removed_form_all_lines() throws Exception {
        String script = "\n" +
                "    some = '';\n" +
                "    if (1 == 1) {\n" +
                "        doIt();\n" +
                "    }\n";
        String editForm = XMLUtils.scriptToEditForm(script);

        String expected = "some = '';\n" +
                "if (1 == 1) {\n" +
                "    doIt();\n" +
                "}\n";
        assertEquals(expected, editForm);
    }

    @Test
    public void trailing_empty_lines_are_removed() throws Exception {
        String script = "\n" +
                "    some = '';\n" +
                "    if (1 == 1) {\n" +
                "        doIt();\n" +
                "    }\n" +
                "\n";
        String editForm = XMLUtils.scriptToEditForm(script);

        String expected = "some = '';\n" +
                "if (1 == 1) {\n" +
                "    doIt();\n" +
                "}\n";
        assertEquals(expected, editForm);
    }

    @Test
    public void empty_script() throws Exception {
        String script = "";
        String editForm = XMLUtils.scriptToEditForm(script);

        assertEquals("", editForm);
    }

    @Test
    public void only_spaces_script() throws Exception {
        String script = "   ";
        String editForm = XMLUtils.scriptToEditForm(script);

        assertEquals("", editForm);
    }

    @Test
    public void only_empty_lines() throws Exception {
        String script = "   \n" +
                "    \n";
        String editForm = XMLUtils.scriptToEditForm(script);

        assertEquals("", editForm);
    }

    @Test
    public void null_script_is_null() throws Exception {
        String editForm = XMLUtils.scriptToEditForm(null);

        assertNull(editForm);
    }
}