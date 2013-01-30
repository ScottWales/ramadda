package gov.noaa.esrl.psd.repository.data.model;

import java.io.File;
import java.util.regex.Matcher;

import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.harvester.FileInfo;
import org.ramadda.repository.harvester.PatternHarvester;
import org.ramadda.repository.type.TypeHandler;
import org.w3c.dom.Element;

public class ClimateModelFileHarvester extends PatternHarvester {

    public ClimateModelFileHarvester(Repository repository, String id)
            throws Exception {
        super(repository, id);
    }

    public ClimateModelFileHarvester(Repository repository, Element element)
            throws Exception {
        super(repository, element);
     }

    public Class getTypeHandlerClass(){
        return ClimateModelFileTypeHandler.class;
    }

    public TypeHandler getTypeHandler() throws Exception {
        return  getRepository().getTypeHandler(ClimateModelFileTypeHandler.TYPE_CLIMATE_MODELFILE);
    }


    /**
     * harvester description
     *
     * @return harvester description
     */
    public String getDescription() {
        return "Climate Model File";
    }

    /**
     * Should this harvester harvest the given file
     *
     * @param fileInfo file information
     * @param f the actual file
     * @param matcher pattern matcher
     *
     * @return the new entry or null if nothing is harvested
     *
     * @throws Exception on badness
     */
    @Override
    public Entry harvestFile(FileInfo fileInfo, File f, Matcher matcher)
            throws Exception {
        if (!f.toString().endsWith(".nc")) {
            return null;
        }
        return super.harvestFile(fileInfo, f, matcher);
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
            //getRepository().getLogManager().logInfo(
            //    "ClimateModelFileHarvester:initializeNewEntry:"
            //    + entry.getResource());
            if (entry.getTypeHandler() instanceof ClimateModelFileTypeHandler) {
                ((ClimateModelFileTypeHandler) entry.getTypeHandler()).initializeEntry(entry);
            }
            //getRepository().getLogManager().logInfo(
            //    "ClimateModelFileHarvester:initializeNewEntry done");
            return entry;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

}
