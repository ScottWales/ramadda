

function CollectionForm(formId) {
    //Look at the bottom of this ctor to
    this.formId = formId;
    this.analysisUrl = "${urlroot}/model/analysis?";

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

        this.updateFields(collection,  collectionId, fieldIdx);
        return false;

    }


    //Get the list of metadata values for the given field and collection
    this.updateFields = function(collection, collectionId, fieldIdx) {
        var url = this.analysisUrl +"json=test&collection=" + collectionId+"&field=" + fieldIdx;
        //Assemble the other field values up to the currently selected field
        for(var i=0;i<fieldIdx;i++) {
            var val = this.getFieldSelect(collection, i).val();
            if(val!="") {
                url = url +"&field" + i + "=" + val;
            }
        }
        var collectionForm = this;
        $.getJSON(url, function(data) {
                collectionForm.setFieldValues(collection, data, fieldIdx);
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
        var html = "<select>";
        for(var i=0;i<data.length;i++)  {
            var value = data[i];
            var label  = value;
            if(label == "") {
                label =  "--";
            }
            html += "<option value=\'"  + data[i]+"\'>" + label +"</option>";
        }
        html+="</select>";
        //        alert("getfield:" +this.getFieldSelect(collection, fieldIdx).size());
        //        alert(this.getFieldSelectId(collection, fieldIdx));

        this.getFieldSelect(collection, fieldIdx).html(html);
        this.clearFields(collection, fieldIdx+1);
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


