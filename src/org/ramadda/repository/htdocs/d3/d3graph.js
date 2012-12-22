


function D3Graph(div, nodes, links,width,height) {
    if(!width)  width = 960;
    if(!height) height = 500;

    this.svg = d3.select(div).append("svg");

    this.svg.attr("width", width).attr("height", height);

    this.force = d3.layout.force()
        .gravity(.05)
        .distance(100)
        .charge(-100)
        .size([width, height]);

    this.force
        .nodes(nodes)
        .links(links);

    this.findNode =  function (value, field) {
        if(!field) {
            field = "id";
        }
        var nodes = this.force.nodes();
        for (var i in nodes) {
            if (nodes[i][field] === value) {
                return nodes[i];
            }
        }
        return null;
    }

    this.getForce =  function () {
        return this.force;
    }
    this.getSvg =  function () {
        return this.svg;
    }
    this.getNodes =  function () {
        return this.getForce().nodes();
    }
    this.getLinks =  function () {
        return this.getForce().links();
    }

    this.update =  function () {
        var theGraph = this;
        var svg =this.getSvg();
        var force =this.getForce();

        var link = svg.selectAll("line.graph-link").
        data(force.links(), function(d) { return d.source.id + "-" + d.target.id; });

        link.enter().insert("line").attr("class", "graph-link");
        link.exit().remove();

        var node = svg.selectAll("g.graph-node").
        data(force.nodes(), function(d) { return d.id;});

        var nodeEnter = node.enter().append("g")
        .attr("class", "graph-node")
        .call(force.drag);

        nodeEnter.append("image")
        .attr("class", "graph-circle")
        .attr("xlink:href", function(d) {return theGraph.getNodeIcon(d);})
        .attr("x", "-8px")
        .attr("y", "-8px")
        .attr("width", "16px")
        .attr("height", "16px");

        nodeEnter.append("text")
        .attr("class", function(d) {if(d.url) return "graph-node-text-unvisited"; else return "graph-node-text-visited";})
        .attr("dx", 12)
        .attr("dy", ".35em")
        .text(function(d) {return d.name});

        node.append("text")
        .attr("class", function(d) {if(d.url) return "graph-node-text-unvisited"; else return "graph-node-text-visited";})

        node.exit().remove();



    node.on("click", function(d){theGraph.nodeClicked(d);});

    force.on("tick", function() {
            link.attr("x1", function(d) { return d.source.x; })
                .attr("y1", function(d) { return d.source.y; })
                .attr("x2", function(d) { return d.target.x; })
                .attr("y2", function(d) { return d.target.y; });

            node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
        });

        var allNodes = svg.selectAll("text");
        allNodes.attr("class", function(d) {if(d.url) return "graph-node-text-unvisited"; else return "graph-node-text-visited";});

        // Restart the force layout.
        force.start();
    }

    this.nodeClicked = function(d) {
        var theGraph  =this;
        if(!d.url) return;
        var url = d.url;
        d.url = null;
        d.visited = true;
        d3.json(url, function(json) {
                if(json.nodes) {
                    for (var i in json.nodes) {
                        theGraph.addNode(json.nodes[i]);
                    }
                }
                if(json.links) {
                    for (var i in json.links) {
                        theGraph.addLink(json.links[i]);
                    }
                }
                theGraph.update();
        });
    }

    this.getNodeIcon = function(d) {
        if (d.icon) return d.icon;
        return "http://ramadda.org/repository/icons/folderclosed.png";
    }

    this.addNode = function(node)  {
        this.force.nodes().push(node);
    }

    this.addLink = function(link)  {
        this.force.links().push(link);
        //        this.getLinks().push(link);
    }


    this.update();
}






