package gov.noaa.esrl.psd.repository.data.model;


import java.io.File;

import org.ramadda.geodata.model.ClimateModelFileHarvester;
import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.harvester.FileInfo;
import org.ramadda.repository.type.TypeHandler;
import org.w3c.dom.Element;

public class CMIP5ModelFileHarvester extends ClimateModelFileHarvester {

    public CMIP5ModelFileHarvester(Repository repository, String id)
            throws Exception {
        super(repository, id);
    }

    public CMIP5ModelFileHarvester(Repository repository, Element element)
            throws Exception {
        super(repository, element);
    }
    /**
     * Get the TypeHandler class
     *
     * @return  the class
     */
    public Class getTypeHandlerClass() {
        return CMIP5ModelFileTypeHandler.class;
    }

    /**
     * Get the type handler
     *
     * @return  the TypeHandler or null
     *
     * @throws Exception  can't find type handler
     */
    public TypeHandler getTypeHandler() throws Exception {
        return getRepository()
            .getTypeHandler(CMIP5ModelFileTypeHandler
                .TYPE_CMIP5_MODEL_FILE);
    }


    /**
     * harvester description
     *
     * @return harvester description
     */
    public String getDescription() {
        return "CMIP5 Model File";
    }

    /**
     * Check for a .properties file that corresponds to the given data file.
     * If it exists than add it to the entry
     *
     * @param fileInfo File information
     * @param originalFile Data file
     * @param entry New entry
     *
     * @return The entry
     */
    @Override
    public Entry initializeNewEntry(FileInfo fileInfo, File originalFile,
                                    Entry entry) {
        try {
            if (entry.getTypeHandler()
                    instanceof CMIP5ModelFileTypeHandler) {
                ((CMIP5ModelFileTypeHandler) entry.getTypeHandler())
                    .initializeEntry(entry);
            }
            return entry;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }


}
