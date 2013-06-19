

function CollectionForm(formId) {
    this.formId = formId;
    this.analysisUrl = "${urlroot}/model/climate_collection/analysis?";
    this.collectionChanged = function  (whichCollection, selectId) {
        var selectedCollection = $('#' + selectId).val();
        if(selectedCollection == "") {
            for(var selectIdx=0;selectIdx<10;selectIdx++) {
                var columnSelectId = this.formId +"_"  + whichCollection + "_select" + (selectIdx+1);
                var columnSelect = $('#' + columnSelectId);
                if(columnSelect.size()==0) {
                    break;
                }
                columnSelect.html("<select><option value=''>--</option></select>");
            }
            return false;
        }
        var url = this.analysisUrl +"json=test&collection=" + selectedCollection;
        var collectionForm = this;
        $.getJSON(url, function(data) {
                collectionForm.setValues(whichCollection, data);
            });
        return false;
    }

    this.getSelectId = function(whichCollection, fieldIdx) {
        var selectId = this.formId +"_"  + whichCollection + "_select" + fieldIdx;
    }

    this.setValues = function(whichCollection, data) {
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
        var columnSelectId = this.formId +"_"  + whichCollection + "_select1";
        $('#' + columnSelectId).html(html);
    }

    this.fieldChanged = function (whichCollection, fieldId) {
        var selectedField = $('#' + fieldId).val();
        alert("field:" + selectedField);
    }



}


