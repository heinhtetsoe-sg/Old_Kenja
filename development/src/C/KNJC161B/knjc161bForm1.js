function btn_submit(cmd) {
        if (cmd == 'execute') {
            if (document.forms[0].month.options.length == 0) {
                return false;
            }

            if (confirm("この処理を実行すると『出欠入力制御日付』以前の出欠入力処理は更新権限がなければ処理できません。\nよろしいですか？")) {
                document.all('marq_msg').style.color = '#FF0000';
            } else {
                return;
            }
        }
        if (cmd == 'execute2'){
            cmd = 'execute';
            document.forms[0].cmd2.value = '1';
        }
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function closing_window(flg){
    if (flg == 1) {
        alert('{rval MSG300}');
    }
    if (flg == 2) {
        alert('{rval MSG305}' + '\n(学期マスタ)');
    }
    closeWin();
    return true;
}


function newwin(SERVLET_URL){

    if (document.forms[0].OUTPUT2.checked == true) {
        if (document.forms[0].CATEGORY_SELECTED.length == 0) {
            alert('{rval MSG916}');
            return;
        }
        for (i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
            document.forms[0].CATEGORY_NAME.options[i].selected = 0;
        }

        for (i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
        }
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJC";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function ClearList(OptionList) {
    OptionList.length = 0;
}

function move1(side) {
    var temp1 = [];
    var temp2 = [];
    var tempa = [];
    var tempb = [];
    var tempaa = [];
    var y=0;
    var attribute;
    var i;
    var val, tmp;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
    }


    //fill an array with old values
    for (i = 0; i < attribute2.length; i++) {
        y = temp1.length;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value + "," + y;
    }

    //assign new values to arrays
    for (i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y = temp1.length;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value + "," + y;
        } else {
            y = temp2.length;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    for (i = 0; i < temp1.length; i++) {
        val = tempaa[i];
        tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1);
    for (i = 0; i < temp2.length; i++) {
        attribute1.options[i] = new Option();
        attribute1.options[i].value = temp2[i];
        attribute1.options[i].text =  tempb[i];
    }
}

function moves(sides) {
    var i;
    var temp5 = [];
    var tempc = [];
    var tempaa = [];
    var z = 0;
    var val, tmp;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;
    }


    //fill an array with old values
    for (i = 0; i < attribute6.length; i++) {
        z = temp5.length;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value + "," + z;
    }

    //assign new values to arrays
    for (i = 0; i < attribute5.length; i++) {
        z = temp5.length;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value + "," + z;
    }

    tempaa.sort();

    //generating new options 
    for (i = 0; i < temp5.length; i++) {
        val = tempaa[i];
        tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5);
}
