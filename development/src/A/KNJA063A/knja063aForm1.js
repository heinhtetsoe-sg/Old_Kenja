function btn_submit(cmd) {   
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) return true;
    }
    //サブミット時、一旦、左リストをクリア
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    attribute4 = document.forms[0].selectdataLabel;
    attribute4.value = "";
    //右クラス変更と更新時、左リストを保持
    if (cmd == 'change_hr_class' || cmd == 'update') {
        sep = "";
        for (var i = 0; i < document.forms[0].LEFT_PART.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].LEFT_PART.options[i].value;
            attribute4.value = attribute4.value + sep + document.forms[0].LEFT_PART.options[i].text;
            sep = ",";
        }

        with(document.forms[0]){
            document.getElementById("RIGHT_NUM").innerHTML = LEFT_PART.options.length;
            document.getElementById("LEFT_NUM").innerHTML = RIGHT_PART.options.length;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//生徒移動
function moveStudent(side) {
    moveList(side,'LEFT_PART','RIGHT_PART',1);

    with(document.forms[0]){
        document.getElementById("RIGHT_NUM").innerHTML = LEFT_PART.options.length;
        document.getElementById("LEFT_NUM").innerHTML = RIGHT_PART.options.length;
    }
}

//リスト移動
function ClearList(OptionList) {
    OptionList.length = 0;
}
function moveList(side, left, right, sort) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute1;
    var attribute2;

    var sch = document.forms[0].SCH_LABEL.value;

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
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        a[tempa[y]] = temp1[y];
    }

    //assign new values to arrays
    regd_check_flg = false;
    for (var i = 0; i < attribute1.length; i++) {
        if (side == "left" || side == "sel_add_all") {
            regd_check = attribute1.options[i].text.indexOf('[無]');
            if ((side == "left" && attribute1.options[i].selected) || side == "sel_add_all") {
                if (regd_check != -1) regd_check_flg = true;
            }
        } else {
            regd_check = -1;
        }
        if (side == "right" || side == "left") {
            if (attribute1.options[i].selected && regd_check == -1) {
                y=current1++
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[tempa[y]] = temp1[y];
            } else {
                y=current2++
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {
            if (regd_check == -1) {
                y=current1++
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                a[tempa[y]] = temp1[y];
            } else {
                y=current2++
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        }
    }

    if (sort){
        //sort
        tempa = tempa.sort();
        //generating new options
        for (var i = 0; i < tempa.length; i++) {
            temp1[i] = a[tempa[i]];
        }
    }

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
    }

    //generating new options
    ClearList(attribute1);
    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0][left].length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }

    if (regd_check_flg) {
        alert('[無]の'+sch+'は更新対象外です。');
    }
}

//子画面へ
function openCheck() {
    var sch = document.forms[0].SCH_LABEL.value;
    alert('更新対象複式クラス'+sch+'一覧に'+sch+'が一人も割り振られていません。\n割り振り後は更新ボタンを押してください。');
    return;
}

function openCheck2() {
    if (!confirm('{rval MSG108}')) return true;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
function closecheck() {
    parent.window.close();
}
