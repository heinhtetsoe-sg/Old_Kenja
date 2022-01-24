function btn_submit(cmd)
{
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL)
{
    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].DATE.value == '') {
        alert("日付が不正です。");
        return false;
    }

    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }
    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;

    return false;
}

function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function move1(side)
{
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    if (side == "left") {
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;
    }

    for (var i = 0; i < attribute2.length; i++) {
        y = current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if (document.forms[0].CATEGORY_IS_CLASS1.checked) {
            tempaa[y] = attribute2.options[i].value + "__" + y;
        } else {
            tempaa[y] = attribute2.options[i].text + "__" + y;
        }
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y = current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            if (document.forms[0].CATEGORY_IS_CLASS1.checked) {
                tempaa[y] = attribute1.options[i].value + "__" + y;
            } else {
                tempaa[y] = attribute1.options[i].text + "__" + y;
            }
        } else {
            y = current2++
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split('__');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    ClearList(attribute1,attribute1);
    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }
}

function moves(sides)
{
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z = 0;

    if (sides == "left") {
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;
    }

    for (var i = 0; i < attribute6.length; i++) {
        z = current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        if (document.forms[0].CATEGORY_IS_CLASS1.checked) {
            tempaa[z] = attribute6.options[i].value + "__" + z;
        } else {
            tempaa[z] = attribute6.options[i].text + "__" + z;
        }
    }

    for (var i = 0; i < attribute5.length; i++) {
        z = current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        if (document.forms[0].CATEGORY_IS_CLASS1.checked) {
            tempaa[z] = attribute5.options[i].value + "__" + z;
        } else {
            tempaa[z] = attribute5.options[i].text + "__" + z;
        }
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split('__');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);
}
