function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == 'update'){

        //日付チェック（学期範囲）
        var day   = document.forms[0].RIGHT_TEST_DATE.value.split('/');        //試験日付
        var sdate = document.forms[0].SEME_SDATE.value.split('-');  //学期開始日付
        var edate = document.forms[0].SEME_EDATE.value.split('-');  //学期終了日付
        if ((new Date(eval(sdate[0]),eval(sdate[1])-1,eval(sdate[2])) > new Date(eval(day[0]),eval(day[1])-1,eval(day[2])))
           || ((new Date(eval(day[0]),eval(day[1])-1,eval(day[2])) > new Date(eval(edate[0]),eval(edate[1])-1,eval(edate[2])))))
        {
            alert("日付が年度の範囲外です");
            return;
        }

        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        var arrayStr = new Array();
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            arrayStr = document.forms[0].CATEGORY_SELECTED.options[i].value.split(':');
            attribute3.value = attribute3.value + sep + arrayStr[0];
            sep = ",";
        }
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
function goEnter(obj){
    if (window.event.keyCode==13) {
        obj.blur();
        return false;
    }
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
    var valArr  = new Array();
    var valArr2 = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;

    //リストのデータをセット
    if (side == "left") {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;  
    }
    
    //移動先の情報を配列にセット
    for (var i = 0; i < attribute2.length; i++) {
        y = current1++
        temp1[y]  = attribute2.options[i].value;
        tempa[y]  = attribute2.options[i].text;
        valArr    = attribute2.options[i].value.split(':');
        valArr2   = valArr[1].split('-');//valArr2[0]:出席番号, [1]:受験結果コード
        tempaa[y] = valArr2[1]+valArr2[0]+","+y;
    }

    //移動元の情報を配列にセット
    for (var i = 0; i < attribute1.length; i++) {
        if (attribute1.options[i].selected) {
            y = current1++
            temp1[y]  = attribute1.options[i].value;
            tempa[y]  = attribute1.options[i].text; 
            valArr    = attribute1.options[i].value.split(':');
            valArr2   = valArr[1].split('-');//valArr2[0]:出席番号, [1]:受験結果コード
            tempaa[y] = valArr2[1]+valArr2[0]+","+y;
        } else {
            y = current2++
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
    var valArr  = new Array();
    var valArr2 = new Array();
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
        z = current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        valArr    = attribute6.options[i].value.split(':');
        valArr2   = valArr[1].split('-');//valArr2[0]:出席番号, [1]:受験結果コード
        tempaa[z] = valArr2[1]+valArr2[0]+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z = current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        valArr    = attribute5.options[i].value.split(':');
        valArr2   = valArr[1].split('-');//valArr2[0]:出席番号, [1]:受験結果コード
        tempaa[z] = valArr2[1]+valArr2[0]+","+z;
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
