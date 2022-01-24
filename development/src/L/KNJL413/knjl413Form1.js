function btn_submit(cmd) {

    if (cmd == 'add') {
        //必須チェック
        if (document.forms[0].SEND_CD_PRGID.value == '') {
            alert('{rval MSG301}'+'\n　　　（送付名）');
            return;
        }
        if (document.forms[0].SEND_DATE.value == '') {
            alert('{rval MSG301}'+'\n　　　（送付日付）');
            return;
        }

        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        if (document.forms[0].CATEGORY_SELECTED.length == 0 && document.forms[0].CATEGORY_NAME.length == 0) {
            alert('{rval MSG916}');
            return false;
        }
        if (document.forms[0].CATEGORY_SELECTED.length == 0) {
            alert('{rval MSG916}'+'\n生徒を選択してください。');
            return;
        }
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
            sep = ",";
        }

        if (confirm('送付履歴を作成しますか？')) {
            document.forms[0].REPRINT.value = '1';
        } else {
            document.forms[0].REPRINT.value = '';
        }
    }

    if (cmd == '') {

        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
            sep = ",";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function mail_send() {
    var addr = "";
    var sep = "";
    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        var seito = document.forms[0].CATEGORY_SELECTED.options[i].value;
        seito = "EMAIL_" + seito;
        addr = addr + sep + document.forms[0][seito].value;
        sep = ",";
    }
    location.href = "mailto:" + addr + "?subject=メール送信&body=メール送信テスト";
}

//印刷
function newwin(SERVLET_URL){
    //必須チェック
    if (document.forms[0].SEND_CD_PRGID.value == '') {
        alert('{rval MSG301}'+'\n　　　（送付名）');
        return;
    }
    if (document.forms[0].SEND_DATE.value == '') {
        alert('{rval MSG301}'+'\n　　　（送付日付）');
        return;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";

    if (document.forms[0].CATEGORY_SELECTED.length == 0 && document.forms[0].CATEGORY_NAME.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    if (document.forms[0].CATEGORY_SELECTED.length == 0) {
        alert('{rval MSG916}'+'\n生徒を選択してください。');
        return;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
        document.forms[0].CATEGORY_NAME.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
        document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
        attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
        sep = ",";
    }

    send = document.forms[0].SEND_CD_PRGID.value.split(":");
    document.forms[0].SEND_CD.value = send[0];
    if (document.forms[0].SEND_METHOD.value == "03") { return; }
    if (document.forms[0].SEND_METHOD.value == "03") {
        document.forms[0].PRGID.value = "KNJL413_C";
    } else if (document.forms[0].SEND_METHOD.value == "02" && document.forms[0].PRINT_GUARDNAME1.checked) {
        document.forms[0].PRGID.value = "KNJL413_B";
    } else {
        document.forms[0].PRGID.value = "KNJL413_A";
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}
function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].CATEGORY_NAME;
    ClearList(attribute,attribute);
    attribute = document.forms[0].CATEGORY_SELECTED;
    ClearList(attribute,attribute);
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
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].value)+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = String(attribute1.options[i].value)+","+y;
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
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
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].value)+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = String(attribute5.options[i].value)+","+z;
    }

    tempaa.sort();

    //generating new options
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5,attribute5);
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
