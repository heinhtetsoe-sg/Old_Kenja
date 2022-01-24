function btn_submit(cmd) {
    if (cmd == "change") {
        attribute4 = document.forms[0].selectdata;
        attribute4.value = "";

        sep = "";
        for (var i = 0; i < document.forms[0].left_expmcd.length; i++) {
            var schno = document.forms[0].left_expmcd.options[i].value.split("-");
            attribute4.value = attribute4.value + sep + schno[1];
            sep = ",";
        }
    }

    //取消
    if (cmd == "clear") {
        if (!confirm("{rval MSG106}")) {
            return false;
        } else {
            if (document.forms[0].not_select_schregno_auto_income.value != "1") {
                document.forms[0].GRADE.value = "";
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function doSubmit(cmd) {
    //削除
    if (cmd == "delete" && !confirm("{rval MSG103}")) {
        return false;
    }

    if (document.forms[0].AUTO_NO.value == "") {
        alert("{rval MSG301}\n( ID )");
        return true;
    }

    if (cmd == "add" || cmd == "update") {
        if (document.forms[0].AUTO_NAME.value == "") {
            alert("{rval MSG301}\n( 名称 )");
            return true;
        }
        if (document.forms[0].COLLECT_LM_CD.value == "") {
            alert("{rval MSG310}\n( 入金項目 )");
            return true;
        }
        if (document.forms[0].INCOME_LM_CD.value == "") {
            alert("{rval MSG310}\n( 預り金項目 )");
            return true;
        }

        if (document.forms[0].not_select_schregno_auto_income.value != "1") {
            if (document.forms[0].COMMODITY_PRICE.value == "") {
                alert("{rval MSG301}\n( 単価 )");
                return true;
            }

            if (document.forms[0].left_expmcd.length == 0) {
                alert("{rval MSG916}\n生徒を選択してください。");
                return;
            }

            attribute4 = document.forms[0].selectdata;
            attribute4.value = "";

            sep = "";
            for (var i = 0; i < document.forms[0].left_expmcd.length; i++) {
                var schno = document.forms[0].left_expmcd.options[i].value.split("-");
                attribute4.value = attribute4.value + sep + schno[1];
                sep = ",";
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function move1(side, left, right) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute1;
    var attribute2;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all") {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    } else {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[y] = String(temp1[y]) + "," + y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            if (attribute1.options[i].selected) {
                y = current1++;
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[y] = String(temp1[y]) + "," + y;
            } else {
                y = current2++;
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            a[y] = String(temp1[y]) + "," + y;
        }
    }

    a.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        var val = a[i];
        var tmp = val.split(",");

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text = tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1);
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text = tempb[i];
        }
    }
    document.getElementById("LEFT_PART_NUM").innerHTML = document.forms[0].left_expmcd.options.length;
    document.getElementById("RIGHT_PART_NUM").innerHTML = document.forms[0].right_expmcd.options.length;
}

function ClearList(OptionList) {
    OptionList.length = 0;
}
