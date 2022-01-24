//////////////////////////////////////////////////////////////////////////////
//LISTtoLISTの処理

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
    var tempaa = new Array();
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
    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if (chdt == 2) {
            tempaa[y] =
                String(attribute2.options[i].value).substr(
                    String(attribute2.options[i].value).indexOf("-")
                ) +
                "," +
                y;
        } else {
            tempaa[y] = String(attribute2.options[i].value) + "," + y; //NO001
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            if (chdt == 2) {
                tempaa[y] =
                    String(attribute1.options[i].value).substr(
                        String(attribute1.options[i].value).indexOf("-")
                    ) +
                    "," +
                    y;
            } else {
                tempaa[y] = String(attribute1.options[i].value) + "," + y; //NO001
            }
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
        var tmp = val.split(",");

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

function moves(sides, chdt) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
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
    for (var i = 0; i < attribute6.length; i++) {
        z = current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        if (chdt == 2) {
            tempaa[z] =
                String(attribute6.options[i].value).substr(
                    String(attribute6.options[i].value).indexOf("-")
                ) +
                "," +
                z;
        } else {
            tempaa[z] = String(attribute6.options[i].value) + "," + z; //NO001
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z = current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        if (chdt == 2) {
            tempaa[z] =
                String(attribute5.options[i].value).substr(
                    String(attribute5.options[i].value).indexOf("-")
                ) +
                "," +
                z;
        } else {
            tempaa[z] = String(attribute5.options[i].value) + "," + z; //NO001
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(",");

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text = tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5, attribute5);
}

//////////////////////////////////////////////////////////////////////////////

//健康診断票のチェック時にフォーム選択のラジオボタンを使用可/不可にする
function KenshinCheck(n) {
    var flg = document.forms[0]["CHECK" + n].checked;

    document.forms[0]["RADIO" + n + "_1"].disabled = !flg;
    document.forms[0]["RADIO" + n + "_2"].disabled = !flg;
    document.forms[0]["RADIO" + n + "_3"].disabled = !flg;

    //健康診断票　両面印刷を使用可能にする
    document.forms[0].CHECK1_2.disabled =
        document.forms[0].CHECK1.checked == false ||
        document.forms[0].CHECK2.checked == false;
    if (document.forms[0].CHECK1_2.disabled) {
        document.forms[0].CHECK1_2.checked = false;
    }
}

//健康診断結果通知書のチェック時に「医療機関向け」「保護者向け」ラジオボタンを使用可/不可にする
function TuchiCheck(n) {
    var flg = document.forms[0]["CHECK" + n].checked;

    document.forms[0]["RADIO" + n + "_1"].disabled = !flg;
    document.forms[0]["RADIO" + n + "_2"].disabled = !flg;
}
//健康診断結果通知書（尿）のチェック時にサブチェックボックスを使用可/不可にする
function TuchiCheck_Nyou() {
    var flg = document.forms[0]["CHECK14"].checked;

    document.forms[0]["CHECK15"].disabled = !flg;
    document.forms[0]["CHECK16"].disabled = !flg;
}

//印刷
function newwin(SERVLET_URL) {
    //出力対象チェック
    if (document.forms[0].CLASS_SELECTED.length == 0) {
        alert("{rval MSG916}");
        return;
    }
    //印刷対象チェック
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
        document.forms[0].CHECK10.checked == false &&
        document.forms[0].CHECK11.checked == false &&
        document.forms[0].CHECK12.checked == false &&
        document.forms[0].CHECK13.checked == false &&
        document.forms[0].CHECK14.checked == false
    ) {
        alert("{rval MSG916}");
        return;
    }

    //提出日チェック
    if (
        document.forms[0].CHECK3.checked == false &&
        document.forms[0].CHECK4.checked == false &&
        document.forms[0].CHECK5.checked == false &&
        document.forms[0].CHECK6.checked == false &&
        document.forms[0].CHECK7.checked == false &&
        document.forms[0].CHECK8.checked == false &&
        document.forms[0].CHECK9.checked == false &&
        document.forms[0].CHECK10.checked == false &&
        document.forms[0].CHECK11.checked == false &&
        document.forms[0].CHECK12.checked == false &&
        document.forms[0].CHECK13.checked == false &&
        document.forms[0].CHECK14.checked == false
    ) {
        if (document.forms[0].SEND_DATE.value == "") {
            alert("提出日を指定して下さい。");
            return;
        }
    }

    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL + "/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function btn_submit(cmd) {
    attribute3 = document.forms[0].selectleft;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].CLASS_NAME.length; i++) {
        document.forms[0].CLASS_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CLASS_SELECTED.length; i++) {
        document.forms[0].CLASS_SELECTED.options[i].selected = 1;
        attribute3.value =
            attribute3.value +
            sep +
            document.forms[0].CLASS_SELECTED.options[i].value;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
