function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit(cmd) {
    var i;
    var select;
    var sep;
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}')){
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
    if (document.forms[0].L_COURSE.length == 0 && document.forms[0].R_COURSE.length == 0) {
        alert("データは存在していません。");
        return false;
    }
    select = "";
    sep = "";
    for (i = 0; i < document.forms[0].L_COURSE.length; i++) {
        select += sep + document.forms[0].L_COURSE.options[i].value;
        sep = ",";
    }
    document.forms[0].selectdata.value = select;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function ClearList(OptionList) {
    OptionList.length = 0;
}

function compareVal(o1, o2) {
    if (o1.value < o2.value) {
        return -1;
    } else if (o1.value > o2.value) {
        return 1;
    }
    return 0;
}

function move(side) {
    var temp1 = [];
    var temp2 = [];
    var i;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all") {
        attribute1 = document.forms[0].L_COURSE;
        attribute2 = document.forms[0].R_COURSE;
    } else {
        attribute1 = document.forms[0].R_COURSE;
        attribute2 = document.forms[0].L_COURSE;
    }


    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        temp1.push({value: attribute2.options[i], text: attribute2.options[i].text});
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            if (attribute1.options[i].selected) {
                temp1.push({value: attribute1.options[i], text: attribute1.options[i].text});
            } else {
                temp2.push({value: attribute1.options[i], text: attribute1.options[i].text});
            }
        } else {
            temp1.push({value: attribute1.options[i], text: attribute1.options[i].text});
        }
    }

    temp1.sort(compareVal);

    //generating new options
    for (i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i].value;
        attribute2.options[i].text =  temp1[i].text;
    }

    //generating new options
    ClearList(attribute1);
    if (temp2.length>0) {
        for (i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i].value;
            attribute1.options[i].text =  temp2[i].text;
        }
    }
}

function move1(side) {
    var temp1 = [];
    var temp2 = [];
    var i;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].L_COURSE;
        attribute2 = document.forms[0].R_COURSE;
    } else {
        attribute1 = document.forms[0].R_COURSE;
        attribute2 = document.forms[0].L_COURSE;  
    }

    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        temp1.push({value: attribute2.options[i].value, text: attribute2.options[i].text});
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected )
        {  
            temp1.push({value: attribute1.options[i].value, text: attribute1.options[i].text});
        } else {
            temp2.push({value: attribute1.options[i].value, text: attribute1.options[i].text});
        }
    }

    temp1.sort(compareVal);

    //generating new options 
    for (i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i].value;
        attribute2.options[i].text =  temp1[i].text;
    }

    //generating new options
    ClearList(attribute1);
    if (temp2.length>0) {
        for (i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i].value;
            attribute1.options[i].text =  temp2[i].text;
        }
    }
}
