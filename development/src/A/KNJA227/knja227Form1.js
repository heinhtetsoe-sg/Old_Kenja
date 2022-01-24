function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    if (document.forms[0].SCHOOL_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }

    //
    for (var i = 0; i < document.forms[0].SCHOOL_NAME.length; i++) {
        document.forms[0].SCHOOL_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].SCHOOL_SELECTED.length; i++) {
        document.forms[0].SCHOOL_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJA";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].SCHOOL_NAME;
    ClearList(attribute,attribute);
    attribute = document.forms[0].SCHOOL_SELECTED;
    ClearList(attribute,attribute);
}

function compare(v1, v2) {
    if (v1 > v2) {
        return 1;
    }
    if (v1 < v2) {
        return -1;
    }
    return 0;
}
function compareTextValue(o1, o2) {
    var c;
    c = compare(o1.text, o2.text);
    if (c != 0) {
        return c;
    }
    c = compare(o1.value, o2.value);
    return c;
}

function movec(side, left, right, sort)
{
    var temp1 = new Array();
    var temp2 = new Array();
    var attribute1;
    var attribute2;
    var i;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all") {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    } else {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }
    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        temp1[temp1.length] = {"value": attribute2.options[i].value, "text": attribute2.options[i].text};
    }
    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            if (attribute1.options[i].selected) {
                temp1[temp1.length] = {"value": attribute1.options[i].value, "text": attribute1.options[i].text};
            } else {
                temp2[temp2.length] = {"value": attribute1.options[i].value, "text": attribute1.options[i].text};
            }
        } else {
            temp1[temp1.length] = {"value": attribute1.options[i].value, "text":attribute1.options[i].text};
        }
    }
    if (sort){
        temp1.sort(compareTextValue);
    }

    for (i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i].value;
        attribute2.options[i].text =  temp1[i].text;
    }
    
    ClearList(attribute1);
    for (i = 0; i < temp2.length; i++) {
        attribute1.options[i] = new Option();
        attribute1.options[i].value = temp2[i].value;
        attribute1.options[i].text =  temp2[i].text;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (i = 0; i < document.forms[0][left].length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }
}

