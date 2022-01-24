function btn_submit(cmd) {
    attribute3 = document.forms[0].selectleft;
    selectleftval = document.forms[0].selectleftval;
    attribute3.value = "";
    selectleftval.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
        document.forms[0].CATEGORY_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
        attribute3.value =
            attribute3.value +
            sep +
            document.forms[0].CATEGORY_SELECTED.options[i].value;
        selectleftval.value =
            selectleftval.value +
            sep +
            document.forms[0].CATEGORY_SELECTED.options[i].text;
        sep = ",";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    if (document.forms[0].CATEGORY_SELECTED.length == 0) {
        alert("{rval MSG916}");
        return;
    }

    //日付入力チェック
    if (
        document.forms[0].END_ATTENDANCE.value == "" ||
        document.forms[0].START_ATTENDANCE.value == ""
    ) {
        alert("日付の指定範囲が不正です");
        document.forms[0].END_ATTENDANCE.focus();
        return false;
    } else {
        //年月固定チェック
        var strtspl = document.forms[0].START_ATTENDANCE.value.split("/");
        var endspl = document.forms[0].END_ATTENDANCE.value.split("/");
        if (strtspl.length < 2 || endspl.length < 2) {
            alert("出欠集計範囲が不正です");
            if (strtspl.length < 2) {
                document.forms[0].START_ATTENDANCE.focus();
            } else {
                document.forms[0].END_ATTENDANCE.focus();
            }
            return false;
        }
        //開始日付 > 終了日付
        var startDate = new Date(
            parseInt(strtspl[0]),
            parseInt(strtspl[1]) - 1,
            parseInt(strtspl[2])
        );
        var endDate = new Date(
            parseInt(endspl[0]),
            parseInt(endspl[1]) - 1,
            parseInt(endspl[2])
        );
        if (startDate > endDate) {
            alert("出欠集計範囲が不正です");
            document.forms[0].END_ATTENDANCE.focus();
            return false;
        }
        //開始日付 > 終了日付
        if (
            checkYMD(
                document.forms[0].START_ATTENDANCE.value,
                document.forms[0].END_ATTENDANCE.value
            ) == false
        ) {
            alert("出欠集計範囲が不正です。年度内で選択してください");
            document.forms[0].END_ATTENDANCE.focus();
            return false;
        }
    }

    for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
        document.forms[0].CATEGORY_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

    document.forms[0].action = SERVLET_URL + "/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function move1(side, disp) {
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
        if (disp == 1) {
            tempaa[y] =
                String(attribute2.options[i].value).substr(
                    String(attribute2.options[i].value).indexOf("-")
                ) +
                "," +
                y;
        } else {
            tempaa[y] = String(attribute2.options[i].value) + "," + y;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            if (disp == 1) {
                tempaa[y] =
                    String(attribute1.options[i].value).substr(
                        String(attribute1.options[i].value).indexOf("-")
                    ) +
                    "," +
                    y;
            } else {
                tempaa[y] = String(attribute1.options[i].value) + "," + y;
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

function moves(sides, disp) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z = 0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
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
        if (disp == 1) {
            tempaa[z] =
                String(attribute6.options[i].value).substr(
                    String(attribute6.options[i].value).indexOf("-")
                ) +
                "," +
                z;
        } else {
            tempaa[z] = String(attribute6.options[i].value) + "," + z;
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z = current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        if (disp == 1) {
            tempaa[z] =
                String(attribute5.options[i].value).substr(
                    String(attribute5.options[i].value).indexOf("-")
                ) +
                "," +
                z;
        } else {
            tempaa[z] = String(attribute5.options[i].value) + "," + z;
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

//選択した月が年度内に収まっているか確認
function checkYMD(startCheck, endCheck) {
    var start = Number(document.forms[0].CTRL_YEAR.value) + "/4/01";
    var end = Number(document.forms[0].CTRL_YEAR.value) + 1 + "/3/31";

    var strtspl = start.split("/");
    var endspl = end.split("/");

    //年度内の測定基準値
    var startDate = new Date(
        parseInt(strtspl[0]),
        parseInt(strtspl[1]) - 1,
        parseInt(strtspl[2])
    );
    var endDate = new Date(
        parseInt(endspl[0]),
        parseInt(endspl[1]) - 1,
        parseInt(endspl[2])
    );

    var strtCheckSpl = startCheck.split("/");
    var endCheckSpl = endCheck.split("/");

    //年度内測定値
    var startCheckDate = new Date(
        parseInt(strtCheckSpl[0]),
        parseInt(strtCheckSpl[1]) - 1,
        parseInt(strtCheckSpl[2])
    );
    var endCheckDate = new Date(
        parseInt(endCheckSpl[0]),
        parseInt(endCheckSpl[1]) - 1,
        parseInt(endCheckSpl[2])
    );

    if (
        startDate > startCheckDate ||
        endDate < startCheckDate ||
        startDate > endCheckDate ||
        endDate < endCheckDate
    ) {
        return false;
    } else {
        if (startCheckDate > endCheckDate) {
            return false;
        }
        return true;
    }
}
