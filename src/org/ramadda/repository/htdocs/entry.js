
function createEntriesFromJson(data) {
    var entries = new Array();
    for(var i=0;i<data.length;i++)  {
        var entry = new Entry(data[i]);
        entries.push(entry);
    }
    return entries;
}

function Entry (entryTuple) {
    this.entry = entryTuple;
    this.getId = function () {
        return  this.entry.id;
    }
    this.getIconImage = function () {
        return "<img src=\"" + this.entry.icon +"\">";
    }
    this.getColumnValue = function (name) {
        var value = this.entry["column." + name];
        return value;
    }

    this.getColumnNames = function () {
        var names =  this.entry.columnNames;
        if (!names) names = new Array();
        return names;
    }

    this.getColumnLabels = function () {
        var names =  this.entry.columnLabels;
        if (!names) names = new Array();
        return names;
    }

    this.getName = function () {
        if(this.entry.name ==null || this.entry.name == "") {
            return "no name";
        }
        return this.entry.name;
    }
    this.getFilesize = function () {
        var size =  parseInt(this.entry.filesize);
        if(size == size) return size;
        return 0;
    }
    this.getFormattedFilesize = function () {
        return size_format(this.getFilesize());
    }
    this.getLink = function (label) {
        if(!label) label = this.getName();
        return  "<a href=\"${urlroot}/entry/show?entryid=" + this.entry.id +"\">" + label +"</a>";
    }

}


