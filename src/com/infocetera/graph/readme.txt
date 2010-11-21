
The GraphApplet html tags  look like:

<applet code=com.infocetera.graph.GraphApplet.class
        width=700 height=450
	codebase=/ifcht/java archive=graph.jar>
        <param name=dfltnode  value="the node id of the first centered node">
        <param name=dataurl value="the url to  get the graph xml file">
        <param name=nodeurl value="see below">	
</applet>


If the nodeurl is defined then it is used to incrementally load a
a sub-graph defined by a  given list  of node ids. The nodeurl should be a
template that contains a macro %nodeids%, which is replaced by a ","
comma separated list of node ids. e.g.:
value="/getgraph?ids=%nodeids%"

If you use the nodeurl you shoud also still define a dataurl to
provide the initial graph.

When the graph applet encounters a node that is has not loaded
yet (e.g., node1 has an edge whose head is node2 but node2 does not
exist yet) it will instantiate the actual url by substituting the a
comma separated list of the lacking node ids  for %nodeids%. The return is
of the same xml format  described below and can contain any number of
node specifications. 

The dataurl parameter is used to get the  xml file that defines the graph.
The xml format is  made up of an enclosing canvas tag that holds a
graph tag that holds a set of nodetype, edgetype, node and edge tags:

  <canvas (canvas attributes)>
    <graph>
       <nodetype name="type name" (node attributes) />
       <edgetype name="type name" (edge attributes) />       
       <node id="unique id" type="node type" (node attributes) />
       <node (node attributes)>
          <edge to=(node id) (edge attributes)/>
       </node>
       <edge from="node id" to ="node id" (edge attributes)
    </graph>
  </canvas>


canvas attributes:
------------------
level     = (1-5) default level used
radial    = ("true", "false") is radial layout  default is true
simplegui = ("true", "false") if true then just show the graph view
bgcolor   = background color
bgimage   = url of image to use in background. drawn centered.


node attributes:
------------------
The only required  attribute for nodes is the "id" attribute.
Color values are named colors (e.g., red, blue, black, white, etc.) or
integer value of color.

id= the unique id of this node
type = node type (see below)
fillcolor =  (color)
fontface=named font face, e.g. Dialog, Arial, Times Roman, etc.
fontsize= (integer)
fontstyle= (PLAIN,ITALIC,BOLD, BOLDITALIC)
cfontsize = font size to use when this node is the center node  
linecolor=color of box outline
mouse = text used for mouseover. Can contain new lines.
shape = one of:RECT, OVAL, TRIANGLE, 3DRECT,  BARREL  
textcolor= color used to draw text label
vtextcolor = text color used when this node has been clicked on, i.e., visited
title = text used in display
data=extra stuff used in the url processing described below


edge attributes:
----------------
color= edge color 
width=line width (integer)
arrow=size of arrow. (if negative then a filled arrow is drawn)

edge tags can be standalone or can be contained within a node tag.
If standalone they define the ids of the two nodes (i.e., from, to)
that the edge connects. If contained within a node tag they contain
the "to" node id.

The nodetype and edgetype tags allow you to define common properties
for nodes and edges. e.g., you could have a node type called "redbox"
and an edgetype called "blueline"

  <canvas>
    <graph>
       <edgeype name="blueline" color="blue" width="3" arrow="4"/>
       <nodetype name="redbox" fillcolor="red" shape="RECT" />
       <node id="some node" type="redbox">
          <edge to="some node" type="blueline"/>       
       ...

Now if you wanted a particular node (or edge) to have different attributes just
add them in: The node and edge properties take priority over the types:
       <node id="some node" type="redbox" fillcolor="blue">
          <edge to="some node" type="blueline" arrow="1"/>


Now there are a few more tags that are used to define the behavior of the applet.
command tags are used to create popup menus. They can be defined within a node,
nodetype, graph and canvas tags:
    <graph>
       <command label="View" url="%HTTP%/some_url?ID=%ID%" target="_some_browser_window"/>
       <nodetype name="type name" (node attributes)>
          <command label="View" url="%HTTP%/some_other_url?ID=%ID%" />
       </nodetype>
       <node id=...>
                 <command label="Command for this node" url="..." />
       </node>


The command tags have a label  a template url and a browser window target. 
When the user right mouse clicks on a node the command tags defined for the node,
its nodetype, the graph and the canvas are used to create a popup menu. The
url template contains a set of macros (e.g., %ID%, %HTTP%) that are substituted
with the relevant values. The macros are:
%ID% the id field of the node
%TITLE% the title of the node
%DATA% data field of node
%HTTP% the http protocol header (e.g., http://www.foo.com:80/)

The url is then opened in the target window. If target equals blank (i.e., target="")
then the current applet browser window is used.

A set of "click" tags are also supported:
  <click url="..." target="" />
  <shiftclick url="..." target="" />
  <controlclick url="..." target="" />


These  can be defined within the node, nodetype, graph and canvas tags
and are used when the user clicks, shift-clicks or control-clicks a node.
Same processing is done as with the command tags.



