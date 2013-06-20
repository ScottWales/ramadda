

function CollectionForm(formId) {
    //Look at the bottom of this ctor to
    this.formId = formId;
    this.analysisUrl = "${urlroot}/model/climate_collection/analysis?";

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
        for(var fieldIdx=1;fieldIdx<10;fieldIdx++) {
            this.initField(collection, fieldIdx);
        }
    }

    this.initField = function(collection, fieldIdx) {
        var collectionForm = this;
        this.getFieldSelect(collection, fieldIdx).change(function(event) {
                return collectionForm.fieldChanged(collection, fieldIdx);
            });
    }


    this.collectionChanged = function  (collection, selectId) {
        var collectionId = this.getCollectionSelect(collection).val();
        var fieldIdx = 1;
        if(!collectionId || collectionId == "") {
            this.clearFields(collection, fieldIdx);
            return false;
        }

        this.updateFields(collection,  collectionId, fieldIdx);
        return false;

    }

    this.updateFields = function(collection, collectionId, fieldIdx) {
        var url = this.analysisUrl +"json=test&collection=" + collectionId+"&field=" + fieldIdx;
        var collectionForm = this;
        $.getJSON(url, function(data) {
                collectionForm.setFieldValues(collection, data, fieldIdx);
            });

    }
    this.clearFields = function(collection, start) {
        for(var idx=start;idx<10;idx++) {
            this.getFieldSelect(collection, idx).html("<select><option value=''>--</option></select>");
        }
    }

    this.getFieldSelect = function(collection, fieldIdx) {
        return  $('#' + this.getFieldSelectId(collection, fieldIdx));
    }

    this.getCollectionId = function(collection) {
        return this.getCollectionSelect(collection).val();
    }

    this.getField = function(collection, fieldIdx) {
        return this.getFieldSelect(collection, fieldIdx).val();
    }



    this.getCollectionSelect = function(collection) {
        return  $('#' + this.getCollectionSelectId(collection));
    }


    this.getFieldSelectId = function(collection, fieldIdx) {
        return  this.getCollectionSelectId(collection) + "_select" + fieldIdx;
    }

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
        this.getFieldSelect(collection, fieldIdx).html(html);
        this.clearFields(collection, fieldIdx+1);
    }

    this.fieldChanged = function (collection, fieldIdx) {
        var value = this.getFieldSelect(collection, fieldIdx).val();
        if(value == null || value == "") {
            this.clearFields(collection, fieldIdx+1);
            return;
        }
        this.updateFields(collection, this.getCollectionId(collection),  fieldIdx+1);
    }

    this.init();


}


