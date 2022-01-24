function btn_submit(cmd) {
    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }

    //除外された人をいったん項目として戻す
     $('select[name=category_name] > span > option').unwrap('<span>');

    if (document.forms[0].SCH_CNT) {
        student = document.forms[0].selectStudent;
        student.value = "";
        studentLabel = document.forms[0].selectStudentLabel;
        studentLabel.value = "";
        studentRight = document.forms[0].selectStudentRight;
        studentRight.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].category_selected.length; i++) {
            student.value = student.value + sep + document.forms[0].category_selected.options[i].value;
            studentLabel.value = studentLabel.value + sep + document.forms[0].category_selected.options[i].text;
            sep = ",";
        }
        sep = "";
        for (var i = 0; i < document.forms[0].category_name.length; i++) {
            studentRight.value = studentRight.value + sep + document.forms[0].category_name.options[i].value;
            sep = ",";
        }
    }

    if (cmd === 'update') {
        //会計細目未選択
        if (document.forms[0].OUTGO_L_M_S_CD.value == "") {
            alert('{rval MSG301}' + '(会計細目)');
            return false;
        }
        //支出単価がブランクで、生徒選択していたらエラー
        if (document.forms[0].COMMODITY_PRICE.value == "") {
            alert('{rval MSG301}' + '(支出単価)');
            return;
        }
        //生徒未選択エラー
        if (document.forms[0].category_selected.length == 0) {
            alert('生徒を選択して下さい。');
            return;
        }
    } else if (cmd == 'not_warihuri_update'){
        if (document.forms[0].OUTGO_L_M_S_CD.value == "") {
            alert('{rval MSG301}' + '(会計細目)');
            return false;
        }
        if (document.forms[0].COMMODITY_PRICE.value == "") {
            alert('{rval MSG301}' + '(支出単価)');
            return;
        }
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
//コンボにテキストの追加
function add(lmsCd, cmbCnt) {
    var temp1    = new Array();
    var tempa    = new Array();
    var cmbLen   = document.forms[0].OUTGO_L_M_S_CD.length;
    var textVal  = document.forms[0].OUTGO_NAME.value
    var sNameArr = document.forms[0].LEVY_S_NAMES.value.split(':');

    if (textVal == "") {
        alert("{rval MSG901}\n文字を入力してください。");
        return false;
    }

    if (encodeURIComponent(textVal).replace(/%../g,"x").length > 90) {
        alert("{rval MSG901}\n90バイトまで。");
    }

    for (var i = 0; i < sNameArr.length; i++) {
        if (textVal == sNameArr[i]) {
            alert("同じ名称はセットできません。");
            return false;
        }
    }
    document.forms[0].OUTGO_L_M_S_CD.options[cmbCnt]          = new Option();
    document.forms[0].OUTGO_L_M_S_CD.options[cmbCnt].value    = lmsCd + ":" + textVal;
    document.forms[0].OUTGO_L_M_S_CD.options[cmbCnt].text     = lmsCd + ":" + textVal;
    document.forms[0].OUTGO_L_M_S_CD.options[cmbCnt].selected = true;
}

function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}
    
function AllClearList(OptionList, TitleName) {
        attribute = document.forms[0].category_name;
        ClearList(attribute,attribute);
        attribute = document.forms[0].category_selected;
        ClearList(attribute,attribute);
}
function move1(side) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var tempaa = new Array();    // 2004/01/23
    var current1 = 0;
    var current2 = 0;
    var y=0;
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
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = String(attribute2.options[i].text)+","+y; // 2004/01/23
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text; 
            tempaa[y] = String(attribute1.options[i].text)+","+y; // 2004/01/23
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value; 
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();    // 2004/01/23

    //generating new options // 2004/01/23
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
//        attribute2.options[i] = new Option();
//        attribute2.options[i].value = temp1[i];
//        attribute2.options[i].text =  tempa[i];
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

    //右リストの異動者を非表示にする
    changeTransferDisp();

}
function moves(sides) {
    var temp5 = new Array();
    var tempc = new Array();
    var tempaa = new Array();    // 2004/01/23
    var current5 = 0;
    var z=0;
    
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
        tempaa[z] = String(attribute6.options[i].text)+","+z; // 2004/01/23
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        z=current5++
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text; 
        tempaa[z] = String(attribute5.options[i].text)+","+z; // 2004/01/23
    }

    tempaa.sort();    // 2004/01/23

    //generating new options // 2004/01/23
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
//        attribute6.options[i] = new Option();
//        attribute6.options[i].value = temp5[i];
//        attribute6.options[i].text =  tempc[i];
    }

    //generating new options
    ClearList(attribute5,attribute5);

    document.getElementById("LEFT_PART_NUM").innerHTML = document.forms[0].category_selected.options.length;
    document.getElementById("RIGHT_PART_NUM").innerHTML = document.forms[0].category_name.options.length;

    //右リストの異動者を非表示にする
    changeTransferDisp();

}

function changeTransferDisp() {
    
    var temp1  = new Array();
    var tempa  = new Array();
    var tempaa = new Array();

    var rmvTransferFlg = document.forms[0].CHK_TRANSFER.checked;
    var transferNum = 0;

    if (rmvTransferFlg == "1") { //異動者除外処理
        var transferList = document.forms[0].transferList.value.split(",");
        transferNum = transferList.length;

        for (var i = 0; i < transferList.length; i++) {
            $('select[name=category_name] > option[value='+transferList[i]+']').wrap('<span>');
        }
    } else {                     //除外された人を再度表示する
        $('select[name=category_name] > span > option').unwrap('<span>');
    }

    //並び替え
    var y = 0;
    var attribute1 = document.forms[0].category_name;
    for (var i = 0; i < attribute1.length; i++) {
        temp1[i]  = attribute1.options[i].value;
        tempa[i]  = attribute1.options[i].text;
        tempaa[i] = String(attribute1.options[i].text)+","+i;
    }
    tempaa.sort();
    attribute1.length = 0;
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');
        attribute1.options[i] = new Option();
        attribute1.options[i].value = temp1[tmp[1]];
        attribute1.options[i].text =  tempa[tmp[1]];
    }

	document.getElementById("LEFT_PART_NUM").innerHTML = document.forms[0].category_selected.options.length;
    document.getElementById("RIGHT_PART_NUM").innerHTML = document.forms[0].category_name.options.length;
}

