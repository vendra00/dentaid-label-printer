package com.rockwell.custmes.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PNutsInputDescriptors implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<PNutsDescriptor> parameters = new ArrayList<PNutsDescriptor>();

    /**
     * @return the parameters
     */
    public List<PNutsDescriptor> getParameters() {
        return parameters;
    }

    /**
     * @param parameters
     *            the parameters to set
     */
    public void setParameters(List<PNutsDescriptor> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        if (getParameters().isEmpty()) {
            bld.append("void");

        } else {
            for (int idx = 0; idx < getParameters().size(); idx++) {
                if (idx > 0) {
                    bld.append(',');
                }
                bld.append(getParameters().get(idx).toString());
            }
        }
        return bld.toString();
    }
}
