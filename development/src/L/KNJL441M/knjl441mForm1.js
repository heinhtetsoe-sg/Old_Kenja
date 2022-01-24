function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].target = '_self';
    document.forms[0].action = 'knjl441mindex.php';
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    if (document.forms[0].PRINT_DATE.value.length < 1) {
        alert('{rval MSG301}' + '\n\n( 発行日付 )');
        document.forms[0].PRINT_DATE.focus();
        return false;
    }

    if (document.forms[0].CATEGORY_SELECTED.options.length < 1) {
        alert('{rval MSG310}' + '\n\n( 出力対象志望区分 )');
        return false;
    }

    for (var idx = 0; idx < document.forms[0].CATEGORY_SELECTED.options.length; idx++) {
        document.forms[0].CATEGORY_SELECTED.options[idx].selected = true;
    }

    //url = location.hostname;
    //document.forms[0].action = 'http://' + url + '/cgi-bin/printenv.pl';
    document.forms[0].action = SERVLET_URL + '/KNJL';
    document.forms[0].target = '_blank';
    document.forms[0].submit();
}

/***************************************************************************/
/**************************** List to List 関係 ****************************/
/***************************************************************************/
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].CATEGORY_NAME;
    ClearList(attribute, attribute);
    attribute = document.forms[0].CATEGORY_SELECTED;
    ClearList(attribute, attribute);
}

function move1(side) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == 'left') {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].value).substring(0, 12) + ',' + y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = String(attribute1.options[i].value).substring(0, 12) + ',' + y;
        } else {
            y = current2++;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text = tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1, attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text = tempb[i];
        }
    }
}

function moves(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z = 0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == 'left') {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z = current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].value).substring(0, 12) + ',' + z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z = current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = String(attribute5.options[i].value).substring(0, 12) + ',' + z;
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text = tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5, attribute5);
}
