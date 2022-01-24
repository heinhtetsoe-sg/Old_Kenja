function btn_submit(cmd) {
    if (cmd == 'changeYear') {
        if (document.forms[0].STRT_YEAR.value == "" ||  document.forms[0].END_YEAR.value == "") {
            //どちらかが空文字なら、画面描画を更新しない。
            return false;
        }
        if (document.forms[0].STRT_YEAR.value > document.forms[0].END_YEAR.value) {
            alert('{rval MSG916}'+'開始/終了年度の指定に誤りがあります。');
            return false;
        }
    } 
    if (cmd == 'knjd625f_2') {
        tmp_list(cmd, "off");  //選択リスト設定
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//選択リスト設定
function tmp_list(cmd, submit) {
    attribute3 = document.forms[0].selectCollege;
    attribute3.value = "";
    attribute4 = document.forms[0].selectCollegeText;
    attribute4.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].COLLEGE_SELECTED.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].COLLEGE_SELECTED.options[i].value;
        attribute4.value = attribute4.value + sep + document.forms[0].COLLEGE_SELECTED.options[i].text;
        sep = ",";
    }

    if (submit == 'on') {
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }
}

function newwin(SERVLET_URL) {
    if (document.forms[0].TESTITEM_SELECTED) {
        if (document.forms[0].TESTITEM_SELECTED.length == 0 || document.forms[0].SUBCLASS_SELECTED.length == 0){
            alert('{rval MSG916}');
            return false;
        }
    }
    if (document.forms[0].COLLEGE_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return false;
    }

    //設定年月日チェック
    if (document.forms[0].STRT_YEAR.value == "") {
        alert('{rval MSG301}');
        return false;
    }
    if (document.forms[0].END_YEAR.value == "") {
        alert('{rval MSG301}');
        return false;
    }
    if (document.forms[0].STRT_YEAR.value > document.forms[0].END_YEAR.value) {
        alert('{rval MSG916}'+'開始/終了年度の指定に誤りがあります。');
        return false;
    }

    for (var i = 0; i < document.forms[0].COLLEGE_NAME.length; i++) {
        document.forms[0].COLLEGE_NAME.options[i].selected = 0;
    }
    for (var i = 0; i < document.forms[0].COLLEGE_SELECTED.length; i++) {
        document.forms[0].COLLEGE_SELECTED.options[i].selected = 1;
    }
    if (document.forms[0].TESTINFO_SELECTED) {
        for (var i = 0; i < document.forms[0].TESTINFO_NAME.length; i++) {
            document.forms[0].TESTINFO_NAME.options[i].selected = 0;
        }
        for (var i = 0; i < document.forms[0].TESTINFO_SELECTED.length; i++) {
            document.forms[0].TESTINFO_SELECTED.options[i].selected = 1;
        }
    }

    if (document.forms[0].SUBCLASS_SELECTED) {
        for (var i = 0; i < document.forms[0].SUBCLASS_NAME.length; i++) {
            document.forms[0].SUBCLASS_NAME.options[i].selected = 0;
        }
        for (var i = 0; i < document.forms[0].SUBCLASS_SELECTED.length; i++) {
            document.forms[0].SUBCLASS_SELECTED.options[i].selected = 1;
        }
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

function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}

function move1(side, categoryName, categorySelected, jsSort) {
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
        attribute1 = document.forms[0][categoryName];
        attribute2 = document.forms[0][categorySelected];
    } else {
        attribute1 = document.forms[0][categorySelected];
        attribute2 = document.forms[0][categoryName];
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

    if (jsSort != "0") {
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

function moves(sides, categoryName, categorySelected, jsSort) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;

    if (sides == "left") {
        attribute5 = document.forms[0][categoryName];
        attribute6 = document.forms[0][categorySelected];
    } else {
        attribute5 = document.forms[0][categorySelected];
        attribute6 = document.forms[0][categoryName];
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

    if (jsSort != "0") {
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
