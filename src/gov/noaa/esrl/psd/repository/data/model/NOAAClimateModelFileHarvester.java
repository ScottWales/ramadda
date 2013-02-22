/*
* Copyright 2008-2013 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/
package gov.noaa.esrl.psd.repository.data.model;


import org.ramadda.geodata.model.ClimateModelFileHarvester;

import org.ramadda.repository.Entry;
import org.ramadda.repository.Repository;
import org.ramadda.repository.harvester.FileInfo;
import org.ramadda.repository.harvester.PatternHarvester;
import org.ramadda.repository.type.TypeHandler;

import org.w3c.dom.Element;

import java.io.File;

import java.util.regex.Matcher;


/**
 * Class description
 *
 *
 */
public class NOAAClimateModelFileHarvester extends ClimateModelFileHarvester {

    /**
     * Construct a new NOAAClimateModelFileHarvester
     *
     * @param repository  the Repository
     * @param id          the id
     *
     * @throws Exception problem creating harvester
     */
    public NOAAClimateModelFileHarvester(Repository repository, String id)
            throws Exception {
        super(repository, id);
    }

    /**
     * Create a new NOAAClimateModelFileHarvester from the XML
     *
     * @param repository  the Repository
     * @param element     the XML declaration
     *
     * @throws Exception  problem creating the harvester
     */
    public NOAAClimateModelFileHarvester(Repository repository,
                                         Element element)
            throws Exception {
        super(repository, element);
    }

    /**
     * Get the TypeHandler class
     *
     * @return  the class
     */
    public Class getTypeHandlerClass() {
        return NOAAClimateModelFileTypeHandler.class;
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
            .getTypeHandler(NOAAClimateModelFileTypeHandler
                .TYPE_NOAA_FACTS_CLIMATE_MODELFILE);
    }


    /**
     * harvester description
     *
     * @return harvester description
     */
    public String getDescription() {
        return "NOAA Climate Model File";
    }

    /**
     * Should this harvester harvest the given file
     *
     * @param fileInfo file information
     * @param f the actual file
     * @param matcher pattern matcher
     * @param originalFile  the original file
     * @param entry   the Entry
     *
     * @return the new entry or null if nothing is harvested
     *
     * @throws Exception on badness
     * @Override
     * public Entry harvestFile(FileInfo fileInfo, File f, Matcher matcher)
     *       throws Exception {
     *   if (!f.toString().endsWith(".nc")) {
     *       return null;
     *   }
     *   return super.harvestFile(fileInfo, f, matcher);
     * }
     */



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
            if (entry.getTypeHandler()
                    instanceof NOAAClimateModelFileTypeHandler) {
                ((NOAAClimateModelFileTypeHandler) entry.getTypeHandler())
                    .initializeEntry(entry);
            }

            //getRepository().getLogManager().logInfo(
            //    "ClimateModelFileHarvester:initializeNewEntry done");
            return entry;
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

}
