function btn_submit(cmd) {

    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }

    if (cmd == "update" || cmd == "delete") {
        //必須チェック
        if (document.forms[0].TOUROKU_DATE.value == ""){
            alert('{rval MSG304}'+'\n　　（ 登録日付 ）');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//戻る
function closeMethod() {
    window.opener.btn_submit('ret410_1');
    closeWin();
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    window.opener.btn_submit('ret410_1');
    closeWin();
}

//算出（通知票）
function keisanScore(sem) {
    //３・５・９科計
    total03 = eval("document.forms[0][\"TOTAL3_S_" + sem + "\"]");
    total05 = eval("document.forms[0][\"TOTAL5_S_" + sem + "\"]");
    total09 = eval("document.forms[0][\"TOTAL9_S_" + sem + "\"]");
    //各科
    sub01 = eval("document.forms[0][\"SUBCLASSCD01_S_" + sem + "\"]");
    sub02 = eval("document.forms[0][\"SUBCLASSCD02_S_" + sem + "\"]");
    sub03 = eval("document.forms[0][\"SUBCLASSCD03_S_" + sem + "\"]");
    sub04 = eval("document.forms[0][\"SUBCLASSCD04_S_" + sem + "\"]");
    sub05 = eval("document.forms[0][\"SUBCLASSCD05_S_" + sem + "\"]");
    sub06 = eval("document.forms[0][\"SUBCLASSCD06_S_" + sem + "\"]");
    sub07 = eval("document.forms[0][\"SUBCLASSCD07_S_" + sem + "\"]");
    sub08 = eval("document.forms[0][\"SUBCLASSCD08_S_" + sem + "\"]");
    sub09 = eval("document.forms[0][\"SUBCLASSCD09_S_" + sem + "\"]");

    val01 = (sub01.value == "") ? 0 : sub01.value;
    val02 = (sub02.value == "") ? 0 : sub02.value;
    val03 = (sub03.value == "") ? 0 : sub03.value;
    val04 = (sub04.value == "") ? 0 : sub04.value;
    val05 = (sub05.value == "") ? 0 : sub05.value;
    val06 = (sub06.value == "") ? 0 : sub06.value;
    val07 = (sub07.value == "") ? 0 : sub07.value;
    val08 = (sub08.value == "") ? 0 : sub08.value;
    val09 = (sub09.value == "") ? 0 : sub09.value;

    setSum3 = 0;
    if (document.forms[0].FLG3_01_S.value == "1") setSum3 += eval(val01);
    if (document.forms[0].FLG3_02_S.value == "1") setSum3 += eval(val02);
    if (document.forms[0].FLG3_03_S.value == "1") setSum3 += eval(val03);
    if (document.forms[0].FLG3_04_S.value == "1") setSum3 += eval(val04);
    if (document.forms[0].FLG3_05_S.value == "1") setSum3 += eval(val05);
    if (document.forms[0].FLG3_06_S.value == "1") setSum3 += eval(val06);
    if (document.forms[0].FLG3_07_S.value == "1") setSum3 += eval(val07);
    if (document.forms[0].FLG3_08_S.value == "1") setSum3 += eval(val08);
    if (document.forms[0].FLG3_09_S.value == "1") setSum3 += eval(val09);

    setSum5 = 0;
    if (document.forms[0].FLG5_01_S.value == "1") setSum5 += eval(val01);
    if (document.forms[0].FLG5_02_S.value == "1") setSum5 += eval(val02);
    if (document.forms[0].FLG5_03_S.value == "1") setSum5 += eval(val03);
    if (document.forms[0].FLG5_04_S.value == "1") setSum5 += eval(val04);
    if (document.forms[0].FLG5_05_S.value == "1") setSum5 += eval(val05);
    if (document.forms[0].FLG5_06_S.value == "1") setSum5 += eval(val06);
    if (document.forms[0].FLG5_07_S.value == "1") setSum5 += eval(val07);
    if (document.forms[0].FLG5_08_S.value == "1") setSum5 += eval(val08);
    if (document.forms[0].FLG5_09_S.value == "1") setSum5 += eval(val09);

    setSum9 = 0;
    if (document.forms[0].FLG9_01_S.value == "1") setSum9 += eval(val01);
    if (document.forms[0].FLG9_02_S.value == "1") setSum9 += eval(val02);
    if (document.forms[0].FLG9_03_S.value == "1") setSum9 += eval(val03);
    if (document.forms[0].FLG9_04_S.value == "1") setSum9 += eval(val04);
    if (document.forms[0].FLG9_05_S.value == "1") setSum9 += eval(val05);
    if (document.forms[0].FLG9_06_S.value == "1") setSum9 += eval(val06);
    if (document.forms[0].FLG9_07_S.value == "1") setSum9 += eval(val07);
    if (document.forms[0].FLG9_08_S.value == "1") setSum9 += eval(val08);
    if (document.forms[0].FLG9_09_S.value == "1") setSum9 += eval(val09);

    //合計欄が空白の時、セット
    if (total03.value == "") total03.value = setSum3;
    if (total05.value == "") total05.value = setSum5;
    if (total09.value == "") total09.value = setSum9;

    return
}

//算出（模試）
function keisanMock(mon) {
    //３・５科
    avg3 = eval("document.forms[0][\"AVG3_M_" + mon + "\"]");
    avg5 = eval("document.forms[0][\"AVG5_M_" + mon + "\"]");
    //各科
    sub01 = eval("document.forms[0][\"SUBCLASSCD01_M_" + mon + "\"]");
    sub02 = eval("document.forms[0][\"SUBCLASSCD02_M_" + mon + "\"]");
    sub03 = eval("document.forms[0][\"SUBCLASSCD03_M_" + mon + "\"]");
    sub04 = eval("document.forms[0][\"SUBCLASSCD04_M_" + mon + "\"]");
    sub05 = eval("document.forms[0][\"SUBCLASSCD05_M_" + mon + "\"]");

    setSum3 = 0;
    if (sub01.value != "") setSum3 += parseFloat(sub01.value);
    if (sub02.value != "") setSum3 += parseFloat(sub02.value);
    if (sub03.value != "") setSum3 += parseFloat(sub03.value);
    setCnt3 = 0;
    if (sub01.value != "") setCnt3 += 1;
    if (sub02.value != "") setCnt3 += 1;
    if (sub03.value != "") setCnt3 += 1;

    setSum5 = 0;
    if (sub01.value != "") setSum5 += parseFloat(sub01.value);
    if (sub02.value != "") setSum5 += parseFloat(sub02.value);
    if (sub03.value != "") setSum5 += parseFloat(sub03.value);
    if (sub04.value != "") setSum5 += parseFloat(sub04.value);
    if (sub05.value != "") setSum5 += parseFloat(sub05.value);
    setCnt5 = 0;
    if (sub01.value != "") setCnt5 += 1;
    if (sub02.value != "") setCnt5 += 1;
    if (sub03.value != "") setCnt5 += 1;
    if (sub04.value != "") setCnt5 += 1;
    if (sub05.value != "") setCnt5 += 1;

    //平均欄が空白の時、セット
    if (avg3.value == "" && setCnt3 == 3) {
        setAvg3 = parseFloat(setSum3) / setCnt3 * 10;
        avg3.value = Math.round(setAvg3) / 10;
    }
    if (avg5.value == "" && setCnt5 == 5) {
        setAvg5 = parseFloat(setSum5) / setCnt5 * 10;
        avg5.value = Math.round(setAvg5) / 10;
    }

    return
}

//模試名テキスト（その他）
function disCompanyText(obj) {
    var nam = obj.name;
    var val = obj.value;
    var mon = nam.split("_M_")[1];
    var name = nam.split("_M_")[0];
    name = name.replace("COMPANYCD", "COMPANY_TEXT");
    targetObject = eval("document.forms[0][\"" + name + "_M_" + mon + "\"]");
    //その他を選択した時、入力可能
    if (val == '00009999') {
        targetObject.disabled = false;
    } else {
        targetObject.disabled = true;
    }
}

//TOP2（同一月不可）、TOP2の偏差値平均
function calcMock() {
    //３科５科の入力値を１つの配列にセット。あわせて月も配列にセット。
    var mocks = new Array();
    cnt = 0;
    for (var i = 4; i <= 12; i++) {
        mon = ("0"+i).slice(-2);
        targetObjectCD = eval("document.forms[0][\"COMPANYCD_M_" + mon + "\"]");
        targetObjectTEXT = eval("document.forms[0][\"COMPANY_TEXT_M_" + mon + "\"]");
        //３科
        targetObject = eval("document.forms[0][\"AVG3_M_" + mon + "\"]");
        if (targetObject.value != '') {
            var obj = {};
            obj.month = mon;
            obj.avg = targetObject.value;
            obj.companycd = targetObjectCD.value;
            obj.companyText = targetObjectTEXT.value;
            obj.subcnt = 3;
            mocks[cnt] = obj;
            cnt++;
        }
        //５科
        targetObject = eval("document.forms[0][\"AVG5_M_" + mon + "\"]");
        if (targetObject.value != '') {
            var obj = {};
            obj.month = mon;
            obj.avg = targetObject.value;
            obj.subcnt = 5;
            obj.companycd = targetObjectCD.value;
            obj.companyText = targetObjectTEXT.value;
            mocks[cnt] = obj;
            cnt++;
        }
    }
    if (cnt == 0) {
        alert('３科、５科を入力して下さい。');
        return;
    }
    //平均(降順)、３科、５科、の順に並び替え
    mocks.sort(function(a,b){
        if (a.avg > b.avg) return -1;
        if (a.avg < b.avg) return 1;
        if (a.subcnt < b.subcnt) return -1;
        if (a.subcnt > b.subcnt) return 1;
        return 0;
    });
    //TOP1,TOP2を取得
    top1 = "";
    subcnt1 = "";
    companycd1 = "";
    companyText1 = "";
    top2 = "";
    subcnt2 = "";
    companycd2 = "";
    companyText2 = "";
    monthKeep1 = "";
    for (var i = 0; i < mocks.length; i++ ) {
        //TOP1（MAX値）
        if (i == 0) {
            top1 = mocks[i].avg;
            subcnt1 = mocks[i].subcnt;
            companycd1 = mocks[i].companycd;
            companyText1 = mocks[i].companyText;
            monthKeep1 = mocks[i].month;
        //TOP2（TOP1と同一月不可）
        } else if (monthKeep1 != mocks[i].month) {
            top2 = mocks[i].avg;
            subcnt2 = mocks[i].subcnt;
            companycd2 = mocks[i].companycd;
            companyText2 = mocks[i].companyText;
        }
        //TOP1,TOP2取得したらループを抜ける
        if (top2 != "") break;
    }
    //TOP1をセット
//    targetObject3 = document.getElementById('ID_TOP1_AVG3_M_99');
//    targetObject5 = document.getElementById('ID_TOP1_AVG5_M_99');
    hiddenObject3 = eval("document.forms[0][\"TOP1_AVG3_M_99\"]");
    hiddenObject5 = eval("document.forms[0][\"TOP1_AVG5_M_99\"]");
    hiddenObjectC = eval("document.forms[0][\"TOP1_COMPANYCD_M_99\"]");
    hiddenObjectT = eval("document.forms[0][\"TOP1_COMPANY_TEXT_M_99\"]");
    if (subcnt1 == 3) {
//        targetObject3.innerHTML = top1;
//        targetObject5.innerHTML = "";
        hiddenObject3.value = top1;
        hiddenObject5.value = "";
        hiddenObjectC.value = companycd1;
        hiddenObjectT.value = companyText1;
    } else if (subcnt1 == 5) {
//        targetObject3.innerHTML = "";
//        targetObject5.innerHTML = top1;
        hiddenObject3.value = "";
        hiddenObject5.value = top1;
        hiddenObjectC.value = companycd1;
        hiddenObjectT.value = companyText1;
    } else {
//        targetObject3.innerHTML = "";
//        targetObject5.innerHTML = "";
        hiddenObject3.value = "";
        hiddenObject5.value = "";
        hiddenObjectC.value = "";
        hiddenObjectT.value = "";
    }
    //その他を選択した時、入力可能
    if (hiddenObjectC.value == '00009999') {
        hiddenObjectT.disabled = false;
    } else {
        hiddenObjectT.disabled = true;
    }
    //TOP2をセット
//    targetObject3 = document.getElementById('ID_TOP2_AVG3_M_99');
//    targetObject5 = document.getElementById('ID_TOP2_AVG5_M_99');
    hiddenObject3 = eval("document.forms[0][\"TOP2_AVG3_M_99\"]");
    hiddenObject5 = eval("document.forms[0][\"TOP2_AVG5_M_99\"]");
    hiddenObjectC = eval("document.forms[0][\"TOP2_COMPANYCD_M_99\"]");
    hiddenObjectT = eval("document.forms[0][\"TOP2_COMPANY_TEXT_M_99\"]");
    if (subcnt2 == 3) {
//        targetObject3.innerHTML = top2;
//        targetObject5.innerHTML = "";
        hiddenObject3.value = top2;
        hiddenObject5.value = "";
        hiddenObjectC.value = companycd2;
        hiddenObjectT.value = companyText2;
    } else if (subcnt2 == 5) {
//        targetObject3.innerHTML = "";
//        targetObject5.innerHTML = top2;
        hiddenObject3.value = "";
        hiddenObject5.value = top2;
        hiddenObjectC.value = companycd2;
        hiddenObjectT.value = companyText2;
    } else {
//        targetObject3.innerHTML = "";
//        targetObject5.innerHTML = "";
        hiddenObject3.value = "";
        hiddenObject5.value = "";
        hiddenObjectC.value = "";
        hiddenObjectT.value = "";
    }
    //その他を選択した時、入力可能
    if (hiddenObjectC.value == '00009999') {
        hiddenObjectT.disabled = false;
    } else {
        hiddenObjectT.disabled = true;
    }
    //TOP2の偏差値平均（TOP1とTOP2の平均）をセット
//    targetObject = document.getElementById('ID_TOP_AVG_M_99');
    hiddenObject = eval("document.forms[0][\"TOP_AVG_M_99\"]");
    if (top1 != "" && top2 != "") {
        avg = (parseFloat(top1) + parseFloat(top2)) / 2 * 10;
//        targetObject.innerHTML = Math.round(avg) / 10;
        hiddenObject.value = Math.round(avg) / 10;
    } else if (top1 != "") {
//        targetObject.innerHTML = top1;
        hiddenObject.value = top1;
    } else if (top2 != "") {
//        targetObject.innerHTML = top2;
        hiddenObject.value = top2;
    } else {
//        targetObject.innerHTML = "";
        hiddenObject.value = "";
    }
}
