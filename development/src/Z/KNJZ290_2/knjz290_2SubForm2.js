function btn_submit(cmd) {
    //取消
    if (cmd == 'subform2_clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //更新
    if (cmd == 'subform2_update'){
        setSelect('selectStaff', 'STAFF');
        setSelect('selectSubclass', 'SUBCLASS');

        if (document.forms[0].STAFF_SELECTED.length == 0) {
            alert('{rval MSG916}');
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function setSelect(name, fieldName)
{
    attribute3 = document.getElementById(name);
    attribute3.value = "";
    sep = "";
    if (fieldName == "STAFF") {
        attributeName = document.forms[0].STAFF_NAME;
        attributeSelect =  document.forms[0].STAFF_SELECTED;
    } else {
        attributeName = document.forms[0].SUBCLASS_NAME;
        attributeSelect =  document.forms[0].SUBCLASS_SELECTED;
    }
    for (var i = 0; i < attributeName.length; i++) {
        attributeName.options[i].selected = 0;
    }
    for (var i = 0; i < attributeSelect.length; i++) {
        attributeSelect.options[i].selected = 1;
        attribute3.value = attribute3.value + sep + attributeSelect.options[i].value;
        sep = ",";
    }
}

function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function move1(side, fieldName)
{
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;

    if (side == "left") {
        if (fieldName == "STAFF") {
            attribute1 = document.forms[0].STAFF_NAME;
            attribute2 = document.forms[0].STAFF_SELECTED;
        } else {
            attribute1 = document.forms[0].SUBCLASS_NAME;
            attribute2 = document.forms[0].SUBCLASS_SELECTED;
        }
    } else {
        if (fieldName == "STAFF") {
            attribute1 = document.forms[0].STAFF_SELECTED;
            attribute2 = document.forms[0].STAFF_NAME;
        } else {
            attribute1 = document.forms[0].SUBCLASS_SELECTED;
            attribute2 = document.forms[0].SUBCLASS_NAME;
        }
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value+","+y;
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++)
    {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1,attribute1);
    if (temp2.length>0)
    {
        for (var i = 0; i < temp2.length; i++)
        {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }

}
function moves(sides, fieldName)
{
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    if (sides == "left") {
        if (fieldName == "STAFF") {
            attribute5 = document.forms[0].STAFF_NAME;
            attribute6 = document.forms[0].STAFF_SELECTED;
        } else {
            attribute5 = document.forms[0].SUBCLASS_NAME;
            attribute6 = document.forms[0].SUBCLASS_SELECTED;
        }
    } else {
        if (fieldName == "STAFF") {
            attribute5 = document.forms[0].STAFF_SELECTED;
            attribute6 = document.forms[0].STAFF_NAME;
        } else {
            attribute5 = document.forms[0].SUBCLASS_SELECTED;
            attribute6 = document.forms[0].SUBCLASS_NAME;
        }
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++)
    {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++)
    {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value+","+z;
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5,attribute5);
}
