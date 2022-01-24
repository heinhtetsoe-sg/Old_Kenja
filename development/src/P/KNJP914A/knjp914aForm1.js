//サブミット
function btn_submit(cmd) {
    if (cmd == "update" && !confirm("{rval MSG101}")) {
        return false;
    }

    if (cmd == "update") {
        if (document.forms[0].INCOME_LM_CD_MOTO.value == "") {
            alert("振替元項目を指定してください。");
            return;
        } else if (document.forms[0].INCOME_LM_CD_SAKI.value == "") {
            alert("振替先項目を指定してください。");
            return;
        } else if (document.forms[0].OUTGO_LM_CD_MOTO.value == "") {
            alert("支出項目を指定してください。");
            return;
        } else if (document.forms[0].OUTGO_LMS_CD_MOTO.value == "") {
            alert("支出細目を指定してください。");
            return;
        } else if (document.forms[0].SET_MONEY.value == "" || document.forms[0].SET_MONEY.value == 0) {
            alert("振替金額を指定してください。");
            return;
        }

        if (document.forms[0].COLLECT_LM_CD.value == "") {
            alert("名称マスタ(P015)に入金項目を設定してください。");
            return;
        }

        //生徒未選択エラー
        if (document.forms[0].category_selected.length == 0) {
            alert("生徒を選択して下さい。");
            return;
        }
        student = document.forms[0].selectStudent;
        student.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].category_selected.length; i++) {
            student.value = student.value + sep + document.forms[0].category_selected.options[i].value.split("-")[1];
            sep = ",";
        }
    }

    //読み込み中は、実行ボタンはグレーアウト
    document.forms[0].btn_upd.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    return false;
}
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].category_name;
    ClearList(attribute, attribute);
    attribute = document.forms[0].category_selected;
    ClearList(attribute, attribute);
}
function move1(side) {
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
        attribute1 = document.forms[0].category_name;
        attribute2 = document.forms[0].category_selected;
    } else {
        attribute1 = document.forms[0].category_selected;
        attribute2 = document.forms[0].category_name;
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y = current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].text) + "," + y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = String(attribute1.options[i].text) + "," + y;
        } else {
            y = current2++;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort(); // 2004/01/23

    //generating new options // 2004/01/23
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(",");

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text = tempa[tmp[1]];
        //        attribute2.options[i] = new Option();
        //        attribute2.options[i].value = temp1[i];
        //        attribute2.options[i].text =  tempa[i];
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

    document.getElementById("LEFT_PART_NUM").innerHTML = document.forms[0].category_selected.options.length;
    document.getElementById("RIGHT_PART_NUM").innerHTML = document.forms[0].category_name.options.length;
}
function moves(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var z = 0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].category_name;
        attribute6 = document.forms[0].category_selected;
    } else {
        attribute5 = document.forms[0].category_selected;
        attribute6 = document.forms[0].category_name;
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z = current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].text) + "," + z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z = current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = String(attribute5.options[i].text) + "," + z;
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(",");

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text = tempc[tmp[1]];
        //        attribute6.options[i] = new Option();
        //        attribute6.options[i].value = temp5[i];
        //        attribute6.options[i].text =  tempc[i];
    }

    //generating new options
    ClearList(attribute5, attribute5);

    document.getElementById("LEFT_PART_NUM").innerHTML = document.forms[0].category_selected.options.length;
    document.getElementById("RIGHT_PART_NUM").innerHTML = document.forms[0].category_name.options.length;
}