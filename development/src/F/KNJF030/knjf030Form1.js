function btn_submit(cmd) {
    attribute3 = document.forms[0].selectleft;
    attribute3.value = "";
    sep = "";
    for (var cnt = 0; cnt < document.forms[0].CLASS_NAME.length; cnt++) {
        document.forms[0].CLASS_NAME.options[cnt].selected = 0;
    }

    for (var cnt = 0; cnt < document.forms[0].CLASS_SELECTED.length; cnt++) {
        document.forms[0].CLASS_SELECTED.options[cnt].selected = 1;
        attribute3.value = attribute3.value + sep + document.forms[0].CLASS_SELECTED.options[cnt].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    //出力対象チェック
    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert("{rval MSG916}");
        return;
    }
    //印刷対象チェック
    if (document.forms[0].SCHOOLNAME.value == "risshisha") {
        if (document.forms[0].CHECK1.checked == false && document.forms[0].CHECK2.checked == false) {
            alert("{rval MSG916}");
            return;
        }
    } else {
        if (
            document.forms[0].CHECK1.checked == false &&
            document.forms[0].CHECK2.checked == false &&
            document.forms[0].CHECK3.checked == false &&
            document.forms[0].CHECK4.checked == false &&
            document.forms[0].CHECK5.checked == false &&
            document.forms[0].CHECK6.checked == false &&
            document.forms[0].CHECK7.checked == false &&
            document.forms[0].CHECK8.checked == false &&
            document.forms[0].CHECK9.checked == false &&
            (!document.forms[0].CHECK10 || document.forms[0].CHECK10.checked == false) &&
            (!document.forms[0].CHECK11 || document.forms[0].CHECK11.checked == false) &&
            (!document.forms[0].CHECK12 || document.forms[0].CHECK12.checked == false) &&
            (!document.forms[0].CHECK13 || document.forms[0].CHECK13.checked == false)
        ) {
            if (document.forms[0].KenkouSindan_Ippan_Pattern.value == "1") {
                if (document.forms[0].URINALYSIS_CHECK.checked == false) {
                    alert("{rval MSG916}");
                    return;
                }
            } else {
                alert("{rval MSG916}");
                return;
            }
        }
        //未受検項目チェック
        if (document.forms[0].CHECK3.checked == true) {
            if (
                document.forms[0].MIJUKEN_ITEM01.checked == false &&
                document.forms[0].MIJUKEN_ITEM02.checked == false &&
                document.forms[0].MIJUKEN_ITEM03.checked == false &&
                document.forms[0].MIJUKEN_ITEM04.checked == false &&
                document.forms[0].MIJUKEN_ITEM05.checked == false &&
                document.forms[0].MIJUKEN_ITEM06.checked == false
            ) {
                alert("未受検項目を指定して下さい。");
                return;
            }
        }
        //定期健康診断異常者一覧表チェック
        if (document.forms[0].CHECK9.checked == true) {
            ind = document.forms[0].SELECT1.selectedIndex;
            ind2 = document.forms[0].SELECT2.selectedIndex;
            if (document.forms[0].SELECT1.options[ind].value == "01" && document.forms[0].SELECT2.options[ind2].value == "01") {
                alert("一般条件または歯・口腔条件を指定して下さい。");
                return;
            }
        }
        //提出日チェック
        if (document.forms[0].CHECK3.checked == true || document.forms[0].CHECK4.checked == true) {
            if (document.forms[0].DATE.value == "") {
                alert("提出日を指定して下さい。");
                return;
            }
        }
        //未受検項目チェック
        if (document.forms[0].CHECK7.checked == true) {
            if (document.forms[0].DATE7) {
                if (document.forms[0].DATE7.value == "") {
                    alert("作成日を指定して下さい。");
                    return;
                }
            }
        }
        //視力の検査結果チェック
        if (document.forms[0].CHECK11 && document.forms[0].CHECK11.checked == true) {
            ind = document.forms[0].SIGHT_CONDITION.selectedIndex;
            if (document.forms[0].SELECT1.options[ind].value == "01") {
                alert("視力条件を指定して下さい。");
                return;
            }
        }
    }

    for (var cnt = 0; cnt < document.forms[0].CLASS_NAME.length; cnt++) {
        document.forms[0].CLASS_NAME.options[cnt].selected = 0;
    }

    for (var cnt = 0; cnt < document.forms[0].CLASS_SELECTED.length; cnt++) {
        document.forms[0].CLASS_SELECTED.options[cnt].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    //    document.forms[0].action = "/cgi-bin/printenv.pl";
    //    url = location.hostname;
    //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].CLASS_NAME;
    ClearList(attribute, attribute);
    attribute = document.forms[0].CLASS_SELECTED;
    ClearList(attribute, attribute);
}

function move1(side, chdt) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array(); // 2004/01/26
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].CLASS_NAME;
        attribute2 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute1 = document.forms[0].CLASS_SELECTED;
        attribute2 = document.forms[0].CLASS_NAME;
    }

    //fill an array with old values
    for (var cnt = 0; cnt < attribute2.length; cnt++) {
        y = current1++;
        temp1[y] = attribute2.options[cnt].value;
        tempa[y] = attribute2.options[cnt].text;
        if (chdt == 2) {
            tempaa[y] = String(attribute2.options[cnt].value).substr(String(attribute2.options[cnt].value).indexOf("-")) + "," + y;
        } else {
            tempaa[y] = String(attribute2.options[cnt].value) + "," + y; // NO001
        }
    }

    //assign new values to arrays
    for (var cnt = 0; cnt < attribute1.length; cnt++) {
        if (attribute1.options[cnt].selected) {
            y = current1++;
            temp1[y] = attribute1.options[cnt].value;
            tempa[y] = attribute1.options[cnt].text;
            if (chdt == 2) {
                tempaa[y] = String(attribute1.options[cnt].value).substr(String(attribute1.options[cnt].value).indexOf("-")) + "," + y;
            } else {
                tempaa[y] = String(attribute1.options[cnt].value) + "," + y; // NO001
            }
        } else {
            y = current2++;
            temp2[y] = attribute1.options[cnt].value;
            tempb[y] = attribute1.options[cnt].text;
        }
    }

    tempaa.sort(); // 2004/01/26

    //generating new options // 2004/01/26
    for (var cnt = 0; cnt < temp1.length; cnt++) {
        var val = tempaa[cnt];
        var tmp = val.split(",");

        attribute2.options[cnt] = new Option();
        attribute2.options[cnt].value = temp1[tmp[1]];
        attribute2.options[cnt].text = tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1, attribute1);
    if (temp2.length > 0) {
        for (var cnt = 0; cnt < temp2.length; cnt++) {
            attribute1.options[cnt] = new Option();
            attribute1.options[cnt].value = temp2[cnt];
            attribute1.options[cnt].text = tempb[cnt];
        }
    }
}

function moves(sides, chdt) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array(); // 2004/01/26
    var current5 = 0;
    var z = 0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].CLASS_NAME;
        attribute6 = document.forms[0].CLASS_SELECTED;
    } else {
        attribute5 = document.forms[0].CLASS_SELECTED;
        attribute6 = document.forms[0].CLASS_NAME;
    }

    //fill an array with old values
    for (var cnt = 0; cnt < attribute6.length; cnt++) {
        z = current5++;
        temp5[z] = attribute6.options[cnt].value;
        tempc[z] = attribute6.options[cnt].text;
        if (chdt == 2) {
            tempaa[z] = String(attribute6.options[cnt].value).substr(String(attribute6.options[cnt].value).indexOf("-")) + "," + z;
        } else {
            tempaa[z] = String(attribute6.options[cnt].value) + "," + z; // NO001
        }
    }

    //assign new values to arrays
    for (var cnt = 0; cnt < attribute5.length; cnt++) {
        z = current5++;
        temp5[z] = attribute5.options[cnt].value;
        tempc[z] = attribute5.options[cnt].text;
        if (chdt == 2) {
            tempaa[z] = String(attribute5.options[cnt].value).substr(String(attribute5.options[cnt].value).indexOf("-")) + "," + z;
        } else {
            tempaa[z] = String(attribute5.options[cnt].value) + "," + z; // NO001
        }
    }

    tempaa.sort(); // 2004/01/26

    //generating new options // 2004/01/26
    for (var cnt = 0; cnt < temp5.length; cnt++) {
        var val = tempaa[cnt];
        var tmp = val.split(",");

        attribute6.options[cnt] = new Option();
        attribute6.options[cnt].value = temp5[tmp[1]];
        attribute6.options[cnt].text = tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5, attribute5);
}

function DataUse(obj1) {
    if (document.forms[0].CHECK3.checked == true || document.forms[0].CHECK4.checked == true) {
        flag1 = false;
    } else {
        flag1 = true;
    }
    document.forms[0].DATE.disabled = flag1;
    document.forms[0].btn_calen.disabled = flag1;
}

function SelectUse(obj2) {
    if (document.forms[0].CHECK9.checked == true) {
        document.forms[0].SELECT1.disabled = false;
        document.forms[0].SELECT2.disabled = false;
    } else {
        document.forms[0].SELECT1.disabled = true;
        document.forms[0].SELECT2.disabled = true;
    }
}

function OptionUse(obj3) {
    if (document.forms[0].CHECK6.checked == true) {
        document.forms[0].OUTPUT[0].disabled = false;
        document.forms[0].OUTPUT[1].disabled = false;
    } else {
        document.forms[0].OUTPUT[0].disabled = true;
        document.forms[0].OUTPUT[1].disabled = true;
    }
}

function OptionUse2(obj4) {
    var dis = document.forms[0].CHECK1.checked == false;
    document.forms[0].OUTPUTA[0].disabled = dis;
    document.forms[0].OUTPUTA[1].disabled = dis;
    document.forms[0].PRINT_STAMP.disabled = dis;
    document.forms[0].PRINT_SCHREGNO1.disabled = dis;
    if (document.forms[0].useForm9_PJ_Ippan) {
        document.forms[0].useForm9_PJ_Ippan.disabled = dis;
    }
    if (document.forms[0].useForm7_JH_Ippan) {
        document.forms[0].useForm7_JH_Ippan.disabled = dis;
    }
    document.forms[0].CHECK1_2.disabled = document.forms[0].CHECK1.checked == false || document.forms[0].CHECK2.checked == false;
    if (document.forms[0].CHECK1_2.disabled) {
        document.forms[0].CHECK1_2.checked = false;
        for (var cnt = 3; cnt <= 13; cnt++) {
            if (document.forms[0]["CHECK" + cnt]) {
                document.forms[0]["CHECK" + cnt].disabled = false;
            }
        }
    }
}

function OptionUse3(obj5) {
    var dis = document.forms[0].CHECK2.checked == false;
    document.forms[0].OUTPUTB[0].disabled = dis;
    document.forms[0].OUTPUTB[1].disabled = dis;
    if (document.forms[0].PRINT_STAMP2 != null) {
        document.forms[0].PRINT_STAMP2.disabled = dis;
    }
    document.forms[0].PRINT_SCHREGNO2.disabled = dis;
    if (document.forms[0].useForm9_PJ_Ha) {
        document.forms[0].useForm9_PJ_Ha.disabled = dis;
    }
    if (document.forms[0].useForm7_JH_Ha) {
        document.forms[0].useForm7_JH_Ha.disabled = dis;
    }
    document.forms[0].CHECK1_2.disabled = document.forms[0].CHECK1.checked == false || document.forms[0].CHECK2.checked == false;
    if (document.forms[0].CHECK1_2.disabled) {
        document.forms[0].CHECK1_2.checked = false;
        for (var cnt = 3; cnt <= 13; cnt++) {
            if (document.forms[0]["CHECK" + cnt]) {
                document.forms[0]["CHECK" + cnt].disabled = false;
            }
        }
    }
}

function dis_date(flag) {
    document.forms[0].DATE.disabled = flag;
    document.forms[0].btn_calen.disabled = flag;
}

function dis_date5() {
    var flag = !document.forms[0].CHECK5.checked;
    document.forms[0].DATE5.disabled = flag;
    document.forms[0].btn_calen.disabled = flag;
}
//３）両面印刷チェックボックス
function OptionUse1_2(obj) {
    if (document.forms[0].CHECK1_2.checked == true) {
        for (var cnt = 3; cnt <= 13; cnt++) {
            if (document.forms[0]["CHECK" + cnt]) {
                document.forms[0]["CHECK" + cnt].disabled = true;
            }
        }
    } else {
        for (var cnt = 3; cnt <= 13; cnt++) {
            if (document.forms[0]["CHECK" + cnt]) {
                document.forms[0]["CHECK" + cnt].disabled = false;
            }
        }
    }
}
//６）定期健康診断結果チェックボックス(※kumamotoは７)
function dis_check7() {
    if (document.forms[0].CHECK7.checked == true) {
        //作成日カレンダー(宮城)
        if (document.forms[0].DATE7) {
            document.forms[0].DATE7.disabled = false;
        }
        //家庭連絡コメント記入checkbox(熊本)
        if (document.forms[0].FAMILY_CONTACT_COMMENT) {
            document.forms[0].FAMILY_CONTACT_COMMENT.disabled = false;
        }
        //標準体重・肥満度チェックボックス
        if (document.forms[0].STANDARD_NOTSHOW) {
            document.forms[0].STANDARD_NOTSHOW.disabled = false;
        }
    } else {
        if (document.forms[0].DATE7) {
            document.forms[0].DATE7.disabled = true;
        }
        if (document.forms[0].FAMILY_CONTACT_COMMENT) {
            document.forms[0].FAMILY_CONTACT_COMMENT.disabled = true;
        }
        if (document.forms[0].STANDARD_NOTSHOW) {
            document.forms[0].STANDARD_NOTSHOW.disabled = true;
        }
    }
}

//６）定期健康診断結果 文面をチップヘルプで表示(※kumamotoは７)
function ViewDocument(obj) {
    var msg = "";
    msg = document.forms[0]["DOUMENT_TEXT-" + obj.value].value;

    x = event.clientX + document.body.scrollLeft;
    y = event.clientY + document.body.scrollTop;
    document.all("lay").innerHTML = msg;
    document.all["lay"].style.width = "590px";
    document.all["lay"].style.position = "absolute";
    document.all["lay"].style.left = x + 5;
    document.all["lay"].style.top = y + 10;
    document.all["lay"].style.padding = "4px 3px 3px 8px";
    document.all["lay"].style.border = "1px solid";
    document.all["lay"].style.visibility = "visible";
    document.all["lay"].style.background = "#ccffff";
}
function ViewDocumentMouseout() {
    document.all["lay"].style.visibility = "hidden";
}

//７）尿検査診断結果チェックボックス
function dis_urinalysis() {
    //尿検査診断結果チェックボックス
    if (document.forms[0].URINALYSIS_CHECK.checked == true) {
        //検査結果ラジオボタン
        document.forms[0].URINALYSIS_OUTPUT[0].disabled = false;
        document.forms[0].URINALYSIS_OUTPUT[1].disabled = false;
    } else {
        document.forms[0].URINALYSIS_OUTPUT[0].disabled = true;
        document.forms[0].URINALYSIS_OUTPUT[1].disabled = true;
    }
}

//１２）視力の検査結果のお知らせチェックボックス(熊本)
function dis_cmb11(obj) {
    if (obj.checked == true) {
        if (document.forms[0].SIGHT_CONDITION) {
            document.forms[0].SIGHT_CONDITION.disabled = false;
        }
    } else {
        if (document.forms[0].SIGHT_CONDITION) {
            document.forms[0].SIGHT_CONDITION.disabled = true;
        }
    }
}
