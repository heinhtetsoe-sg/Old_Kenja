function btn_submit(cmd) {
    if (cmd == "clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }
    if (cmd == 'read') {
        //選択チェック（テスト種別）
        if (document.forms[0].CATEGORY_SELECTED.length == 0) {
            alert('{rval MSG304}' + '( テスト種別 )');
            return false;
        }
        //選択したテスト種別をhiddenで保持
        for (var i = 0; i < document.forms[0].CATEGORY_NAME.length; i++) {
            document.forms[0].CATEGORY_NAME.options[i].selected = 0;
        }
        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            document.forms[0].CATEGORY_SELECTED.options[i].selected = 1;
            attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
            sep = ",";
        }
    }
    if (cmd == "update") {
        if (document.forms[0].TEST_COUNT.value == 0) {
            alert('{rval MSG304}' + '( テスト種別 )');
            return false;
        }
        
        var val = 0;
        for (var i = 0; i < document.forms[0].length; i++) {
            if (document.forms[0][i].name.match(/PERCENT/)) {
                if (document.forms[0][i].value == "") {
                    alert('{rval MSG301}' + '( 割合 )');
                    return false;
                }
                
                val = val + Number(document.forms[0][i].value);
            }
        }

        if(val > 100 ){
//            alert('割合は合計が100以下となるように入力してください。');
//            return false
        }
    }

    if (cmd == "delete") {
        if (document.forms[0].TEST_COUNT.value == 0) {
            alert('{rval MSG304}' + '( テスト種別 )');
            return false;
        }

        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }

    //読み込み中は、更新系ボタンをグレー（押せないよう）にする。
    document.forms[0].btn_update.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//テキスト
function isNumb(obj, mode){
    //数字チェック
    if (isNaN(obj.value)) {
        alert('{rval MSG907}');
        obj.value = obj.defaultValue;
        return;
    }
    //割合を変更した場合、合計点をブランクにする
    if (mode == 'per' && obj.value != obj.defaultValue) {
        document.forms[0].TOTAL.value = "";
        document.all['totalID'].innerHTML = "";
    }
}

////シュミレーション
//function keisan(testCnt) {
//    /****************/
//    /* 必須チェック */
//    /****************/
//    if (testCnt == 0) {
//        alert('{rval MSG304}' + '( テスト種別 )');
//        return false;
//    }
//    /****************/
//    /* 合計点を算出 */
//    /****************/
//    //算出式・・・(満点)*(割合)の合計 / 100
//    //例・・・{(150*25%)+(100*60%)}/(100)={(3750)+(6000)}/(100)=(9750)/(100)=97.5≒98(四捨五入)
//    var bunbo;   //分母・・・100
//    var bunsi;   //分子・・・(満点)*(割合)の合計
//    for (var i = 1; i <= testCnt; i++) {
//        perfectObject = eval("document.forms[0].PERFECT" + i);
//        percentObject = eval("document.forms[0].PERCENT" + i);
//        if (perfectObject.value == '') {
//            alert('{rval MSG305}' + '( 満点 )');
//            return false;
//        }
//        if (percentObject.value == '') {
//            alert('{rval MSG301}' + '( 割合 )');
//            return false;
//        }
//        perfectValue = parseInt(perfectObject.value);
//        percentValue = parseInt(percentObject.value);
//        if (i == 1) {
//            bunbo  = 100;
//            bunsi  = perfectValue * percentValue;
//        } else {
//            //bunbo += perfectValue;
//            bunsi += perfectValue * percentValue;
//        }
//    }
//    /******************/
//    /* 合計点をセット */
//    /******************/
//    var kekka = bunsi / bunbo;
//    var total = Math.round(kekka);
//    document.forms[0].TOTAL.value  = total;
//    document.all['totalID'].innerHTML = total;
//
//    alert('{rval MSG201}' + '確定ボタンを押下しないとデータは保存されません。');
//
//    return;
//}

/****************************************** リストtoリスト ********************************************/
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

    if (side == "left") {
        attribute1 = document.forms[0].CATEGORY_NAME;
        attribute2 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute1 = document.forms[0].CATEGORY_SELECTED;
        attribute2 = document.forms[0].CATEGORY_NAME;
    }

    for (var i = 0; i < attribute2.length; i++) {
        y=current1++;
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    for (var i = 0; i < attribute1.length; i++) {
        if ( attribute1.options[i].selected ) {
            y=current1++;
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value+","+y;
        } else {
            y=current2++;
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];
    }

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

    if (sides == "left") {
        attribute5 = document.forms[0].CATEGORY_NAME;
        attribute6 = document.forms[0].CATEGORY_SELECTED;
    } else {
        attribute5 = document.forms[0].CATEGORY_SELECTED;
        attribute6 = document.forms[0].CATEGORY_NAME;
    }

    for (var i = 0; i < attribute6.length; i++) {
        z=current5++;
        temp5[z] = attribute6.options[i].value;
        tempc[z] = attribute6.options[i].text;
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    for (var i = 0; i < attribute5.length; i++) {
        z=current5++;
        temp5[z] = attribute5.options[i].value;
        tempc[z] = attribute5.options[i].text;
        tempaa[z] = attribute5.options[i].value+","+z;
    }

    tempaa.sort();

    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];
    }

    ClearList(attribute5,attribute5);
}

function closing_window(){
        alert('{rval MSG300}');
        closeWin();
        return true;
}
