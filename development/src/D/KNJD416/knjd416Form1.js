function btn_submit(cmd, type, dataCnt) {
    selectdata = document.forms[0].selectdata;
    selectdata.value = "";
    sep = "";

    for (var i = 0; i < document.forms[0].LEFT_SELECT.length; i++) {
        var val = document.forms[0].LEFT_SELECT.options[i].value;
        selectdata.value = selectdata.value + sep + val;
        sep = ",";
    }

    setType = type;

    if (cmd == 'gakunen'){
        setType = '1';  //1:学年別
    }else if (cmd == 'kojin'){
        setType = '2';  //2:個人別
    }else if (cmd == 'back'){
        setType = '0';  //0:基本
    }

    if (type == '0' || type == ''){
        //基本の場合
        if (cmd == 'gakunen' || cmd == 'kojin') {
            //学年別ボタン または 個人別ボタン押下時
            if (dataCnt == '0') {
                alert('基本設定が未登録です。');
                return true;
            }
        }
    }

    //更新
    if (cmd == 'update') {
        //必須チェック
        //if (document.forms[0].LEFT_SELECT.length==0) {
        //    alert('{rval MSG304}');
        //    return true;
        //}
    }

    if (cmd == "copy") {
        if (document.forms[0].PRE_YEAR_CNT.value <= 0) {
            alert('前年度のデータが存在しません。');
            return false;
        }
        if (document.forms[0].THIS_YEAR_CNT.value > 0) {
            if (!confirm('今年度のデータは破棄されます。コピーしてもよろしいですか？')) {
                return false;
            }
        } else {
            if (!confirm('{rval MSG101}')) {
                return false;
            }
        }
    }

    document.forms[0].type.value = setType;
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

/**********************/
/**  リストtoリスト  **/
/**********************/
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

function AllClearList(OptionList, TitleName) {
    attribute = document.forms[0].RIGHT_SELECT;
    ClearList(attribute,attribute);
    attribute = document.forms[0].LEFT_SELECT;
    ClearList(attribute,attribute);
}

function move1(side) {
    var temp1   = new Array();
    var temp2   = new Array();
    var tempa   = new Array();
    var tempb   = new Array();
    var tempaa  = new Array();
    var current1 = 0;
    var current2 = 0;
    var y = 0;
    var attribute;

    //assign what select attribute treat as attribute1 and attribute2
    if (side == "left") {
        attribute1 = document.forms[0].RIGHT_SELECT;
        attribute2 = document.forms[0].LEFT_SELECT;
    } else {
        attribute1 = document.forms[0].LEFT_SELECT;
        attribute2 = document.forms[0].RIGHT_SELECT;
    }


    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y]    = attribute2.options[i].value;
        tempa[y]    = attribute2.options[i].text;
        tempaa[y]   = attribute2.options[i].value+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y]    = attribute1.options[i].value;
            tempa[y]    = attribute1.options[i].text;
            tempaa[y]   = attribute1.options[i].value+","+y;
        } else {
            y=current2++
            temp2[y]    = attribute1.options[i].value;
            tempb[y]    = attribute1.options[i].text;
        }
    }

    //tempaa.sort();

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i]       = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text  =  tempa[tmp[1]];
    }

    //generating new options
    ClearList(attribute1,attribute1);
    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i]       = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text  =  tempb[i];
        }
    }
}

function moves(sides) {
    var temp5   = new Array();
    var tempc   = new Array();
    var tempaa  = new Array();
    var current5 = 0;
    var z = 0;

    //assign what select attribute treat as attribute5 and attribute6
    if (sides == "left") {
        attribute5 = document.forms[0].RIGHT_SELECT;
        attribute6 = document.forms[0].LEFT_SELECT;
    } else {
        attribute5 = document.forms[0].LEFT_SELECT;
        attribute6 = document.forms[0].RIGHT_SELECT;
    }

    //fill an array with old values
    for (var i = 0; i < attribute6.length; i++) {
        z=current5++
        temp5[z]    = attribute6.options[i].value;
        tempc[z]    = attribute6.options[i].text;
        tempaa[z]   = attribute6.options[i].value+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z]    = attribute5.options[i].value;
        tempc[z]    = attribute5.options[i].text;
        tempaa[z]   = attribute5.options[i].value+","+z;
    }

    //tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i]       = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text  =  tempc[tmp[1]];
    }

    //generating new options
    ClearList(attribute5,attribute5);
}ss