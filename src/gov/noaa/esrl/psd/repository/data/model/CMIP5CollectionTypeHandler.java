package gov.noaa.esrl.psd.repository.data.model;

import org.ramadda.geodata.model.ClimateCollectionTypeHandler;
import org.ramadda.repository.Repository;
import org.w3c.dom.Element;

public class CMIP5CollectionTypeHandler extends
        ClimateCollectionTypeHandler {

    public CMIP5CollectionTypeHandler(Repository repository,
            Element entryNode) throws Exception {
        super(repository, entryNode);
    }

}
