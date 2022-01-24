function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].EXAMNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return true;
        }
        if (vflg == true) {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }
    if (cmd == 'disp_clear') {
        for (i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'select-one' || document.forms[0].elements[i].type == 'text' || document.forms[0].elements[i].name == 'DE003REMARK5') {
                if (document.forms[0].elements[i].type == 'select-one') {
                    document.forms[0].elements[i].value = document.forms[0].elements[i].options[0].value;
                } else {
                    document.forms[0].elements[i].value = "";
                }
            }
        }
        outputLAYER('NAME', '');
        outputLAYER('SHDIV', '');
        outputLAYER('NAME_KANA', '');
        outputLAYER('JUDGE_KIND', '');
        outputLAYER('SEX', '');
        outputLAYER('BIRTHDAY', '');
        outputLAYER('DESIREDIV', '');
        outputLAYER('FS_CD', '');
        outputLAYER('FS_GRDYEAR', '');
        outputLAYER('CONF1_RPT10', '0');
        outputLAYER('CONF1_RPT11', '0');
        outputLAYER('CONF2_RPT10', '0');
        outputLAYER('CONF2_RPT11', '0');
        outputLAYER('TOTAL5', '0');
        outputLAYER('TOTAL_ALL', '0');
        var kyoukasu = eval(document.forms[0].kyouka_count.value);
        for (i=1; i <= kyoukasu; i++) {
            outputLAYER('SUBTOTAL0'+ i, '0');
        }
        outputLAYER('SUBTOTAL55', '0');
        outputLAYER('SUBTOTAL99', '0');
        outputLAYER('ABSENCE_TOTAL', '0');

        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}

//ボタンを押し不可にする
function btn_disabled() {
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
    document.forms[0].btn_del.disabled = true;
}
//計算(内申)
function Culc() {
    var setSubNum = [1, 2, 3, 4, 9];//科目番号
    for(i=1; i<=9; i++){
        eval("var setSubTotal"+ i +" = 0;");
    }
    for(i=1; i<=3; i++){
        eval("var set"+ i +"Sum5 = 0;");
    }
    var setSumTotal1 = 0;
    var setSumTotal2 = 0;
    var setSumTotal3 = 0;
    var setTotalSum5 = 0;
    var setTotalSumALL = 0;
    var kyoukasu5 = 5;
    var kyoukasu = eval(document.forms[0].kyouka_count.value);

    for (i=1; i <= kyoukasu; i++) {
        if (document.getElementById("CONF1_RPT0" + i).value == "") document.getElementById("CONF1_RPT0" + i).value = 0;
        if (document.getElementById("CONF2_RPT0" + i).value == "") document.getElementById("CONF2_RPT0" + i).value = 0;
        if (document.getElementById("CONFIDENTIAL_RPT0" + i).value == "") document.getElementById("CONFIDENTIAL_RPT0" + i).value = 0;
    }

    //５教科合計(各学年)
        for (var i=0; i < 5; i++) {
            set1Sum5 = set1Sum5 + eval(document.getElementById("CONF1_RPT0"+setSubNum[i]).value);
            set2Sum5 = set2Sum5 + eval(document.getElementById("CONF2_RPT0"+setSubNum[i]).value);
            set3Sum5 = set3Sum5 + eval(document.getElementById("CONFIDENTIAL_RPT0"+setSubNum[i]).value);
        }
        document.getElementById("CONF1_RPT10").innerHTML = set1Sum5;//５教科合計(１年)
        document.forms[0].CONF1_RPT10.value              = set1Sum5;//５教科合計(１年)hidden用
        document.getElementById("CONF2_RPT10").innerHTML = set2Sum5;//５教科合計(２年)
        document.forms[0].CONF2_RPT10.value              = set2Sum5;//５教科合計(２年)hidden用
        document.getElementById("TOTAL5").innerHTML      = set3Sum5;//５教科合計(３年)
        document.forms[0].TOTAL5.value                   = set3Sum5;//５教科合計(３年)hidden用

    //全体合計(各学年)
    for (i=1; i <= kyoukasu; i++) {
        setSumTotal1 = setSumTotal1 + eval(document.getElementById("CONF1_RPT0" + i).value);
        setSumTotal2 = setSumTotal2 + eval(document.getElementById("CONF2_RPT0" + i).value);
        setSumTotal3 = setSumTotal3 + eval(document.getElementById("CONFIDENTIAL_RPT0" + i).value);
    }
    document.getElementById("CONF1_RPT11").innerHTML = setSumTotal1;//全体合計(１年)
    document.forms[0].CONF1_RPT11.value              = setSumTotal1;//全体合計(１年)hidden用
    document.getElementById("CONF2_RPT11").innerHTML = setSumTotal2;//全体合計(２年)
    document.forms[0].CONF2_RPT11.value              = setSumTotal2;//全体合計(２年)hidden用
    document.getElementById("TOTAL_ALL").innerHTML   = setSumTotal3;//全体合計(３年)
    document.forms[0].TOTAL_ALL.value                = setSumTotal3;//全体合計(３年)hidden用

    //各科目３年間合計
    for (var i=1; i <= kyoukasu; i++) {
        //１，２年
        for (var grade=1; grade <= 2; grade++) {
            eval("setSubTotal"+i+" = setSubTotal"+i+" + "+document.getElementById("CONF"+grade+"_RPT0"+i).value+"");
        }
        //３年
        eval("setSubTotal"+i+" = setSubTotal"+i+" + "+document.getElementById("CONFIDENTIAL_RPT0"+i).value+"");
        document.getElementById("SUBTOTAL0"+i).innerHTML = eval("setSubTotal"+i+"");
    }

    //５教科合計(３年間総計)
    if (document.getElementById("CONF1_RPT10").innerHTML != "") setTotalSum5 = setTotalSum5 + eval(document.getElementById("CONF1_RPT10").innerHTML);
    if (document.getElementById("CONF2_RPT10").innerHTML != "") setTotalSum5 = setTotalSum5 + eval(document.getElementById("CONF2_RPT10").innerHTML);
    if (document.getElementById("TOTAL5").innerHTML != "")      setTotalSum5 = setTotalSum5 + eval(document.getElementById("TOTAL5").innerHTML);
    document.getElementById("SUBTOTAL55").innerHTML = setTotalSum5;

    //全体合計(３年間総計)
    if (document.getElementById("CONF1_RPT11").innerHTML != "") setTotalSumALL = setTotalSumALL + eval(document.getElementById("CONF1_RPT11").innerHTML);
    if (document.getElementById("CONF2_RPT11").innerHTML != "") setTotalSumALL = setTotalSumALL + eval(document.getElementById("CONF2_RPT11").innerHTML);
    if (document.getElementById("TOTAL_ALL").innerHTML != "")   setTotalSumALL = setTotalSumALL + eval(document.getElementById("TOTAL_ALL").innerHTML);
    document.getElementById("SUBTOTAL99").innerHTML = setTotalSumALL;

    return;
}
//計算(出欠)
function CulcAbsence() {
    var setSumAbsence = 0;

    if (document.forms[0].ABSENCE_DAYS.value == "")  document.forms[0].ABSENCE_DAYS.value  = 0;
    if (document.forms[0].ABSENCE_DAYS2.value == "") document.forms[0].ABSENCE_DAYS2.value = 0;
    if (document.forms[0].ABSENCE_DAYS3.value == "") document.forms[0].ABSENCE_DAYS3.value = 0;

    setSumAbsence = eval(document.forms[0].ABSENCE_DAYS.value)
                  + eval(document.forms[0].ABSENCE_DAYS2.value)
                  + eval(document.forms[0].ABSENCE_DAYS3.value);

    document.getElementById("ABSENCE_TOTAL").innerHTML = setSumAbsence;//出欠の記録(合計)

    return
}
// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab(obj) {
    //移動可能なオブジェクト
    var textFieldArray = document.forms[0].setTextField.value.split(",");
    // Ent13
    var e = window.event;
    if (e.keyCode != 13) {
        return;
    }

    for (var i = 0; i < textFieldArray.length; i++) {
        if (textFieldArray[i] == obj.name) {
            targetObject = eval("document.forms[0][\"" + textFieldArray[(i + 1)] + "\"]");
            Culc();
            targetObject.focus();
            if (obj.name != "DE003REMARK4") {
                targetObject.select();
            } else {
                e.keyCode = 8;//Backspace8
            }
            return;
        }
    }
}
