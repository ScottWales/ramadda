package gov.noaa.esrl.psd.repository.data.model;

import org.ramadda.geodata.model.ClimateModelFileTypeHandler;
import org.ramadda.repository.Repository;
import org.w3c.dom.Element;

public class NOAAClimateModelFileTypeHandler extends
        ClimateModelFileTypeHandler {

    public NOAAClimateModelFileTypeHandler(Repository repository,
            Element entryNode) throws Exception {
        super(repository, entryNode);
        // TODO Auto-generated constructor stub
    }

}
