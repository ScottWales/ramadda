

function CollectionForm(formId) {
    //Look at the bottom of this ctor for the init call
    this.formId = formId;
    this.analysisUrl = "${urlroot}/model/compare?";

    //We'll call this at the bottom
    this.init = function() {
        var collectionForm = this;
        for(var i=1;i<=2;i++) {
            collection  = 'collection' + i;
            this.initCollection(collection);
        }
    }

    this.initCollection = function(collection) {
        var collectionForm = this;
        this.getCollectionSelect(collection).change(function(event) {
                return collectionForm.collectionChanged(collection);
            });
        for(var fieldIdx=0;fieldIdx<10;fieldIdx++) {
            this.initField(collection, fieldIdx);
        }
        var collectionId  =  this.getCollectionSelect(collection).val();
        //If they had one selected 
        if(collectionId!="") {
            this.updateFields(collection,  collectionId, 0, true);
        }
    }

    this.initField = function(collection, fieldIdx) {
        var collectionForm = this;
        this.getFieldSelect(collection, fieldIdx).change(function(event) {
                return collectionForm.fieldChanged(collection, fieldIdx);
            });
    }


    //Gets called when the collection select widget is changed
    this.collectionChanged = function  (collection, selectId) {
        var collectionId = this.getCollectionSelect(collection).val();
        var fieldIdx = 0;
        if(!collectionId || collectionId == "") {
            this.clearFields(collection, fieldIdx);
            return false;
        }
        this.updateFields(collection,  collectionId, fieldIdx, false);
        return false;

    }


    //Get the list of metadata values for the given field and collection
    this.updateFields = function(collection, collectionId, fieldIdx, fromInit) {
        var url = this.analysisUrl +"json=test&collection=" + collectionId+"&field=" + fieldIdx;
        //Assemble the other field values up to the currently selected field
        for(var i=0;i<fieldIdx;i++) {
            var val = this.getFieldSelect(collection, i).val();
            if(val!="") {
                url = url +"&field" + i + "=" + encodeURIComponent(val);
            }
        }
        var collectionForm = this;
        $.getJSON(url, function(data) {
                var hadValue = collectionForm.setFieldValues(collection, data, fieldIdx);
                if(!fromInit) {
                    collectionForm.clearFields(collection, fieldIdx+1);
                } else if(collectionForm.hasField(collection, fieldIdx+1)) {
                    //If we're initializing then repopulate the selects
                    if(hadValue) {
                        collectionForm.updateFields(collection, collectionId, fieldIdx+1, true);
                    }
                }
            });

    }

    //Clear the field selects starting at start idx
    this.clearFields = function(collection, startIdx) {
        for(var idx=startIdx;idx<10;idx++) {
            this.getFieldSelect(collection, idx).html("<select><option value=''>--</option></select>");
        }
    }

    //Get the select object for the given field
    this.getFieldSelect = function(collection, fieldIdx) {
        return  $('#' + this.getFieldSelectId(collection, fieldIdx));
    }


    this.hasField = function(collection, fieldIdx) {
        return  this.getFieldSelect(collection, fieldIdx).size()>0;
    }


    //Get the selected entry id
    this.getSelectedCollectionId = function(collection) {
        return this.getCollectionSelect(collection).val();
    }

    //Get the collection selector 
    this.getCollectionSelect = function(collection) {
        return  $('#' + this.getCollectionSelectId(collection));
    }

    //This matches up with ClimateModelApiHandler.getFieldSelectId
    this.getFieldSelectId = function(collection, fieldIdx) {
        return  this.getCollectionSelectId(collection) + "_field" + fieldIdx;
    }

    //dom id of the collection select widget
    //This matches up with ClimateModelApiHandler.getCollectionSelectId
    this.getCollectionSelectId = function(collection) {
        return  this.formId +"_"  + collection;
    }



    this.setFieldValues = function(collection, data, fieldIdx) {
        var currentValue =    this.getFieldSelect(collection, fieldIdx).val();
        var html = "<select>\n";
        for(var i=0;i<data.length;i++)  {
            var objIQ = data[i];
            var value,label;
            var type = typeof(objIQ);
            if (type == 'object') {  
                // made from TwoFacedObject [ {id:id1,value:value1}, {id:id2,value:value2} ]
                value = objIQ.id;
                label = objIQ.label;
            } else {
                value = objIQ;
                label  = value;
            }
            if(label == "") {
                label =  "--";
            }
            var extra = "";
            if(currentValue == value) {
                extra = " selected ";
            }
            html += "<option value=\'"+value+"\'   " + extra +" >" + label +"</option>\n";
        }
        html+="</select>\n";
        this.getFieldSelect(collection, fieldIdx).html(html);
        return currentValue !="";
    }

    this.fieldChanged = function (collection, fieldIdx) {
        var value = this.getFieldSelect(collection, fieldIdx).val();
        if(value == null || value == "") {
            this.clearFields(collection, fieldIdx+1);
            return;
        }
        this.updateFields(collection, this.getSelectedCollectionId(collection),  fieldIdx+1);
    }

    this.init();


}


