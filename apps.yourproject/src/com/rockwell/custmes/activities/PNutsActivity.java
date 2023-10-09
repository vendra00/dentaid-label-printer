package com.rockwell.custmes.activities;

import org.apache.commons.lang3.StringUtils;

import pnuts.ext.CompositePackage;
import pnuts.lang.Context;
import pnuts.lang.Package;
import pnuts.lang.Pnuts;

import com.datasweep.compatibility.client.Response;
import com.datasweep.compatibility.pnuts.Environment;
import com.rockwell.activity.ItemDescriptor;
import com.rockwell.activityset.TransitionFunctions;
import com.rockwell.mes.clientfw.commons.ifc.view.ActivitySupport;


public class PNutsActivity extends ActivitySupport {

    public static final String CONFIG_INPUTS = "inputSpecification";
    public static final String CONFIG_OUTPUTS = "outputSpecification";
    public static final String CONFIG_SCRIPT = "script";
    public static final String OUTPUT_RETURN_VALUE = "returnValue";

    public static final String TEMPLATE_PNUTS_START = "// Automatically generated code - start";
    public static final String TEMPLATE_PNUTS_END = "// Automatically generated code - end\n";
    private boolean configLoaded = false;


    @Override
    protected Response doWork() {
        String script = (String) getConfigurationItem(CONFIG_SCRIPT);
        if (script == null) {
            return createErrorResponse(new Exception("Script undefined"));
        }
        Context rootContext = getParent().getEnvironmentGeneral().getRootContext();
        Package pack = new CompositePackage(rootContext.getCurrentPackage());
        Context cont = new Context(rootContext);
        cont.setClassLoader(Environment.getCustomClassLoader());
        cont.setCurrentPackage(pack);

        TransitionFunctions.register(getParent().getEnvironmentGeneral(), cont, pack);
        try {

            for (PNutsDescriptor desc : getInputs().getParameters()) {
                String key = desc.getName().intern();
                pack.set(key, getInputItem(key));
            }
            Object retValue = Pnuts.eval(script, cont);

            for (PNutsDescriptor desc : getOutputs().getParameters()) {
                String key = desc.getName().intern();
                Object outValue = pack.get(key);
                setOutputItem(key, outValue);
            }

            setOutputItem(OUTPUT_RETURN_VALUE, retValue);
        } catch (Exception exc) {
            return createErrorResponse(exc);
        }

        return new Response();
    }

    @Override
    public String getActivityDescription() {
        return "Executes the given PNuts script";
    }

    /**
     * @return
     */
    public String getSpecificationInPNuts() {
        StringBuilder bld = new StringBuilder();
        bld.append(TEMPLATE_PNUTS_START);
        bld.append("\n// Input specification: Following variables are available in the script\n");
        for (PNutsDescriptor desc : getInputs().getParameters()) {
            bld.append("// @inputparam ");
            bld.append(desc.getName());
            bld.append("(");
            bld.append(desc.getType().getSimpleName());
            bld.append(")\n");
        }
        bld.append("//\n// Output specification: Following variables need to be assigned within the script\n");
        for (PNutsDescriptor desc : getOutputs().getParameters()) {
            bld.append("// @outputparam ");
            bld.append(desc.getName());
            bld.append("(");
            bld.append(desc.getType().getSimpleName());
            bld.append(")\n");
        }
        bld.append("//\n");

        Class<?> cls = getOutputs().getReturnValue();
        if (cls == null) {
            bld.append("// No return value is expected\n\n");
        } else {
            bld.append("// @return returnValue(");
            bld.append(cls.getSimpleName());
            bld.append(")\n//\n");
        }

        bld.append(TEMPLATE_PNUTS_END);
        return bld.toString();
    }

    public void updateScript() {
        if (!configLoaded) {
            return;
        }

        String script = StringUtils.defaultString((String) getConfigurationItem(CONFIG_SCRIPT));
        int endPos = script.indexOf(TEMPLATE_PNUTS_END);
        int pos = 0;
        boolean ok = true;
        while (pos < endPos) {
            String part = script.substring(pos);
            int lineEnd = part.indexOf('\n');
            if (lineEnd >= 0) {
                part = part.substring(0, lineEnd);
            }
            part = part.trim();
            if (!part.startsWith("//") && part.length() > 0) {
                ok = false;
            }
            pos = script.indexOf('\n', pos) + 1;
        }

        if (ok && endPos >= 0) {
            script = script.substring(endPos + TEMPLATE_PNUTS_END.length());
        }
        StringBuilder scriptName = new StringBuilder(getSpecificationInPNuts());
        scriptName.append(script);
        setConfigurationItem(CONFIG_SCRIPT, scriptName);
    }

    @Override
    protected void configurationLoaded() {
        super.configurationLoaded();
        // now we assume, that configuration changes come from the user. This
        // means the script stub has to be updated. When input/output
        // specification changes.
        configLoaded = true;
    }

    @Override
    protected void configurationItemSet(String key, Object value) {
        super.configurationItemSet(key, value);
        if (CONFIG_INPUTS.equals(key) || CONFIG_OUTPUTS.equals(key)) {
            updateScript();
        }
    }

    @Override
    public ItemDescriptor[] configurationDescriptors() {
        ItemDescriptor[] descs = new ItemDescriptor[] {
                ItemDescriptor.createItemDescriptor(getClass(), CONFIG_INPUTS, PNutsInputDescriptors.class,
                        new Object[] { ItemDescriptor.DISPLAYNAME, "Input specification" }),
                ItemDescriptor.createItemDescriptor(getClass(), CONFIG_OUTPUTS, PNutsOutputDescriptors.class,
                        new Object[] { ItemDescriptor.DISPLAYNAME, "Output specification" }),
                ItemDescriptor.createItemDescriptor(getClass(), CONFIG_SCRIPT, String.class, new Object[] {
                        ItemDescriptor.DISPLAYNAME, "Script", ItemDescriptor.PROPERTYEDITORCLASS,
                        ScriptPropertyEditor.class }) };
        return descs;
    }

    public PNutsInputDescriptors getInputs() {
        Object obj = getConfigurationItem(CONFIG_INPUTS);
        if (obj instanceof PNutsInputDescriptors) {
            return (PNutsInputDescriptors) obj;
        }

        return new PNutsInputDescriptors();
    }

    public PNutsOutputDescriptors getOutputs() {
        Object obj = getConfigurationItem(CONFIG_OUTPUTS);
        if (obj instanceof PNutsOutputDescriptors) {
            return (PNutsOutputDescriptors) obj;
        }

        return new PNutsOutputDescriptors();
    }


    @Override
    public ItemDescriptor[] inputDescriptors() {
        PNutsInputDescriptors pnutsDescs = getInputs();
        ItemDescriptor[] descs = new ItemDescriptor[pnutsDescs.getParameters().size()];
        for (int idx = 0; idx < descs.length; idx++) {
            PNutsDescriptor param = pnutsDescs.getParameters().get(idx);
            descs[idx] = ItemDescriptor.createItemDescriptor(getClass(), param.getName(), param.getType(),
                    new Object[] {});
        }
        return descs;
    }

    @Override
    public ItemDescriptor[] outputDescriptors() {
        PNutsOutputDescriptors pnutsDescs = getOutputs();
        int size = pnutsDescs.getParameters().size();
        ItemDescriptor[] descs;

        Class<?> cls = pnutsDescs.getReturnValue();
        if (cls != null) {
            descs = new ItemDescriptor[size + 1];
            ItemDescriptor retValue = ItemDescriptor.createItemDescriptor(getClass(), OUTPUT_RETURN_VALUE, cls,
                    new Object[] {});

            descs[size] = retValue;
        } else {
            descs = new ItemDescriptor[size];
        }

        for (int idx = 0; idx < size; idx++) {
            PNutsDescriptor param = pnutsDescs.getParameters().get(idx);
            descs[idx] = ItemDescriptor.createItemDescriptor(getClass(), param.getName(), param.getType(),
                    new Object[] {});
        }
        return descs;
    }
}
