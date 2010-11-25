/*
 * Copyright 1997-2010 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * Copyright 2010- ramadda.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */
package org.ramadda.ontology;



/**
 * Interface description
 *
 *
 * @author         Enter your name here...    
 */
public interface SweetTags {

    /** _more_          */
    public static final String TAG_RDF_RDF = "rdf:RDF";

    /** _more_          */
    public static final String TAG_OWL_ONTOLOGY = "owl:Ontology";

    /** _more_          */
    public static final String TAG_OWL_IMPORTS = "owl:imports";

    /** _more_          */
    public static final String TAG_OWL_CLASS = "owl:Class";

    /** _more_          */
    public static final String TAG_RDFS_SUBCLASSOF = "rdfs:subClassOf";

    /** _more_          */
    public static final String TAG_OWL_DISJOINTWITH = "owl:disjointWith";

    /** _more_          */
    public static final String TAG_OWL_OBJECTPROPERTY = "owl:ObjectProperty";

    /** _more_          */
    public static final String TAG_OWL_RESTRICTION = "owl:Restriction";

    /** _more_          */
    public static final String TAG_OWL_ALLVALUESFROM = "owl:allValuesFrom";

    /** _more_          */
    public static final String TAG_OWL_ONPROPERTY = "owl:onProperty";

    /** _more_          */
    public static final String TAG_OWL_EQUIVALENTCLASS =
        "owl:equivalentClass";

    /** _more_          */
    public static final String TAG_RDFS_SUBPROPERTYOF = "rdfs:subPropertyOf";

    /** _more_          */
    public static final String TAG_RDFS_RANGE = "rdfs:range";

    /** _more_          */
    public static final String TAG_RDFS_COMMENT = "rdfs:comment";

    /** _more_          */
    public static final String TAG_OWL_HASVALUE = "owl:hasValue";

    /** _more_          */
    public static final String TAG_OWL_MINCARDINALITY = "owl:minCardinality";

    /** _more_          */
    public static final String TAG_STAN_ENVIRONMENTALSTANDARDSBODY =
        "stan:EnvironmentalStandardsBody";

    /** _more_          */
    public static final String TAG_OWL_SAMEAS = "owl:sameAs";

    /** _more_          */
    public static final String TAG_JUR_TREATY = "jur:Treaty";

    /** _more_          */
    public static final String TAG_STAN_ENVIRONMENTALSTANDARD =
        "stan:EnvironmentalStandard";

    /** _more_          */
    public static final String TAG_STAN_ENVIRONMENTALLAW =
        "stan:EnvironmentalLaw";

    /** _more_          */
    public static final String TAG_RDFS_DOMAIN = "rdfs:domain";

    /** _more_          */
    public static final String TAG_OWL_EQUIVALENTPROPERTY =
        "owl:equivalentProperty";

    /** _more_          */
    public static final String TAG_OWL_INVERSEOF = "owl:inverseOf";

    /** _more_          */
    public static final String TAG_OWL_DATATYPEPROPERTY =
        "owl:DatatypeProperty";

    /** _more_          */
    public static final String TAG_OWL_CARDINALITY = "owl:cardinality";

    /** _more_          */
    public static final String TAG_JUR2_FEDERALGOVERNINGBODY =
        "jur2:FederalGoverningBody";

    /** _more_          */
    public static final String TAG_XTEN2_SIZERANGE = "xten2:SizeRange";

    /** _more_          */
    public static final String TAG_UNITS2_HASUNIT = "units2:hasUnit";

    /** _more_          */
    public static final String TAG_MATH2_HASUPPERBOUND =
        "math2:hasUpperBound";

    /** _more_          */
    public static final String TAG_COMP_INORGANICCOMPOUND =
        "comp:InorganicCompound";

    /** _more_          */
    public static final String TAG_CHEM_HASCHEMICAL_1 = "chem:hasChemical_1";

    /** _more_          */
    public static final String TAG_CHEM_HASCHEMICAL_2 = "chem:hasChemical_2";

    /** _more_          */
    public static final String TAG_CHEM_HASCHEMICAL_3 = "chem:hasChemical_3";

    /** _more_          */
    public static final String TAG_CHEM_HASCHEMICAL_4 = "chem:hasChemical_4";

    /** _more_          */
    public static final String TAG_RDF_TYPE = "rdf:type";

    /** _more_          */
    public static final String TAG_COMP_INORGANICACID = "comp:InorganicAcid";

    /** _more_          */
    public static final String TAG_ELEM_ELEMENT = "elem:Element";

    /** _more_          */
    public static final String TAG_ELEM_HASPROTONS = "elem:hasProtons";

    /** _more_          */
    public static final String TAG_ELEM_HASBASENEUTRONS =
        "elem:hasBaseNeutrons";

    /** _more_          */
    public static final String TAG_ALLO_ALLOTROPE = "allo:Allotrope";

    /** _more_          */
    public static final String TAG_ELEM_HASELEMENT = "elem:hasElement";

    /** _more_          */
    public static final String TAG_CHEM_HASATOMS = "chem:hasAtoms";

    /** _more_          */
    public static final String TAG_ION_ION = "ion:Ion";

    /** _more_          */
    public static final String TAG_CHARGE_HASCHARGE = "charge:hasCharge";

    /** _more_          */
    public static final String TAG_ALLO_HASALLOTROPE = "allo:hasAllotrope";

    /** _more_          */
    public static final String TAG_ISOT_ISOTOPE = "isot:Isotope";

    /** _more_          */
    public static final String TAG_CHEM_HASNEUTRONS = "chem:hasNeutrons";

    /** _more_          */
    public static final String TAG_ORGA_ORGANICCOMPOUND =
        "orga:OrganicCompound";

    /** _more_          */
    public static final String TAG_ROLE_HASCHEMICALROLE =
        "role:hasChemicalRole";

    /** _more_          */
    public static final String TAG_CHEM_HASCHEMICAL_5 = "chem:hasChemical_5";

    /** _more_          */
    public static final String TAG_CHEM_HASCHEMICAL_6 = "chem:hasChemical_6";

    /** _more_          */
    public static final String TAG_ORGA_CHLOROPHYLL = "orga:Chlorophyll";

    /** _more_          */
    public static final String TAG_ORGA_HALON = "orga:Halon";

    /** _more_          */
    public static final String TAG_ORGA_CFC = "orga:CFC";

    /** _more_          */
    public static final String TAG_ORGA_HCFC = "orga:HCFC";

    /** _more_          */
    public static final String TAG_ORGA_HYDROCARBON = "orga:Hydrocarbon";

    /** _more_          */
    public static final String TAG_OWL_DIFFERENTFROM = "owl:differentFrom";

    /** _more_          */
    public static final String TAG_OWL_UNIONOF = "owl:unionOf";

    /** _more_          */
    public static final String TAG_RDF_DESCRIPTION = "rdf:Description";

    /** _more_          */
    public static final String TAG_OWL_SOMEVALUESFROM = "owl:someValuesFrom";

    /** _more_          */
    public static final String TAG_RDFS_LABEL = "rdfs:label";

    /** _more_          */
    public static final String TAG_OWL_ANNOTATIONPROPERTY =
        "owl:AnnotationProperty";

    /** _more_          */
    public static final String TAG_ODYN_GYRE = "odyn:Gyre";

    /** _more_          */
    public static final String TAG_ODYN_EARTHOCEANCURRENT =
        "odyn:EarthOceanCurrent";

    /** _more_          */
    public static final String TAG_ODYN_EARTHWESTERNBOUNDARYCURRENT =
        "odyn:EarthWesternBoundaryCurrent";

    /** _more_          */
    public static final String TAG_ASTRO2_STAR = "astro2:Star";

    /** _more_          */
    public static final String TAG_RATIO_DIMENSIONLESSRATIO =
        "ratio:DimensionlessRatio";

    /** _more_          */
    public static final String TAG_SOLU2_APPROXIMATES = "solu2:approximates";

    /** _more_          */
    public static final String TAG_FUNC2_HASTHRESHOLD = "func2:hasThreshold";

    /** _more_          */
    public static final String TAG_EFLUX_RADIATIVEFLUX =
        "eflux:RadiativeFlux";

    /** _more_          */
    public static final String TAG_COMP2_HASSOURCE = "comp2:hasSource";

    /** _more_          */
    public static final String TAG_XTEN2_HEIGHTRANGE = "xten2:HeightRange";

    /** _more_          */
    public static final String TAG_MATH2_HASLOWERBOUND =
        "math2:hasLowerBound";

    /** _more_          */
    public static final String TAG_AVER_MEANANNUALTEMPERATURE =
        "aver:MeanAnnualTemperature";

    /** _more_          */
    public static final String TAG_MATH_HASLOWERBOUND = "math:hasLowerBound";

    /** _more_          */
    public static final String TAG_AVER_DRYSEASONPRECIPITATIONRANGE =
        "aver:DrySeasonPrecipitationRange";

    /** _more_          */
    public static final String TAG_MATH_HASUPPERBOUND = "math:hasUpperBound";

    /** _more_          */
    public static final String TAG_AVER_ITCZDOMINANCE = "aver:ITCZDominance";

    /** _more_          */
    public static final String TAG_AVER_COLDESTMONTHTEMPERATURE =
        "aver:ColdestMonthTemperature";

    /** _more_          */
    public static final String TAG_AVER_WARMESTMONTHTEMPERATURE =
        "aver:WarmestMonthTemperature";

    /** _more_          */
    public static final String TAG_AVER_WINTERDURATION =
        "aver:WinterDuration";

    /** _more_          */
    public static final String TAG_AVER_SNOWCOVERDURATION =
        "aver:SnowCoverDuration";

    /** _more_          */
    public static final String TAG_THIC_POTENTIALEVAPOTRANSPIRATION =
        "thic:PotentialEvapotranspiration";

    /** _more_          */
    public static final String TAG_THIC_HALFPOTENTIALEVAPOTRANSPIRATION =
        "thic:HalfPotentialEvapotranspiration";

    /** _more_          */
    public static final String TAG_ASTRO2_PLANET = "astro2:Planet";

    /** _more_          */
    public static final String TAG_IND2_HASPRECIPITATIONCLIMATE =
        "ind2:hasPrecipitationClimate";

    /** _more_          */
    public static final String TAG_IND2_HASITCZDOMINANCE =
        "ind2:hasITCZDominance";

    /** _more_          */
    public static final String TAG_ZONE_B = "zone:B";

    /** _more_          */
    public static final String TAG_IND2_HASANNUALPRECIPITATIONRANGEUPPERBOUND =
        "ind2:hasAnnualPrecipitationRangeUpperBound";

    /** _more_          */
    public static final String TAG_IND2_HASANNUALPRECIPITATIONRANGELOWERBOUND =
        "ind2:hasAnnualPrecipitationRangeLowerBound";

    /** _more_          */
    public static final String TAG_IND2_HASTEMPERATURECLIMATE =
        "ind2:hasTemperatureClimate";

    /** _more_          */
    public static final String TAG_ZONE_C = "zone:C";

    /** _more_          */
    public static final String TAG_IND2_HASWINTERDURATION =
        "ind2:hasWinterDuration";

    /** _more_          */
    public static final String TAG_IND2_HASWETSEASON = "ind2:hasWetSeason";

    /** _more_          */
    public static final String TAG_ZONE_D = "zone:D";

    /** _more_          */
    public static final String TAG_ZONE_E = "zone:E";

    /** _more_          */
    public static final String TAG_IND2_HASSNOWCOVERDURATION =
        "ind2:hasSnowCoverDuration";

    /** _more_          */
    public static final String TAG_ATMO_TROPOSPHERE = "atmo:Troposphere";

    /** _more_          */
    public static final String TAG_BODY_HASPLANET = "body:hasPlanet";

    /** _more_          */
    public static final String TAG_XTEN_HASREFERENCEHEIGHT =
        "xten:hasReferenceHeight";

    /** _more_          */
    public static final String TAG_EREF_HEIGHTRANGE_KM =
        "eref:HeightRange_km";

    /** _more_          */
    public static final String TAG_ATMO_STRATOSPHERE = "atmo:Stratosphere";

    /** _more_          */
    public static final String TAG_ATMO_MESOSPHERE = "atmo:Mesosphere";

    /** _more_          */
    public static final String TAG_ATMO_THERMOSPHERE = "atmo:Thermosphere";

    /** _more_          */
    public static final String TAG_PLANET_ATMOSPHERE = "planet:Atmosphere";

    /** _more_          */
    public static final String TAG_HELI_EXOSPHERE = "heli:Exosphere";

    /** _more_          */
    public static final String TAG_DIST_DISTANCERANGE = "dist:DistanceRange";

    /** _more_          */
    public static final String TAG_UNITS_HASUNIT = "units:hasUnit";

    /** _more_          */
    public static final String TAG_OCEAN_PHOTICZONE = "ocean:PhoticZone";

    /** _more_          */
    public static final String TAG_XTEN_HASREFERENCEDEPTH =
        "xten:hasReferenceDepth";

    /** _more_          */
    public static final String TAG_EREF_DEPTHRANGE_KM = "eref:DepthRange_km";

    /** _more_          */
    public static final String TAG_OCEAN_MESOPELAGICZONE =
        "ocean:MesopelagicZone";

    /** _more_          */
    public static final String TAG_OCEAN_BATHYPELAGICZONE =
        "ocean:BathypelagicZone";

    /** _more_          */
    public static final String TAG_OCEAN_ABYSSOPELAGICZONE =
        "ocean:AbyssopelagicZone";

    /** _more_          */
    public static final String TAG_GEOL_CORE = "geol:Core";

    /** _more_          */
    public static final String TAG_GEOL_UPPERMANTLE = "geol:UpperMantle";

    /** _more_          */
    public static final String TAG_PLANET_GEOSPHERE = "planet:Geosphere";

    /** _more_          */
    public static final String TAG_GEOL_LITHOSPHERE = "geol:Lithosphere";

    /** _more_          */
    public static final String TAG_GEOL_CRUST = "geol:Crust";

    /** _more_          */
    public static final String TAG_BODY2_STRAIT = "body2:Strait";

    /** _more_          */
    public static final String TAG_FEAT_EARTHOCEAN = "feat:EarthOcean";

    /** _more_          */
    public static final String TAG_XTEN_HASAVERAGEDEPTH =
        "xten:hasAverageDepth";

    /** _more_          */
    public static final String TAG_XTEN_HASMAXIMUMDEPTH =
        "xten:hasMaximumDepth";

    /** _more_          */
    public static final String TAG_DIST_HASVOLUME = "dist:hasVolume";

    /** _more_          */
    public static final String TAG_DIST_HASAREA = "dist:hasArea";

    /** _more_          */
    public static final String TAG_FEAT_OCEANDEPTH = "feat:OceanDepth";

    /** _more_          */
    public static final String TAG_MATH_HASNUMERICVALUE =
        "math:hasNumericValue";

    /** _more_          */
    public static final String TAG_FEAT_OCEANVOLUME = "feat:OceanVolume";

    /** _more_          */
    public static final String TAG_FEAT_OCEANAREA = "feat:OceanArea";

    /** _more_          */
    public static final String TAG_SREG_LATITUDELINE = "sreg:LatitudeLine";

    /** _more_          */
    public static final String TAG_SREG_HASLATITUDELINE =
        "sreg:hasLatitudeLine";

    /** _more_          */
    public static final String TAG_SREG_LATITUDEBAND = "sreg:LatitudeBand";

    /** _more_          */
    public static final String TAG_SREG_HASLATITUDEBAND =
        "sreg:hasLatitudeBand";

    /** _more_          */
    public static final String TAG_SREG_SOUTHLATITUDEBAND =
        "sreg:SouthLatitudeBand";

    /** _more_          */
    public static final String TAG_SREG_NORTHLATITUDEBAND =
        "sreg:NorthLatitudeBand";

    /** _more_          */
    public static final String TAG_SREG_SOUTHLATITUDELINE =
        "sreg:SouthLatitudeLine";

    /** _more_          */
    public static final String TAG_SREG_NORTHLATITUDELINE =
        "sreg:NorthLatitudeLine";

    /** _more_          */
    public static final String TAG_REPR_REFERENCEFRAME =
        "repr:ReferenceFrame";

    /** _more_          */
    public static final String TAG_ANAL_CLASSIFIER = "anal:Classifier";

    /** _more_          */
    public static final String TAG_ANAL_INTERPOLATIONMETHOD =
        "anal:InterpolationMethod";

    /** _more_          */
    public static final String ATTR_XMLNS = "xmlns";

    /** _more_          */
    public static final String TAG_FORM_COMPRESSIONMETHOD =
        "form:CompressionMethod";

    /** _more_          */
    public static final String TAG_FORM_BYTEORDER = "form:ByteOrder";

    /** _more_          */
    public static final String TAG_FORM_FORMAT = "form:Format";

    /** _more_          */
    public static final String TAG_FORM_SELFDESCRIBINGFORMAT =
        "form:SelfDescribingFormat";

    /** _more_          */
    public static final String TAG_SOLU2_BASEDON = "solu2:basedOn";

    /** _more_          */
    public static final String TAG_SERV_INTERFACEPROTOCOL =
        "serv:InterfaceProtocol";

    /** _more_          */
    public static final String TAG_COMP_COMPONENT = "comp:Component";

    /** _more_          */
    public static final String TAG_METH_RETRIEVALAPPROACH =
        "meth:RetrievalApproach";

    /** _more_          */
    public static final String TAG_UNITS_PREFIX = "units:Prefix";

    /** _more_          */
    public static final String TAG_MATH2_HASNUMERICVALUE =
        "math2:hasNumericValue";

    /** _more_          */
    public static final String TAG_UNITS_HASSYMBOL = "units:hasSymbol";

    /** _more_          */
    public static final String TAG_UNITS_BASEUNIT = "units:BaseUnit";

    /** _more_          */
    public static final String TAG_UNITS_UNITDERIVEDBYRAISINGTOPOWER =
        "units:UnitDerivedByRaisingToPower";

    /** _more_          */
    public static final String TAG_UNITS_HASBASEUNIT = "units:hasBaseUnit";

    /** _more_          */
    public static final String TAG_OPER2_TOTHEPOWER = "oper2:toThePower";

    /** _more_          */
    public static final String TAG_UNITS_UNITDERIVEDBYSCALING =
        "units:UnitDerivedByScaling";

    /** _more_          */
    public static final String TAG_UNITS_HASPREFIX = "units:hasPrefix";

    /** _more_          */
    public static final String TAG_UNITS_HASSCALINGNUMBER =
        "units:hasScalingNumber";

    /** _more_          */
    public static final String TAG_UNITS_UNITDERIVEDBYSHIFTING =
        "units:UnitDerivedByShifting";

    /** _more_          */
    public static final String TAG_UNITS_HASSHIFTINGNUMBER =
        "units:hasShiftingNumber";

    /** _more_          */
    public static final String TAG_UNITS_UNITDEFINEDBYPRODUCT =
        "units:UnitDefinedByProduct";

    /** _more_          */
    public static final String TAG_MATH2_HASOPERAND = "math2:hasOperand";

    /** _more_          */
    public static final String TAG_UNITS_UNIT = "units:Unit";

    /** _more_          */
    public static final String TAG_COORD_HORIZONTALCOORDINATE =
        "coord:HorizontalCoordinate";

    /** _more_          */
    public static final String TAG_DIR2_HASDIRECTION = "dir2:hasDirection";

    /** _more_          */
    public static final String TAG_COORD_VERTICALCOORDINATE =
        "coord:VerticalCoordinate";

    /** _more_          */
    public static final String TAG_COORD_ANGULARCOORDINATE =
        "coord:AngularCoordinate";

    /** _more_          */
    public static final String TAG_MDIR2_ANGULARDIRECTION =
        "mdir2:AngularDirection";

    /** _more_          */
    public static final String TAG_MDIR2_HORIZONTALDIRECTION =
        "mdir2:HorizontalDirection";

    /** _more_          */
    public static final String TAG_MDIR2_VERTICALDIRECTION =
        "mdir2:VerticalDirection";

    /** _more_          */
    public static final String TAG_MDIR2_DIRECTION = "mdir2:Direction";

    /** _more_          */
    public static final String TAG_REL2_OPPOSITETO = "rel2:oppositeTo";

    /** _more_          */
    public static final String TAG_REL2_ORTHOGONALTO = "rel2:orthogonalTo";

    /** _more_          */
    public static final String TAG_REL2_PERPENDICULARTO =
        "rel2:perpendicularTo";

    /** _more_          */
    public static final String TAG_SRS_SPATIALREFERENCESYSTEM =
        "srs:SpatialReferenceSystem";

    /** _more_          */
    public static final String TAG_REPR2_COORDINATE_1 = "repr2:coordinate_1";

    /** _more_          */
    public static final String TAG_REPR2_COORDINATE_2 = "repr2:coordinate_2";

    /** _more_          */
    public static final String TAG_REPR2_COORDINATE_3 = "repr2:coordinate_3";

    /** _more_          */
    public static final String TAG_SRS_HORIZONTALCOORDINATESYSTEM =
        "srs:HorizontalCoordinateSystem";

    /** _more_          */
    public static final String TAG_SRS_POLARCOORDINATES =
        "srs:PolarCoordinates";

    /** _more_          */
    public static final String TAG_SCALE_SPATIALSCALE = "scale:SpatialScale";

    /** _more_          */
    public static final String TAG_SCALE_LARGERTHAN = "scale:largerThan";

    /** _more_          */
    public static final String TAG_TIME_SEASON = "time:Season";

    /** _more_          */
    public static final String TAG_OWLT_HASBEGINNING = "owlt:hasBeginning";

    /** _more_          */
    public static final String TAG_OWLT_HASEND = "owlt:hasEnd";

    /** _more_          */
    public static final String TAG_TIME_EQUINOX = "time:Equinox";

    /** _more_          */
    public static final String TAG_TIME_SOLSTICE = "time:Solstice";

    /** _more_          */
    public static final String TAG_TIME_HASNHTIME = "time:hasNHTime";

    /** _more_          */
    public static final String TAG_TIME_HASSHTIME = "time:hasSHTime";

    /** _more_          */
    public static final String TAG_TIME_HASDAYOFYEAR = "time:hasDayOfYear";

    /** _more_          */
    public static final String TAG_TIME_TIMEFRAME = "time:TimeFrame";

    /** _more_          */
    public static final String TAG_TIME_TIMEZONE = "time:TimeZone";

    /** _more_          */
    public static final String TAG_GTIME_SUPEREON = "gtime:Supereon";

    /** _more_          */
    public static final String TAG_TIME_HASENDTIME = "time:hasEndTime";

    /** _more_          */
    public static final String TAG_GTIME_EON = "gtime:Eon";

    /** _more_          */
    public static final String TAG_TIME_HASSTARTTIME = "time:hasStartTime";

    /** _more_          */
    public static final String TAG_RELA_SUBSETOF = "rela:subsetOf";

    /** _more_          */
    public static final String TAG_GTIME_ERA = "gtime:Era";

    /** _more_          */
    public static final String TAG_GTIME_PERIOD = "gtime:Period";

    /** _more_          */
    public static final String TAG_GTIME_EPOCH = "gtime:Epoch";

    /** _more_          */
    public static final String TAG_GTIME_AGE = "gtime:Age";

    /** _more_          */
    public static final String TAG_GTIME_GEOLOGICTIMEUNIT =
        "gtime:GeologicTimeUnit";

    /** _more_          */
    public static final String TAG_SOL_HASERROR = "sol:hasError";

    /** _more_          */
    public static final String TAG_TIME_DURATION = "time:Duration";

    /** _more_          */
    public static final String TAG_STATE_ORDERCATEGORY =
        "state:OrderCategory";

    /** _more_          */
    public static final String TAG_STATE_QUALIFIER = "state:Qualifier";

    /** _more_          */
    public static final String TAG_STATE_MOREEXTENSIVETHAN =
        "state:moreExtensiveThan";

    /** _more_          */
    public static final String TAG_STATE_MINERALSTATE = "state:MineralState";

    /** _more_          */
    public static final String TAG_BIOS_AGE = "bios:Age";

    /** _more_          */
    public static final String TAG_BIOS_OLDERTHAN = "bios:olderThan";

    /** _more_          */
    public static final String TAG_BIOS_BIOLOGICALSTATE =
        "bios:BiologicalState";

    /** _more_          */
    public static final String TAG_CSTATE_CHEMICALSTATE =
        "cstate:ChemicalState";

    /** _more_          */
    public static final String TAG_CSTATE_BIOCHEMICALSTATE =
        "cstate:BiochemicalState";

    /** _more_          */
    public static final String TAG_FLUI_FLUIDSTATE = "flui:FluidState";

    /** _more_          */
    public static final String TAG_FLUI_FLUIDEQUILIBRIUMSTATE =
        "flui:FluidEquilibriumState";

    /** _more_          */
    public static final String TAG_PSTATE_PHYSICALSTATE =
        "pstate:PhysicalState";

    /** _more_          */
    public static final String TAG_CHARGE2_HASCHARGE = "charge2:hasCharge";

    /** _more_          */
    public static final String TAG_PSTATE_STATEOFMATTER =
        "pstate:StateOfMatter";

    /** _more_          */
    public static final String TAG_PSTATE_EQUILIBRIUMSTATE =
        "pstate:EquilibriumState";

    /** _more_          */
    public static final String TAG_PSTATE_SUBSTANCEFORM =
        "pstate:SubstanceForm";

    /** _more_          */
    public static final String TAG_PSTATE_MOISTURESTATE =
        "pstate:MoistureState";

    /** _more_          */
    public static final String TAG_ROLE_ROLE = "role:Role";

    /** _more_          */
    public static final String TAG_ROLE_GREATERROLETHAN =
        "role:greaterRoleThan";

    /** _more_          */
    public static final String TAG_ROLE_REPRESENTATIONROLE =
        "role:RepresentationRole";

    /** _more_          */
    public static final String TAG_IMPA_IMPACT = "impa:Impact";

    /** _more_          */
    public static final String TAG_IMPA_STRONGERTHAN = "impa:strongerThan";

    /** _more_          */
    public static final String TAG_SROLE_CHEMICALROLE = "srole:ChemicalRole";

    /** _more_          */
    public static final String TAG_SROLE_BIOLOGICALROLE =
        "srole:BiologicalRole";

    /** _more_          */
    public static final String TAG_SROLE_KILLS = "srole:kills";

    /** _more_          */
    public static final String TAG_SROLE_CONDUCTIONROLE =
        "srole:ConductionRole";

    /** _more_          */
    public static final String TAG_SROLE_PHYSICALROLE = "srole:PhysicalRole";

    /** _more_          */
    public static final String TAG_TRUST_TRUST = "trust:Trust";

    /** _more_          */
    public static final String TAG_SOLI_SOLIDSTATE = "soli:SolidState";

    /** _more_          */
    public static final String TAG_SPAC2_SIZE = "spac2:Size";

    /** _more_          */
    public static final String TAG_SSPAC_LARGERTHAN = "sspac:largerThan";

    /** _more_          */
    public static final String TAG_VERT2_VERTICALEXTENT =
        "vert2:VerticalExtent";

    /** _more_          */
    public static final String TAG_SSPAC_GREATERVERTICALEXTENTTHAN =
        "sspac:greaterVerticalExtentThan";

    /** _more_          */
    public static final String TAG_DIST2_DISTANCE = "dist2:Distance";

    /** _more_          */
    public static final String TAG_SSPAC_FARTHERTHAN = "sspac:fartherThan";

    /** _more_          */
    public static final String TAG_SPAC2_SHAPE = "spac2:Shape";

    /** _more_          */
    public static final String TAG_SPAC2_SPATIALCONFIGURATION =
        "spac2:SpatialConfiguration";

    /** _more_          */
    public static final String TAG_SPAC2_SPATIALSOURCE =
        "spac2:SpatialSource";

    /** _more_          */
    public static final String TAG_BAND_SPECTRALBAND = "band:SpectralBand";

    /** _more_          */
    public static final String TAG_WAVES2_HASWAVELENGTH =
        "waves2:hasWavelength";

    /** _more_          */
    public static final String TAG_BAND_WAVELENGTHBAND_NM =
        "band:WavelengthBand_nm";

    /** _more_          */
    public static final String TAG_FREQ2_HASFREQUENCY = "freq2:hasFrequency";

    /** _more_          */
    public static final String TAG_BAND_FREQUENCYBAND_MHZ =
        "band:FrequencyBand_MHz";

    /** _more_          */
    public static final String TAG_LINE_SPECTRALLINE = "line:SpectralLine";

    /** _more_          */
    public static final String TAG_LINE_WAVELENGTH_NM = "line:Wavelength_nm";

    /** _more_          */
    public static final String TAG_LINE_WAVELENGTH_CM = "line:Wavelength_cm";

    /** _more_          */
    public static final String TAG_SCALE_BEAUFORTSCALE =
        "scale:BeaufortScale";

    /** _more_          */
    public static final String TAG_SCALE_HASBEAUFORTSCALE =
        "scale:hasBeaufortScale";

    /** _more_          */
    public static final String TAG_SCALE_FUJITAPEARSONSCALE =
        "scale:FujitaPearsonScale";

    /** _more_          */
    public static final String TAG_SCALE_HASFUJITAPEARSONSCALE =
        "scale:hasFujitaPearsonScale";

    /** _more_          */
    public static final String TAG_SCALE_SAFFIRSIMPSONSCALE =
        "scale:SaffirSimpsonScale";

    /** _more_          */
    public static final String TAG_SCALE_HASSAFFIRSIMPSONSCALE =
        "scale:hasSaffirSimpsonScale";

    /** _more_          */
    public static final String TAG_VELO_SPEED = "velo:Speed";

    /** _more_          */
    public static final String TAG_SCALE_SLOWERTHAN = "scale:slowerThan";

    /** _more_          */
    public static final String TAG_SCALE_FASTERTHAN = "scale:fasterThan";

    /** _more_          */
    public static final String TAG_STATE_ACTIVITYLEVEL =
        "state:ActivityLevel";

    /** _more_          */
    public static final String TAG_STATE_SYSTEMSTATE = "state:SystemState";

    /** _more_          */
    public static final String TAG_STATE_CONNECTIVITY = "state:Connectivity";

    /** _more_          */
    public static final String TAG_THERM_THERMODYNAMICSTATE =
        "therm:ThermodynamicState";

    /** _more_          */
    public static final String TAG_SOLU_APPROXIMATES = "solu:approximates";

    /** _more_          */
    public static final String TAG_TEMP_TEMPERATURE = "temp:Temperature";

    /** _more_          */
    public static final String TAG_THERM_WARMERTHAN = "therm:warmerThan";

    /** _more_          */
    public static final String TAG_SCALE_FREQUENCY = "scale:Frequency";

    /** _more_          */
    public static final String TAG_SCALE_MOREFREQUENTTHAN =
        "scale:moreFrequentThan";

    /** _more_          */
    public static final String TAG_VISI_VISIBILITY = "visi:Visibility";

    /** _more_          */
    public static final String TAG_VISI_MOREVISIBLETHAN =
        "visi:moreVisibleThan";




    /** _more_          */
    public static final String ATTR_RDF_ABOUT = "rdf:about";

    /** _more_          */
    public static final String ATTR_RDF_RESOURCE = "rdf:resource";




}
