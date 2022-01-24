function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
        //削除不可チェック
        if (document.forms[0].JUDGE_RESULT.value && document.forms[0].JUDGE_RESULT.value != "1") {
            alert('{rval MSG919}\n　　（審査結果）');
            document.forms[0].YEAR.focus();
            return;
        }

        //必須入力チェック
        if (document.forms[0].YEAR.value == '') {
            alert('{rval MSG304}\n　　（対象年度）');
            document.forms[0].YEAR.focus();
            return;
        }
        if (document.forms[0].REGISTER_DATE_CMB.value == '') {
            alert('{rval MSG304}\n　　（指定登録日）');
            document.forms[0].REGISTER_DATE.focus();
            return;
        }
    }

    if (cmd == 'update') {
        //必須入力チェック
        if (document.forms[0].REGISTER_DATE.value == '') {
            alert('{rval MSG304}\n　　（登録日）');
            document.forms[0].REGISTER_DATE.focus();
            return;
        }
        if (document.forms[0].BOOKDIV1_GK.value == '') {
            alert('{rval MSG304}\n　　（教科書）');
            document.forms[0].BOOKDIV1_GK.focus();
            return;
        }
        if (document.forms[0].BOOKDIV2_GK.value == '') {
            alert('{rval MSG304}\n　　（学習書）');
            document.forms[0].BOOKDIV2_GK.focus();
            return;
        }
        if (document.forms[0].TOTAL_COUNT.value == '') {
            alert('{rval MSG304}\n　　（冊数）');
            document.forms[0].TOTAL_COUNT.focus();
            return;
        }
        if (document.forms[0].PROVIDE_REASON.value == '') {
            alert('{rval MSG304}\n　　（支給対象事由）');
            document.forms[0].PROVIDE_REASON.focus();
            return;
        }

        //無償給与対象教科書
        attribute3 = document.forms[0].selectdata;
        attribute3.value = "";
        sep = "";
        if (document.forms[0].CATEGORY_SELECTED.length == 0 && document.forms[0].CATEGORY_SELECTED.length == 0) {
            alert('{rval MSG304}\n（無償給与対象）');
            return false;
        }
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
            sep = ",";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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
        tempaa[y] = attribute2.options[i].value+","+y;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        //登録済み教科書は無償給与対象に移動不可
        if (side == "left") {
            var textVal = attribute1.options[i].value.split('-');
            div = textVal[3];
        } else {
            div = '0';
        }

        if ( attribute1.options[i].selected && div == '0' ) {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            tempaa[y] = attribute1.options[i].value+","+y;
        } else {
            y=current2++
            temp2[y] = attribute1.options[i].value;
            tempb[y] = attribute1.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    price1 = price2 = num = 0;
    for (var i = 0; i < temp1.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[tmp[1]];
        attribute2.options[i].text =  tempa[tmp[1]];

        //金額計算
        var textVal = attribute2.options[i].value.split('-');
        if (textVal[1] == "1") price1 += parseInt(textVal[2]);
        if (textVal[1] == "2") price2 += parseInt(textVal[2]);
        num++;

        //登録済みは背景色を変更する
        if (textVal[3] == '1') {
            attribute2.options[i].style.backgroundColor = "red";
            attribute2.options[i].style.color = "white";
        } else if(textVal[3] == '2') {
            attribute2.options[i].style.backgroundColor = "#ccffff";
        }
    }

    //金額をセット
    if (side == "left") {
        document.getElementById('L_DIV1_PRICE').innerHTML = number_format(parseInt(price1));
        document.getElementById('L_DIV2_PRICE').innerHTML = number_format(parseInt(price2));
        document.getElementById('L_TOTAL_PRICE').innerHTML = number_format(parseInt(price1) + parseInt(price2));
        document.getElementById('L_TOTAL_NUM').innerHTML = number_format(parseInt(num));

        document.forms[0].BOOKDIV1_GK.value = number_format(parseInt(price1));
        document.forms[0].BOOKDIV2_GK.value = number_format(parseInt(price2));
        document.forms[0].TOTAL_GK.value = number_format(parseInt(price1) + parseInt(price2));
        document.forms[0].TOTAL_COUNT.value = number_format(parseInt(num));
    } else {
        document.getElementById('R_DIV1_PRICE').innerHTML = number_format(parseInt(price1));
        document.getElementById('R_DIV2_PRICE').innerHTML = number_format(parseInt(price2));
        document.getElementById('R_TOTAL_PRICE').innerHTML = number_format(parseInt(price1) + parseInt(price2));
        document.getElementById('R_TOTAL_NUM').innerHTML = number_format(parseInt(num));
    }

    //generating new options
    ClearList(attribute1,attribute1);
    price1 = price2 = num = 0;
    if (temp2.length > 0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];

            //金額計算
            var textVal = attribute1.options[i].value.split('-');
            if (textVal[1] == "1") price1 += parseInt(textVal[2]);
            if (textVal[1] == "2") price2 += parseInt(textVal[2]);
            num++;

            //登録済みは背景色を変更する
            if (textVal[3] == '1') {
                attribute1.options[i].style.backgroundColor = "red";
                attribute1.options[i].style.color = "white";
            } else if (textVal[3] == '2') {
                attribute1.options[i].style.backgroundColor = "#ccffff";
            }
        }
    }

    //金額をセット
    if (side == "left") {
        document.getElementById('R_DIV1_PRICE').innerHTML = number_format(parseInt(price1));
        document.getElementById('R_DIV2_PRICE').innerHTML = number_format(parseInt(price2));
        document.getElementById('R_TOTAL_PRICE').innerHTML = number_format(parseInt(price1) + parseInt(price2));
        document.getElementById('R_TOTAL_NUM').innerHTML = number_format(parseInt(num));
    } else {
        document.getElementById('L_DIV1_PRICE').innerHTML = number_format(parseInt(price1));
        document.getElementById('L_DIV2_PRICE').innerHTML = number_format(parseInt(price2));
        document.getElementById('L_TOTAL_PRICE').innerHTML = number_format(parseInt(price1) + parseInt(price2));
        document.getElementById('L_TOTAL_NUM').innerHTML = number_format(parseInt(num));

        document.forms[0].BOOKDIV1_GK.value = number_format(parseInt(price1));
        document.forms[0].BOOKDIV2_GK.value = number_format(parseInt(price2));
        document.forms[0].TOTAL_GK.value = number_format(parseInt(price1) + parseInt(price2));
        document.forms[0].TOTAL_COUNT.value = number_format(parseInt(num));
    }
}
function moves(sides) {
    var temp5 = new Array();
    var temp6 = new Array();
    var tempc = new Array();
    var tempd = new Array();
    var tempaa = new Array();
    var current5 = 0;
    var current6 = 0;
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
        tempaa[z] = attribute6.options[i].value+","+z;
    }

    //assign new values to arrays
    for (var i = 0; i < attribute5.length; i++) {
        //登録済み教科書は無償給与対象に移動不可
        if (sides == "left") {
            var textVal = attribute5.options[i].value.split('-');
            div = textVal[3];
        } else {
            div = '0';
        }

        if (div == '0') {
            z=current5++
            temp5[z] = attribute5.options[i].value;
            tempc[z] = attribute5.options[i].text;
            tempaa[z] = attribute5.options[i].value+","+z;
        } else {
            z=current6++
            temp6[z] = attribute5.options[i].value;
            tempd[z] = attribute5.options[i].text;
        }
    }

    tempaa.sort();

    //generating new options
    price1 = price2 = num = 0;
    for (var i = 0; i < temp5.length; i++) {
        var val = tempaa[i];
        var tmp = val.split(',');

        attribute6.options[i] = new Option();
        attribute6.options[i].value = temp5[tmp[1]];
        attribute6.options[i].text =  tempc[tmp[1]];

        //金額計算
        var textVal = attribute6.options[i].value.split('-');
        if (textVal[1] == "1") price1 += parseInt(textVal[2]);
        if (textVal[1] == "2") price2 += parseInt(textVal[2]);
        num++;

        //登録済みは背景色を変更する
        if (textVal[3] == '1') {
            attribute6.options[i].style.backgroundColor = "red";
            attribute6.options[i].style.color = "white";
        } else if (textVal[3] == '2') {
            attribute6.options[i].style.backgroundColor = "#ccffff";
        }
    }

    //金額をセット
    if (sides == "left") {
        document.getElementById('L_DIV1_PRICE').innerHTML = number_format(parseInt(price1));
        document.getElementById('L_DIV2_PRICE').innerHTML = number_format(parseInt(price2));
        document.getElementById('L_TOTAL_PRICE').innerHTML = number_format(parseInt(price1) + parseInt(price2));
        document.getElementById('L_TOTAL_NUM').innerHTML = number_format(parseInt(num));

        document.forms[0].BOOKDIV1_GK.value = number_format(parseInt(price1));
        document.forms[0].BOOKDIV2_GK.value = number_format(parseInt(price2));
        document.forms[0].TOTAL_GK.value = number_format(parseInt(price1) + parseInt(price2));
        document.forms[0].TOTAL_COUNT.value = number_format(parseInt(num));
    } else {
        document.getElementById('R_DIV1_PRICE').innerHTML = number_format(parseInt(price1));
        document.getElementById('R_DIV2_PRICE').innerHTML = number_format(parseInt(price2));
        document.getElementById('R_TOTAL_PRICE').innerHTML = number_format(parseInt(price1) + parseInt(price2));
        document.getElementById('R_TOTAL_NUM').innerHTML = number_format(parseInt(num));
    }

    //generating new options
    ClearList(attribute5,attribute5);
    price1 = price2 = num = 0;
    if (temp6.length > 0) {
        for (var i = 0; i < temp6.length; i++) {
            attribute5.options[i] = new Option();
            attribute5.options[i].value = temp6[i];
            attribute5.options[i].text =  tempd[i];

            //金額計算
            var textVal = attribute5.options[i].value.split('-');
            if (textVal[1] == "1") price1 += parseInt(textVal[2]);
            if (textVal[1] == "2") price2 += parseInt(textVal[2]);
            num++;

            //登録済みは背景色を変更する
            if (textVal[3] == '1') {
                attribute5.options[i].style.backgroundColor = "red";
                attribute5.options[i].style.color = "white";
            } else if (textVal[3] == '2') {
                attribute5.options[i].style.backgroundColor = "#ccffff";
            }
        }
    }

    //金額をセット
    if (sides == "left") {
        document.getElementById('R_DIV1_PRICE').innerHTML = number_format(parseInt(price1));
        document.getElementById('R_DIV2_PRICE').innerHTML = number_format(parseInt(price2));
        document.getElementById('R_TOTAL_PRICE').innerHTML = number_format(parseInt(price1) + parseInt(price2));
        document.getElementById('R_TOTAL_NUM').innerHTML = number_format(parseInt(num));
    } else {
        document.getElementById('L_DIV1_PRICE').innerHTML = number_format(parseInt(price1));
        document.getElementById('L_DIV2_PRICE').innerHTML = number_format(parseInt(price2));
        document.getElementById('L_TOTAL_PRICE').innerHTML = number_format(parseInt(price1) + parseInt(price2));
        document.getElementById('L_TOTAL_NUM').innerHTML = number_format(parseInt(num));

        document.forms[0].BOOKDIV1_GK.value = number_format(parseInt(price1));
        document.forms[0].BOOKDIV2_GK.value = number_format(parseInt(price2));
        document.forms[0].TOTAL_GK.value = number_format(parseInt(price1) + parseInt(price2));
        document.forms[0].TOTAL_COUNT.value = number_format(parseInt(num));
    }
}

//給与費対象額計算
function CalculatePrice(obj) {

    //数値チェック
    obj.value = toNumber(obj.value);

    gk1 = document.forms[0].BOOKDIV1_GK.value.replace(",", "");
    gk2 = document.forms[0].BOOKDIV2_GK.value.replace(",", "");

    total = parseInt("0");
    if (gk1) total += parseInt(gk1);
    if (gk2) total += parseInt(gk2);

    document.forms[0].TOTAL_GK.value = number_format(parseInt(total));
    document.forms[0].BOOKDIV1_GK.value = number_format(parseInt(gk1));
    document.forms[0].BOOKDIV2_GK.value = number_format(parseInt(gk2));
}
