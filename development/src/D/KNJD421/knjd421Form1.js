function btn_submit(cmd) {
    var i;
    var sel = document.forms[0].selectdata;
    sel.value = "";
    sep = "";
    for (i = 0; i < document.forms[0].CATEGORY_NAME2.length; i++) {
        document.forms[0].CATEGORY_NAME2.options[i].selected = 0;
    }

    for (i = 0; i < document.forms[0].CATEGORY_SELECTED2.length; i++) {
        document.forms[0].CATEGORY_SELECTED2.options[i].selected = 1;
        sel.value = sel.value + sep + document.forms[0].CATEGORY_SELECTED2.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    var e;
    if (document.forms[0].CATEGORY_SELECTED.length == 0 || document.forms[0].CATEGORY_SELECTED2.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].DATE.value == '') {
        alert("日付が未入力です。");
        return;
    }
    if (document.forms[0].DESC_DATE.value == '') {
        alert("記載日付が未入力です。");
        return;
    }
    for (i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
        document.forms[0].CATEGORY_NAME.options[i].selected = 0;
    }

    for (i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        var val = document.forms[0].CATEGORY_SELECTED.options[i].value.split("-");
        if (val.length > 1) {
            document.forms[0].CATEGORY_SELECTED.options[i].value = val[2];
        }
        document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
    }

    for (i = 0; i < document.forms[0].CATEGORY_NAME2.length; i++) {
        document.forms[0].CATEGORY_NAME2.options[i].selected = 0;
    }

    for (i = 0; i < document.forms[0].CATEGORY_SELECTED2.length; i++) {
        document.forms[0].CATEGORY_SELECTED2.options[i].selected = 1;
    }
    if (document.forms[0].HUKUSIKI_RADIO) {
        e = document.forms[0].HUKUSIKI_RADIO2;
        document.forms[0].SELECT_GHR.value = e.checked ? "1" : "";
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}


//印刷面チェック
function kubun(obj)
{
    if (document.getElementById("OUTPUT_DIV1").checked) {
        if (document.forms[0].PRINT_SIDE1.checked == false && 
            document.forms[0].PRINT_SIDE2.checked == false && 
            (document.forms[0].PRINT_SIDE3.disabled || document.forms[0].PRINT_SIDE3.checked == false) && 
            document.forms[0].PRINT_SIDE4.checked == false) {
            document.forms[0].btn_print.disabled = true;
        } else {
            document.forms[0].btn_print.disabled = false;
        }
    } else if (document.getElementById("OUTPUT_DIV2").checked) {
        document.forms[0].btn_print.disabled = false;
    }

    if (document.forms[0].PRINT_SIDE1_ATTEND) {
        var dis2 = document.forms[0].PRINT_SIDE1_ATTEND.checked;
        document.forms[0].PRINT_SIDE3.disabled = dis2;
        if (obj == document.forms[0].PRINT_SIDE1_ATTEND && dis2) {
            document.forms[0].PRINT_SIDE1.checked = true;
        }
    }
}

if (window.addEventListener) {
    window.addEventListener('load', function() {
        kubun();
        chkcat2();
    });
}


function chkcat2() {
    document.forms[0].SCHOOL_KIND.style.visibility = document.getElementById("HUKUSIKI_RADIO1").checked ? "visible" : "collapse";
}

/****************************************** リストtoリスト ********************************************/
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move1(side, selLeft, selRight, sortFlg) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute1, attribute2;

    if (side == "left") {
        attribute1 = document.getElementsByName(selRight)[0];
        attribute2 = document.getElementsByName(selLeft)[0];
    } else {
        attribute1 = document.getElementsByName(selLeft)[0];
        attribute2 = document.getElementsByName(selRight)[0];
    }

    for (var i = 0; i < attribute2.length; i++) {
        y=current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value+","+y;
        } else {
            y=current2++;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    if (sortFlg) {
        tempaa.sort();
    }

    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

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

function moves(side, selLeft, selRight, sortFlg) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    var attribute5, attribute5;

    if (side == "left") {
        attribute5 = document.getElementsByName(selRight)[0];
        attribute6 = document.getElementsByName(selLeft)[0];
    } else {
        attribute5 = document.getElementsByName(selLeft)[0];
        attribute6 = document.getElementsByName(selRight)[0];
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value+","+z;
    }

    if (sortFlg) {
        tempaa.sort();
    }

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5);
}
