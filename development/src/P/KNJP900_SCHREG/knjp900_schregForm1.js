function btn_submit(cmd) {
    if (cmd === 'update') {
        student = document.forms[0].selectStudent;
        student.value = "";
        studentLabel = document.forms[0].selectStudentLabel;
        studentLabel.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].category_selected.length; i++) {
            student.value = student.value + sep + document.forms[0].category_selected.options[i].value;
            studentLabel.value = studentLabel.value + sep + document.forms[0].category_selected.options[i].text;
            sep = ",";
        }
        //単価未入力エラー
        if (document.forms[0].COMMODITY_PRICE.value == 0 || document.forms[0].COMMODITY_PRICE.value == "") {
            alert('{rval MSG301}' + '(単価)');
            return;
        }
        //生徒が未選択なら、エラー
        if (document.forms[0].category_selected.length == 0) {
            alert('生徒を選択して下さい。');
            return;
        }
    } else if (cmd == 'not_warihuri_update'){
        //単価未入力エラー
        if (document.forms[0].COMMODITY_PRICE.value == 0 || document.forms[0].COMMODITY_PRICE.value == "") {
            alert('{rval MSG301}' + '(単価)');
            return;
        }
        //数量未入力エラー
        if (document.forms[0].COMMODITY_CNT.value == 0 || document.forms[0].COMMODITY_PRICE.value == "") {
            alert('{rval MSG301}' + '(数量)');
            return false;
        }
        cmd = 'update';
    } else if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    if (document.forms[0].category_selected.length == 0)
    {
        alert('{rval MSG916}');
        return;
    }

    for (var i = 0; i < document.forms[0].category_name.length; i++)
    {  
        document.forms[0].category_name.options[i].selected = 0;
    }

    for (var i = 0; i < document.forms[0].category_selected.length; i++)
    {  
        document.forms[0].category_selected.options[i].selected = 1;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJP";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
//単価の値が変更されたとき
function changeMoney(obj) {
    document.forms[0].chgPflg.value = '1';
    obj.value = toInteger(obj.value);
    btn_submit('edit');
}

function ClearList(OptionList, TitleName) 
{
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) 
{
        attribute = document.forms[0].category_name;
        ClearList(attribute,attribute);
        attribute = document.forms[0].category_selected;
        ClearList(attribute,attribute);
}
function move1(side)
{   
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute;

    if (document.forms[0].COMMODITY_PRICE.value == "") {
        alert('単価未設定です。');
        return false;
    }

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
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].text)+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        var sStrLen = attribute1.options[i].text.indexOf('残額:');
        var eStrLen = attribute1.options[i].text.indexOf(' )');
        var checkVal = attribute1.options[i].text.substring(sStrLen + 3, eStrLen);
        if ((side == "right" && attribute1.options[i].selected) ||
            (side == "left" && attribute1.options[i].selected && attribute1.options[i].text.indexOf('●') == -1) ||
            (attribute1.options[i].selected && (parseInt(document.forms[0].COMMODITY_PRICE.value) <= parseInt(checkVal)))
        ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = String(attribute1.options[i].text)+","+y;
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

    document.getElementById("LEFT_PART_NUM").innerHTML = document.forms[0].category_selected.options.length;
    document.getElementById("RIGHT_PART_NUM").innerHTML = document.forms[0].category_name.options.length;
}
function moves(sides) {
    document.forms[0].movesFlg.value = sides;
    if (document.forms[0].chgPflg.value == '1') {
        btn_submit('edit');
        return;
    }

    var temp5 = new Array();
    var temp6 = new Array();
    var tempc = new Array();
    var tempd = new Array();
    var tempaa = new Array();
    var tempbb = new Array();
    var current5 = 0;
    var current6 = 0;
    var y=0;
    var z=0;

    if (document.forms[0].COMMODITY_PRICE.value == "") {
        alert('単価未設定です。');
        return false;
    }

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
        z=current5++
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = String(attribute6.options[i].text)+","+z;
    }

    //assign new values to arrays
    var temp1Flg = false;
    for (var i = 0; i < attribute5.length; i++) {
        if (sides == "left") {
            var sStrLen = attribute5.options[i].text.indexOf('残額:');
            var eStrLen = attribute5.options[i].text.indexOf(' )');
            var checkVal = attribute5.options[i].text.substring(sStrLen + 3, eStrLen);
            if ((parseInt(document.forms[0].COMMODITY_PRICE.value) > parseInt(checkVal)) &&
                (attribute5.options[i].text.indexOf('●') == 0)
            ) {
                y = current6++;
                temp6[y] = attribute5.options[i].value;
                tempd[y] = attribute5.options[i].text; 
                tempbb[y] = String(attribute5.options[i].text)+","+y;
                temp1Flg = true;
                continue;
            }
        }
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = String(attribute5.options[i].text)+","+z;
    }

    tempaa.sort();
    tempbb.sort();

    //generating new options
    ClearList(attribute5,attribute5);

    //generating new options
    for (var i = 0; i < temp6.length; i++) {
        var val = tempbb[i];
        var tmp = val.split(',');

        attribute5.options[i] = new Option();
        attribute5.options[i].value = temp6[tmp[1]];
        attribute5.options[i].text =  tempd[tmp[1]];
    }

    //generating new options
    ClearList(attribute6,attribute6);

    //generating new options
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }
    document.forms[0].movesFlg.value = '';

    document.getElementById("LEFT_PART_NUM").innerHTML = document.forms[0].category_selected.options.length;
    document.getElementById("RIGHT_PART_NUM").innerHTML = document.forms[0].category_name.options.length;
}

