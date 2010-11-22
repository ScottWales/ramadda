

package ucar.unidata.repository.util;
import ucar.unidata.xml.XmlUtil;
import ucar.unidata.util.IOUtil;
import ucar.unidata.util.StringUtil;
import org.w3c.dom.*;


import java.util.Hashtable;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;


public class ProcessAnds {
    public static final String TAG_REGISTRYOBJECTS = "registryObjects";
    public static final String TAG_REGISTRYOBJECT = "registryObject";
    public static final String TAG_KEY = "key";
    public static final String TAG_ORIGINATINGSOURCE = "originatingSource";
    public static final String TAG_PARTY = "party";
    public static final String TAG_NAME = "name";
    public static final String TAG_NAMEPART = "namePart";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_ADDRESS = "address";
    public static final String TAG_ELECTRONIC = "electronic";
    public static final String TAG_VALUE = "value";
    public static final String TAG_PHYSICAL = "physical";
    public static final String TAG_ADDRESSPART = "addressPart";
    public static final String TAG_RELATEDOBJECT = "relatedObject";
    public static final String TAG_RELATION = "relation";
    public static final String TAG_DESCRIPTION = "description";
    public static final String TAG_RELATEDINFO = "relatedInfo";
    public static final String TAG_ACTIVITY = "activity";
    public static final String TAG_SUBJECT = "subject";
    public static final String TAG_COLLECTION = "collection";
    public static final String TAG_IDENTIFIER = "identifier";
    public static final String TAG_SPATIAL = "spatial";
    public static final String TAG_SERVICE = "service";
    public static final String TAG_URL = "url";

    public static final String ATTR_XMLNS = "xmlns";
    public static final String ATTR_GROUP = "group";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_DATEFROM = "dateFrom";
    public static final String ATTR_DATETO = "dateTo";
    public static final String ATTR_DATEACCESSIONED = "dateAccessioned";
    public static final String ATTR_DATEMODIFIED = "dateModified";

    static int idcnt = 0;
    public static void processFile(String file) throws Exception {
        String [] subTags = {TAG_PARTY, TAG_ACTIVITY, TAG_COLLECTION, TAG_SERVICE};
        String [] subTagNames = {"Parties", "Activities", "Collections", "Services"};
        String [] entryTypes = {"ontology.party", "ontology.activity", "ontology.collection", "ontology.service"};
        List<Object> registryObjects = new ArrayList<Object>();
        Hashtable<String,Object> keyMap = new Hashtable<String,Object>();
        Hashtable<String,Group> groups = new Hashtable<String,Group>();

        System.err.println ("parsing:" + file);
        Element root = XmlUtil.getRoot(file,ProcessAnds.class);
        System.err.println ("done parsing");
        NodeList children = XmlUtil.getElements(root);

        StringBuffer xml = new StringBuffer(XmlUtil.XML_HEADER);
        xml.append("<entries>\n");
        for (int i = 0; i < children.getLength(); i++) {
            Element repositoryObject = (Element)children.item(i);
            if(!repositoryObject.getTagName().equals(TAG_REGISTRYOBJECT)) {
                System.err.println ("unknown tag:" + repositoryObject.getTagName());
                continue;
            }
            String key = XmlUtil.getGrandChildText(repositoryObject,TAG_KEY);
            Element mainElement = null;
            String partName = "";
            String entryType = "";
            for(int j=0;j<subTags.length;j++) {
                String subTag = subTags[j];
                mainElement= (Element)XmlUtil.findChild(repositoryObject, subTag);
                if(mainElement!=null) {
                    partName = subTagNames[j];
                    entryType = entryTypes[j];
                    break;
                }
            }
            if(mainElement == null) {
                System.err.println ("Unknown tag: " + XmlUtil.toString(repositoryObject));
                continue;
            }
            String groupName = XmlUtil.getAttribute(repositoryObject,ATTR_GROUP,"");
            Group group = groups.get(groupName);
            if(group == null) {
                group = new Group(groupName);
                groups.put(groupName, group);
                xml.append(XmlUtil.tag("entry", XmlUtil.attrs("type", "group","name",  group.name,  "id",group.id)));
            }
            String part = mainElement.getTagName();
            String partId = group.getPartId(part);
            if(partId == null) {
                partId = "id_" + (idcnt++);
                group.addPart(part, partId);
                xml.append(XmlUtil.tag("entry", XmlUtil.attrs("name", partName, "parent",group.id,"id", partId)+XmlUtil.attrs("type","group")));
            }

            String name = "";
            NodeList names = XmlUtil.getElements(mainElement, TAG_NAME);
            if(names.getLength()>0) {
                Element nameElement = (Element)names.item(names.getLength()-1);
                NodeList nameParts = XmlUtil.getElements(nameElement, TAG_NAMEPART);
                for(int j=nameParts.getLength()-1;j>=0;j--) {
                    name = name +" " + XmlUtil.getChildText((Element)nameParts.item(j));
                }
            } else {
                //                System.err.println ("no name: " + XmlUtil.toString(mainElement));
            }
            name = name.trim();



            StringBuffer childTags = new StringBuffer();

            List descriptions = XmlUtil.findChildren(mainElement, TAG_DESCRIPTION);
            StringBuffer desc = new StringBuffer();
            for (int j = 0; j < descriptions.size(); j++) {
                Element descriptionNode = (Element) descriptions.get(j);
                if(desc.length()>0) {
                    desc.append("<br>----<br>");
                }
                String text = XmlUtil.getChildText(descriptionNode).trim();
                text = text.replace("\n","<br>");
                desc.append(text);
            }
            if(desc.length()>0) {
                childTags.append(XmlUtil.tag("description","",XmlUtil.getCdata(desc.toString())));
            }

            List subjects = XmlUtil.findChildren(mainElement, TAG_SUBJECT);
            for (int j = 0; j < subjects.size(); j++) {
                Element subject = (Element) subjects.get(j);
                String type = XmlUtil.getAttribute(subject,ATTR_TYPE);
                if(!type.equals("local")) continue;
                String value = XmlUtil.getChildText(subject).trim();
                if(value.indexOf("|")>=0) {
                    value = StringUtil.join(">", StringUtil.split(value,"|",true,true));
                } else if(value.indexOf(" - ")>=0) {
                    value = StringUtil.join(">", StringUtil.split(value,"-",true,true));
                }
                childTags.append(XmlUtil.tag("metadata", XmlUtil.attrs("type","enum_tag",
                                                                       "attr1", value)));
            }


            StringBuffer entryAttrs = new StringBuffer();
            entryAttrs.append(XmlUtil.attrs("name", name,  "parent", partId, "id", key,"type", entryType));
            



            processLocations(mainElement, childTags, entryAttrs);
            xml.append(XmlUtil.tag("entry", entryAttrs.toString(),childTags.toString())); 
            Object o = new Object(mainElement, key, name, group);
            keyMap.put(key, o);
            registryObjects.add(o);
            //            if(registryObjects.size()>50) break;
        }

        HashSet<String> seenAssociation = new HashSet<String>();
        for(Object obj: registryObjects) {
            NodeList related = XmlUtil.getElements(obj.element, TAG_RELATEDOBJECT);
            for (int j = 0; j < related.getLength(); j++) {
                Element relatedObject = (Element)related.item(j);
                String relatedKey = XmlUtil.getGrandChildText(relatedObject,TAG_KEY);
                Object relatedTo = keyMap.get(relatedKey);
                if(relatedTo==null) {
                    continue;
                }
                String type = "";
                Element relation = XmlUtil.findChild(relatedObject,TAG_RELATION);
                if(relation!=null) {
                    type = XmlUtil.getAttribute(relation,ATTR_TYPE, type);
                }
                //        <relation type="isMemberOf">
                String key1  = obj.key +"->" + type +"->" + relatedTo.key;
                String key2  = relatedTo.key +"->" + type +"->" + obj.key;
                if(!seenAssociation.contains(key1) && !seenAssociation.contains(key2)) {
                    xml.append(XmlUtil.tag("association", XmlUtil.attrs("from", obj.key,"to", relatedTo.key,"type",type)));
                    seenAssociation.add(key1);
                    seenAssociation.add(key2);
                } else {
                    //                    System.err.println("seen: " + type);
                }
            }
        }
        xml.append("</entries>\n");
        IOUtil.writeFile("entries.xml", xml.toString());
        System.err.println ("#objects:" + registryObjects.size());


    }


    public static void processAddress(Element address, StringBuffer childTags, StringBuffer entryAttrs, boolean[] didUrl) {
        if(address==null) return;
        NodeList addresses = XmlUtil.getElements(address);
        for (int j = 0; j < addresses.getLength(); j++) {
            Element node = (Element)addresses.item(j);
            String tag = node.getTagName();
            if(tag.equals(TAG_ELECTRONIC)) {
                String type = XmlUtil.getAttribute(node,ATTR_TYPE);
                String value = XmlUtil.getGrandChildText(node,TAG_VALUE).trim();
                if(type.equals("url")) {
                    if(!didUrl[0]) {
                        entryAttrs.append(XmlUtil.attr("url", value));
                        didUrl[0] = true;
                    } else {
                        childTags.append(XmlUtil.tag("metadata", XmlUtil.attrs("type","thredds.link",
                                                                               "attr1", value,
                                                                               "attr2", value)));
                    }
                }  else {
                        childTags.append(XmlUtil.tag("metadata", XmlUtil.attrs("type","misc.contact",
                                                                               "attr1", type,
                                                                               "attr2", value)));
                }
            } else if(tag.equals(TAG_PHYSICAL)) {
                //TODO: handle physica
            } else {
                System.err.println ("unknown address type:" +tag);
            }
        }
    }


    public static void processLocations(Element mainElement, StringBuffer childTags, StringBuffer entryAttrs) {

        List locations = XmlUtil.findChildren(mainElement, TAG_LOCATION);
        boolean[] didUrl = {false};
        boolean[] didBounds = {false};
        HashSet seenLoc = new HashSet();
        for (int i = 0; i < locations.size(); i++) {
            Element location = (Element)locations.get(i);
            processAddress((Element)XmlUtil.findChild(location,TAG_ADDRESS), childTags, entryAttrs,didUrl);
            Element spatial = (Element)XmlUtil.findChild(location,TAG_SPATIAL);
            if(spatial!=null) {
                String type = XmlUtil.getAttribute(spatial, ATTR_TYPE,"");
                String text = XmlUtil.getChildText(spatial).trim();
                if(type.equals("iso19139dcmiBox")) {
                    // <spatial type="iso19139dcmiBox">northlimit=7.4; southlimit=7.2; westlimit=134.3; eastlimit=134.6; projection=WGS84</spatial>
                    List<String> toks = StringUtil.split(text,";",true,true);
                    for(String tok: toks) {
                        List<String> subToks = StringUtil.split(tok,"=",true,true);
                        if(subToks.size()!=2) {
                            //                            System.err.println("toks:" + tok);
                            continue;
                        }
                        String what = subToks.get(0).toLowerCase();
                        String loc = subToks.get(1);
                        String attribute = null;
                        if(what.equals("northlimit"))
                            attribute ="north";
                        else if(what.equals("southlimit"))
                            attribute ="south";
                        else if(what.equals("eastlimit"))
                            attribute ="east";
                        else if(what.equals("westlimit"))
                            attribute ="west";
                        else if(what.equals("projection")) {
                        }
                        else {
                            System.err.println ("na:" + what);
                        }
                        if(attribute!=null && !seenLoc.contains(attribute)) {
                            seenLoc.add(attribute);
                            entryAttrs.append(XmlUtil.attrs(attribute,loc));
                        }
                    }

                } else if(type.equals("iso31662")) {
                    childTags.append(XmlUtil.tag("metadata", XmlUtil.attrs("type","spatial.region",
                                                                           "attr1", text)));
                    //                    <spatial type="iso31662" xml:lang="en">AU-QLD</spatial>
                } else if(type.equals("kmlPolyCoords")) {
                        childTags.append(XmlUtil.tag("metadata", XmlUtil.attrs("type","spatial.kmlpolycoords",
                                                                               "attr1", text)));
                } else if(type.equals("text")) {
                        childTags.append(XmlUtil.tag("metadata", XmlUtil.attrs("type","spatial.region",
                                                                               "attr1", text)));
                }
            }
        }
    }


    public static class Group {
        String name;
        String id;
        Hashtable<String,String> parts = new Hashtable<String,String>();
        Group(String name) {
            this.name = name;
            this.id = "id_" + (idcnt++);
        }

        public String toString() {
            return name;
        }

        String getPartId(String part) {
            return parts.get(part);
        }
        void addPart(String part, String partId) {
            parts.put(part, partId);
        }



    }

    public static class Object {
        Element element;
        String name;
        Group group;
        String key;

        Object(Element element, String key, String name, Group group) {
            this.element = element;
            this.key = key;
            this.name = name;
            this.group = group;
        }
    }


    public  static void main(String[]args) throws Exception {
        for(String arg: args)  {
            processFile(arg);
        }
     
        
    }

}