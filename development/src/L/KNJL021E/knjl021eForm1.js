function btn_submit(cmd) {
    //取消確認
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

    //画面クリア
    if (cmd == 'disp_clear') {
        if (vflg == true) {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }

        for (i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].type == 'select-one' || document.forms[0].elements[i].type == 'text' || document.forms[0].elements[i].name == 'REMARK1') {
                if (document.forms[0].elements[i].type == 'select-one') {
                    document.forms[0].elements[i].value = document.forms[0].elements[i].options[0].value;
                } else {
                    document.forms[0].elements[i].value = "";
                }
            }
        }
        outputLAYER('DESIREDIV', '');
        outputLAYER('NAME', '');
        outputLAYER('NAME_KANA', '');
        outputLAYER('SEX', '');
        outputLAYER('BIRTHDAY', '');
        outputLAYER('FS_CD', '');
        outputLAYER('FS_GRDYEAR', '');
        outputLAYER('CONF1_AVG5', '0');
        outputLAYER('CONF1_AVG9', '0');
        outputLAYER('CONF2_AVG5', '0');
        outputLAYER('CONF2_AVG9', '0');
        outputLAYER('CONF3_AVG5', '0');
        outputLAYER('CONF3_AVG9', '0');
        var kyoukasu = eval(document.forms[0].kyouka_count.value);
        for (i=1; i <= kyoukasu; i++) {
            outputLAYER('SUBAVG0'+ i, '0');
        }
        outputLAYER('CONF_AVG5', '0');
        outputLAYER('CONF_AVG9', '0');
        outputLAYER('ABSENCE_DAYS_TOTAL', '0');

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
    //教科数
    var kyoukasu = eval(document.forms[0].kyouka_count.value);

    //5教科の科目番号
    var setSubNum = document.forms[0].kyouka5.value.split(',').map(Number);
    var setSubNumCnt = setSubNum.length;

    for (var i=1; i <= kyoukasu; i++) {
        //初期化
        eval("var class" + i + "Array = []");

        //空の場合、0をセット
        if (document.getElementById("CONF1_RPT0" + i).value == "") document.getElementById("CONF1_RPT0" + i).value = 0;
        if (document.getElementById("CONF2_RPT0" + i).value == "") document.getElementById("CONF2_RPT0" + i).value = 0;
        if (document.getElementById("CONFIDENTIAL_RPT0" + i).value == "") document.getElementById("CONFIDENTIAL_RPT0" + i).value = 0;
    }

    for (var grade=1; grade <= 3; grade++) {
        //初期化
        eval("var conf" + grade + "Array = []");
        eval("var conf" + grade + "Array5 = []");
    }

    //各算出対象の値を取得
    for (var i=1; i <= kyoukasu; i++) {
        for (var grade=1; grade <= 3; grade++) {
            var itemName = (grade == 3) ? "CONFIDENTIAL_RPT0" : "CONF"+grade+"_RPT0";
            eval("conf"+grade+"Array.push("+document.getElementById(itemName+i).value+")");
            if (i <= setSubNumCnt) eval("conf"+grade+"Array5.push("+document.getElementById(itemName+setSubNum[i-1]).value+")");
            eval("class"+i+"Array.push("+document.getElementById(itemName+i).value+")");
        }
    }

    //教科ごとの算出
    for (var i=1; i <= kyoukasu; i++) {
        /*****各教科*****/
        //合計
        eval("var class" + i + "Sum = class"+ i + "Array.reduce(function(p, c) { return p + c; })");
        //平均
        eval("var class" + i + "Avg = Math.round((class"+ i + "Sum / class"+ i + "Array.length) * 10) / 10");
        //セット
        document.getElementById("SUBAVG0"+i).innerHTML = eval("class"+i+"Avg");
    }

    //学年ごとの算出
    for (var grade=1; grade <= 3; grade++) {
        /*****５教科*****/
        //合計
        eval("var conf" + grade + "Sum5 = conf"+ grade + "Array5.reduce(function(p, c) { return p + c; })");
        //平均
        eval("var conf" + grade + "Avg5 = Math.round((conf"+ grade + "Sum5 / conf"+ grade + "Array5.length) * 10) / 10");
        //セット
        document.getElementById("CONF"+grade+"_AVG5").innerHTML = eval("conf"+grade+"Avg5");

        /*****全教科*****/
        //合計
        eval("var conf" + grade + "Sum = conf"+ grade + "Array.reduce(function(p, c) { return p + c; })");
        //平均
        eval("var conf" + grade + "Avg = Math.round((conf"+ grade + "Sum / conf"+ grade + "Array.length) * 10) / 10");
        //セット
        document.getElementById("CONF"+grade+"_AVG9").innerHTML = eval("conf"+grade+"Avg");

        //hiddenにセット
        if (grade == 3) {
            document.forms[0]["TOTAL5"].value = eval("conf" + grade + "Sum5");
            document.forms[0]["TOTAL_ALL"].value = eval("conf" + grade + "Sum");
        } else {
            document.forms[0]["CONF" + grade + "_RPT10"].value = eval("conf" + grade + "Sum5");
            document.forms[0]["CONF" + grade + "_RPT11"].value = eval("conf" + grade + "Sum");
        }
    }

    /*****全学年の５教科*****/
    //合計
    var confSum5 = conf1Sum5 + conf2Sum5 + conf3Sum5;
    //件数
    var confCnt5 = conf1Array5.length + conf2Array5.length + conf3Array5.length;
    //平均
    eval("var confAvg5 = Math.round((confSum5 / confCnt5) * 10) / 10");
    //セット
    document.getElementById("CONF_AVG5").innerHTML = confAvg5;

    /*****全学年の全教科*****/
    var confSum = conf1Sum + conf2Sum + conf3Sum;
    var confCnt = conf1Array.length + conf2Array.length + conf3Array.length;
    eval("var confAvg = Math.round((confSum / confCnt) * 10) / 10");
    document.getElementById("CONF_AVG9").innerHTML = confAvg;

    return;
}

//計算(欠席数)
function CulcAbs() {
    //空の場合、0をセット
    if (document.getElementById("ABSENCE_DAYS").value  == "") document.getElementById("ABSENCE_DAYS").value  = 0;
    if (document.getElementById("ABSENCE_DAYS2").value == "") document.getElementById("ABSENCE_DAYS2").value = 0;
    if (document.getElementById("ABSENCE_DAYS3").value == "") document.getElementById("ABSENCE_DAYS3").value = 0;

    /*****欠席数*****/
    var ab1   = parseInt(document.getElementById("ABSENCE_DAYS").value);
    var ab2   = parseInt(document.getElementById("ABSENCE_DAYS2").value);
    var ab3   = parseInt(document.getElementById("ABSENCE_DAYS3").value);
    var abSum = ab1 + ab2 + ab3;
    document.getElementById("ABSENCE_DAYS_TOTAL").innerHTML = abSum;

    return;
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
            if (e.shiftKey) {
                targetObject = eval("document.forms[0][\"" + textFieldArray[(i - 1)] + "\"]");
            } else {
                targetObject = eval("document.forms[0][\"" + textFieldArray[(i + 1)] + "\"]");
            }
            Culc();
            CulcAbs();
            targetObject.focus();
            if (obj.name != "REMARK1") {
                targetObject.select();
            } else {
                e.keyCode = 8;  //Backspace8
            }

            return;
        }
    }
}
