function btn_submit(cmd) {
    seq = '';
    for (var i = 0; i < document.forms[0].SORT_SELECTED.length; i++) {
        document.forms[0].SORT_SELECTED_HIDDEN.value += seq + document.forms[0].SORT_SELECTED.options[i].value;
        seq = ',';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷・プレビューボタン
function newwin(SERVLET_URL) {
    if (document.forms[0].DATE1.value == "") {
        alert("日付が不正です。");
        document.forms[0].DATE1.focus();
        return;
    }

    if (document.forms[0].DATE2.value == "") {
        alert("日付が不正です。");
        document.forms[0].DATE2.focus();
        return;
    }

    var chk_sdate = document.forms[0].CHK_SDATE.value; //学期開始日付
    var chk_edate = document.forms[0].CHK_EDATE.value; //学期終了日付

    var date1 = document.forms[0].DATE1.value; //印刷範囲開始日付
    var date2 = document.forms[0].DATE2.value; //印刷範囲終了日付

    if (date1 > date2) {
        alert("日付の大小が不正です。");
        return;
    }

    if((date1 < chk_sdate) || (date2 > chk_edate)){
        alert("日付が範囲外です。\n（" + chk_sdate + "～" + chk_edate + "） ");
        return;
    }

    if (!document.forms[0].NAIKA.checked &&
        !document.forms[0].GEKA.checked &&
        !document.forms[0].KENKO_SODAN.checked &&
        !document.forms[0].SONOTA.checked &&
        !document.forms[0].SEITO_IGAI.checked) {
        alert("出力する帳票を選択して下さい。");
        return false;
    }

    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert('出力対象クラスを指定してください');
        return;
    }

    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
    }

    for (var i = 0; i < document.forms[0].SORT_NAME.length; i++) {
        document.forms[0].SORT_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].SORT_SELECTED.length; i++) {
        document.forms[0].SORT_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();
    document.forms[0].action = action;
    document.forms[0].target = target;
}


function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName)　{
        attribute = document.forms[0].CLASS_NAME;
        ClearList(attribute,attribute);
        attribute = document.forms[0].CLASS_SELECTED;
        ClearList(attribute,attribute);
}

/**********************/
/* リストtoリスト関係 */
/**********************/
function move1(side, kind) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempaa = new Array();
    var tempb = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;

    if (side == "left") {
        if (kind == 'class') {
            attribute1 = document.forms[0].CLASS_NAME;
            attribute2 = document.forms[0].CLASS_SELECTED;
        } else {
            attribute1 = document.forms[0].SORT_NAME;
            attribute2 = document.forms[0].SORT_SELECTED;
        }
    } else {
        if (kind == 'class') {
            attribute1 = document.forms[0].CLASS_SELECTED;
            attribute2 = document.forms[0].CLASS_NAME;
        } else {
            attribute1 = document.forms[0].SORT_SELECTED;
            attribute2 = document.forms[0].SORT_NAME;
        }
    }

    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

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

    if (kind == 'class') {
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

function moves(sides, kind) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    if (sides == "left") {
        if (kind == 'class') {
            attribute5 = document.forms[0].CLASS_NAME;
            attribute6 = document.forms[0].CLASS_SELECTED;
        } else {
            attribute5 = document.forms[0].SORT_NAME;
            attribute6 = document.forms[0].SORT_SELECTED;
        }
    } else {
        if (kind == 'class') {
            attribute5 = document.forms[0].CLASS_SELECTED;
            attribute6 = document.forms[0].CLASS_NAME;
        } else {
            attribute5 = document.forms[0].SORT_SELECTED;
            attribute6 = document.forms[0].SORT_NAME;
        }
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value+","+z;
    }

    if (kind == 'class') {
        tempaa.sort();
    }

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);
}
