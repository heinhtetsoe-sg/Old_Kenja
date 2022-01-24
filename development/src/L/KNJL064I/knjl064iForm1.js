function btn_submit(cmd) {
    if (cmd == "exec" && !confirm("{rval MSG101}")) {
        return;
    }
    if (cmd == "reset" && !confirm("{rval MSG106}")) {
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新
function doSubmit(cmd) {
    if (document.forms[0].BASE_SELECTED.length == 0 && document.forms[0].BEFORE_SELECTED.length == 0) {
        alert("{rval MSG304}");
        return false;
    }
    if (cmd == "exec" && !confirm("{rval MSG102}")) {
        return;
    }

    //上段
    var data = "";
    sep = "";
    if (document.forms[0].BASE_SELECTED.length > 0) {
        for (var i = 0; i < document.forms[0].BASE_SELECTED.length; i++) {
            data += sep + document.forms[0].BASE_SELECTED.options[i].value;
            sep = ",";
        }
    }
    document.forms[0].upd_data_base.value = data;

    //下段
    data = "";
    sep = "";
    if (document.forms[0].BEFORE_SELECTED.length > 0) {
        for (var i = 0; i < document.forms[0].BEFORE_SELECTED.length; i++) {
            data += sep + document.forms[0].BEFORE_SELECTED.options[i].value;
            sep = ",";
        }
    }
    document.forms[0].upd_data_before.value = data;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError() {
    alert("{rval MSG300}");
    closeWin();
}

function ClearList(OptionList) {
    OptionList.length = 0;
}

function move2(side, left, right, sort, errFlg) {
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
        a[temp1[y]] = tempa[y];
    }
    //assign new values to arrays
    var dCntArray = new Object(); //増分
    for (var i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            if (attribute1.options[i].selected) {
                var errcheck = attribute1.options[i].value.split("-");
                var multiOther = document.forms[0]["MULTI_OTHER_" + errcheck[0]];

                if (multiOther) {
                    if (dCntArray[errcheck[0]] === undefined) {
                        dCntArray[errcheck[0]] = 0;
                    }
                    dCntArray[errcheck[0]]++;
                }

                y = current1++;
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[temp1[y]] = tempa[y];
            } else {
                y = current2++;
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            a[temp1[y]] = tempa[y];
        }
    }

    var keys = Object.keys(dCntArray);
    for (var i = 0; i < keys.length; i++) {
        var key = keys[i];
        if (side == "left") {
            if (errFlg && side == "left" && Number(document.forms[0]["MULTI_OTHER_" + key].value) + dCntArray[key] > 1) {
                alert("照合対象者に受験番号は重複して指定できません。\n(受験番号「" + key + "」)");
                attribute1.options[i].selected = false;
                return false;
            }

            document.forms[0]["MULTI_OTHER_" + key].value = Number(document.forms[0]["MULTI_OTHER_" + key].value) + dCntArray[key];
        } else {
            document.forms[0]["MULTI_OTHER_" + key].value = Number(document.forms[0]["MULTI_OTHER_" + key].value) - dCntArray[key];
        }
    }

    if (sort) {
        //sort
        temp1 = temp1.sort();
        //generating new options
        for (var i = 0; i < temp1.length; i++) {
            tempa[i] = a[temp1[i]];
        }
    }

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text = tempa[i];
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
}

function moveAll(side, left, right, sort) {
    var attribute;
    var moto = side == "left" ? right : left;

    //全選択
    var errFlg = false;
    var errExamStr = "";
    var sep = "";
    var ignoreExamArray = new Array();
    attribute = document.forms[0][moto];
    var dCntArray = new Object();
    for (var i = 0; i < attribute.length; i++) {
        var examno = attribute.options[i].value.split("-")[0];
        if (ignoreExamArray.indexOf(examno) !== -1) {
            continue;
        } else {
            ignoreExamArray.push(examno);
        }
        var multiOther = document.forms[0]["MULTI_OTHER_" + examno];
        if (side == "left" && multiOther) {
            //上段に既に同じ受験番号が存在する場合スキップ
            var cnt = 0;
            for (var j = 0; j < attribute.length; j++) {
                var examno2 = attribute.options[i].value.split("-")[0];
                if (examno == examno2) {
                    cnt++;
                }
            }
            if (multiOther.value + cnt > 1) {
                errFlg = true;
                errExamStr += sep + examno;
                sep = ", ";
                continue;
            }
        }
        attribute.options[i].selected = true;
    }

    move2(side, left, right, sort, "");

    //全選択クリア
    attribute = document.forms[0][moto];
    for (var i = 0; i < attribute.length; i++) {
        attribute.options[i].selected = false;
    }

    if (errFlg) {
        alert("照合対象者に受験番号は重複して指定できません。" + "\n(受験番号「" + errExamStr + "」)");
    }
}
