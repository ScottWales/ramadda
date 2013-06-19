

function CollectionForm(formId) {
    this.formId = formId;
    this.analysisUrl = "${urlroot}/model/climate_collection/analysis?";

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

    this.fieldChanged = function (collection, fieldIdx, num) {
        //        var selectedField = $('#' + fieldId).val();
        alert("field:" + selectedField);
    }

    var collectionForm = this;
    for(var i=1;i<=2;i++) {
        var collection  = 'collection' + i;
        this.getCollectionSelect(collection).change(function(event) {
            return collectionForm.collectionChanged('collection' + i);
        });
        for(var fieldIdx=1;fieldIdx<10;fieldIdx++) {
            this.getFieldSelect(collection, fieldIdx).change(function(event) {
                    return collectionForm.collectionChanged('collection' + i);
                });

        }
    }


}


