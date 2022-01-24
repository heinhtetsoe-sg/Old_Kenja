function btn_submit(cmd) {
    setTestkindcd_Score_flg();
    setRemark_flg();

    if (cmd == "csv"){
        //選択チェック（クラス）
        if (document.forms[0].CLASS_SELECTED.length == 0) {
            alert('{rval MSG916}');
            return false;
        }
        //日付チェック（未入力）
        if (document.forms[0].EDATE.value == '') {
            alert("日付が不正です。");
            document.forms[0].EDATE.focus();
            return false;
        }
        //日付チェック（学期範囲）
        var day   = document.forms[0].EDATE.value;       //印刷範囲日付
        var sdate = document.forms[0].SEME_SDATE.value;  //学期開始日付
        var edate = document.forms[0].SEME_EDATE.value;  //学期終了日付
        var flag1 = document.forms[0].SEME_FLG.value;
        if (sdate > day || edate < day) {
            alert("日付が学期の範囲外です");
            return;
        }

        //選択クラスをhiddenで保持
        for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
            document.forms[0].CLASS_NAME.options[i].selected = 0;
        }
        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
            document.forms[0].CLASS_SELECTED.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].CLASS_SELECTED.options[i].value;
            sep = ",";
        }

        //選択学期名・テスト名をhiddenで保持
        attribute1 = document.forms[0].SEMESTER;
        attribute2 = document.forms[0].SUB_TESTKINDCD;
        document.forms[0].selectSemeName.value = attribute1.options[attribute1.selectedIndex].text;
        var testName = attribute2.options[attribute2.selectedIndex].text.split(':');
        document.forms[0].selectTestName.value = testName[1];
    }

    document.forms[0].DATE.value = document.forms[0].EDATE.value;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function setTestkindcd_Score_flg() {
    var subTestCd = document.forms[0].SUB_TESTKINDCD;
    document.forms[0].TESTKINDCD.value = subTestCd.value.substr(0, 4);
    document.forms[0].SCORE_FLG.value  = subTestCd.value.substr(5, 1);
}

function setRemark_flg() {
    if(document.forms[0].SUBCLASS_MAX[0].checked == true) {
        document.forms[0].REMARK.value = "1";
    } else {
        if(document.forms[0].REMARK_SELECT[0].checked == true) {
            document.forms[0].REMARK.value = document.forms[0].REMARK_SELECT[0].value;
        } else {
            document.forms[0].REMARK.value = document.forms[0].REMARK_SELECT[1].value;
        }
    }
}

function newwin(SERVLET_URL) {
    setTestkindcd_Score_flg();
    setRemark_flg();

    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert('{rval MSG916}');
        return false;
    }

    if (document.forms[0].EDATE.value == '') {
        alert("日付が不正です。");
        document.forms[0].EDATE.focus();
        return false;
    }

    var day   = document.forms[0].EDATE.value;       //印刷範囲日付
    var sdate = document.forms[0].SEME_SDATE.value;  //学期開始日付
    var edate = document.forms[0].SEME_EDATE.value;  //学期終了日付
    var flag1 = document.forms[0].SEME_FLG.value;

    if (sdate > day || edate < day) {
        alert("日付が学期の範囲外です");
        return;
    }

    document.forms[0].DATE.value = document.forms[0].EDATE.value;

    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
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

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move1(side) {
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
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;  
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

    tempaa.sort();

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

function moves(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z=0;
    
    if (sides == "left") {
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;  
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

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);

}

//disabled
function OptionUse(obj) {

    if(document.forms[0].SUBCLASS_MAX[1].checked == true) {
        document.forms[0].REMARK_SELECT[0].disabled = false;
        document.forms[0].REMARK_SELECT[1].disabled = false;
    } else {
        document.forms[0].REMARK_SELECT[0].disabled = true;
        document.forms[0].REMARK_SELECT[1].disabled = true;
    }
}
