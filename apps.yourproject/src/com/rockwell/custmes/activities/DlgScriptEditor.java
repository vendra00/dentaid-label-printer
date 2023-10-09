package com.rockwell.custmes.activities;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JDialog;

import com.datasweep.compatibility.scripteditor.ScriptEditor;

/**
 * 
 *
 * @author RWeingar
 */
public class DlgScriptEditor extends JDialog {

    /**
     * Comment for <code>EDITOR_HEIGTH</code>
     */
    private static final int EDITOR_HEIGTH = 600;

    /**
     * Comment for <code>EDITOR_WIDTH</code>
     */
    private static final int EDITOR_WIDTH = 800;

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    /**
     * Comment for <code>editor</code>
     */
    private ScriptEditor editor = new ScriptEditor();

    /**
     * 
     */
    public DlgScriptEditor() {
        setSize(EDITOR_WIDTH, EDITOR_HEIGTH);
        setModal(true);
        setTitle("Script editor");

        Container content = getContentPane();

        content.setLayout(new BorderLayout());
        content.add(editor);
    }

    /**
     * @param script
     */
    public void setScript(String script) {
        editor.setScript(script, null, null);
    }

    /**
     * @return
     */
    public String getScript() {
        return editor.getScript();
    }
}
