function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {

    if (document.forms[0].CATEGORY_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
        document.forms[0].CATEGORY_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
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
    attribute = document.forms[0].CATEGORY_NAME;
    ClearList(attribute,attribute);
    attribute = document.forms[0].CATEGORY_SELECTED;
    ClearList(attribute,attribute);
}

function move(side) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "rightall") {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
    } else {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        temp1[current2]     = attribute2.options[i].value;
        tempa[current2]     = attribute2.options[i].text;
        tempaa[current2]    = attribute2.options[i].value+","+current2;
        current2++;
    }

    if (side == "rightall" || side == "leftall") {
        for (var i = 0; i < attribute1.length; i++) {
            attribute1.options[i].selected = 1;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            temp1[current2]     = attribute1.options[i].value;
            tempa[current2]     = attribute1.options[i].text;
            tempaa[current2]    = attribute1.options[i].value+","+current2;
            current2++;
        } else {
            temp2[current1]     = attribute1.options[i].value;
            tempb[current1]     = attribute1.options[i].text;
            current1++;
        }
    }

    ClearList(attribute2,attribute2);

    tempaa.sort();

    for (var i = 0; i < current2; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i]       = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text  =  tempa[tmp[1]];
    }
    attribute2.length = current2;

    ClearList(attribute1,attribute1);

    if (current1 > 0) {
        for (var i = 0; i < current1; i++) {
            attribute1.options[i]       = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text  = tempb[i];
        }
    }
    attribute1.length = current1;

}
