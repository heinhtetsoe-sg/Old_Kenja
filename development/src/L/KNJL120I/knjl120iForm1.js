function btn_submit(cmd) {
    //ページ切り替え
    if ((cmd == "back" || cmd == "next") && document.forms[0].HID_EXAMNO.value.length != 0 && document.forms[0].changeValFlg.value == "1") {
        if (!confirm("{rval MSG108}")) return false;
    }

    //取消
    if (cmd == "reset" && !confirm("{rval MSG106}")) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_EXAMNO.value.length == 0) {
        if (document.forms[0].HID_EXAMNO.value.length == 0) return false;
    }

    //終了
    if (cmd == "end") {
        if (document.forms[0].TESTDIV.disabled) {
            if (confirm("{rval MSG108}")) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changeDispDiv(obj) {
    //ページ切り替え
    if (document.forms[0].HID_EXAMNO.value.length != 0 && document.forms[0].changeValFlg.value == "1") {
        if (!confirm("{rval MSG108}")) {
            var div1 = document.getElementById("DISP_DIV1");
            var div2 = document.getElementById("DISP_DIV2");
            if (div1.checked) {
                div2.checked = !div2.checked;
            } else {
                div1.checked = !div1.checked;
            }
            return false;
        }
    }

    document.forms[0].cmd.value = "changeDispDiv";
    document.forms[0].submit();
    return false;
}

function calcPoint(testdiv, pointcd, value) {
    var tmp;
    var response = { PLUS_POINT: "0", MINUS_POINT: "0" };

    $.ajax({
        url: "knjl120iindex.php",
        type: "POST",
        data: {
            AJAX_TESTDIV: testdiv,
            AJAX_POINTCD: pointcd,
            AJAX_INPUT_VAL: value,
            cmd: "getPointMst",
        },
        async: false,
    }).done(function (data, textStatus, jqXHR) {
        response = $.parseJSON(data);
    });

    return response;
}

function calc(obj) {
    document.forms[0].btn_search.disabled = true;
    document.forms[0].btn_calc.disabled = true;
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_reset.disabled = true;
    document.forms[0].btn_end.disabled = true;
    var examnos = document.forms[0].HID_EXAMNO.value.split(",");
    var i;
    for (i = 0; i < examnos.length; i++) {
        setPoint1(null, examnos[i]);
    }
    document.forms[0].btn_search.disabled = false;
    document.forms[0].btn_calc.disabled = false;
    document.forms[0].btn_update.disabled = false;
    document.forms[0].btn_reset.disabled = false;
    document.forms[0].btn_end.disabled = false;
    alert("算出しました。\n更新ボタンで更新してください");
}

//表示区分 調査
function setPoint1(obj, examno, errFunc) {
    //エラーチェック関数実行
    if (typeof errFunc === "function") {
        errFunc(obj);
    }

    var testDiv = document.forms[0].TESTDIV.value;
    var dispDiv = document.getElementById("DISP_DIV1").checked ? "1" : "2";

    //評定
    var score1Element = document.forms[0]["SCORE1_" + examno];
    var score2Element = document.forms[0]["SCORE2_" + examno];
    var score3Element = document.forms[0]["SCORE3_" + examno];
    var score4Element = document.forms[0]["SCORE4_" + examno];
    var score5Element = document.forms[0]["SCORE5_" + examno];
    var score6Element = document.forms[0]["SCORE6_" + examno];
    var score7Element = document.forms[0]["SCORE7_" + examno];
    var score8Element = document.forms[0]["SCORE8_" + examno];
    var score9Element = document.forms[0]["SCORE9_" + examno];

    var score1 = Number(score1Element.value);
    var score2 = Number(score2Element.value);
    var score3 = Number(score3Element.value);
    var score4 = Number(score4Element.value);
    var score5 = Number(score5Element.value);
    var score6 = Number(score6Element.value);
    var score7 = Number(score7Element.value);
    var score8 = Number(score8Element.value);
    var score9 = Number(score9Element.value);
    var scoreTotal9 = score1 + score2 + score3 + score4 + score5 + score6 + score7 + score8 + score9;
    var scoreTotal3 = score5 + score6 + score7;

    //欠席
    var abDay1Element = document.forms[0]["ABSENCE_DAYS_" + examno];
    var abDay2Element = document.forms[0]["ABSENCE_DAYS2_" + examno];
    var abDay3Element = document.forms[0]["ABSENCE_DAYS3_" + examno];
    var abDay1 = Number(abDay1Element.value);
    var abDay2 = Number(abDay2Element.value);
    var abDay3 = Number(abDay3Element.value);
    var abTotalVal = abDay1 + abDay2 + abDay3;

    //A方式のみの項目
    var reportPlus = 0;
    var reportMinus = 0;
    var selfRecPlus = 0;
    var selfRecMinus = 0;
    var hyouteGoukei1_2 = 0;
    var achivementPlus = 0;
    var dousouPlus = 0;
    if (testDiv == "1") {
        //調査書
        reportPlus = Number(document.forms[0]["REPORT_PLUS_" + examno].value);
        reportMinus = Number(document.forms[0]["REPORT_MINUS_" + examno].value);
        //B方式のみの項目
    } else if (testDiv == "2") {
        //1・2年次評定合計
        hyouteGoukei1_2 = Number(document.forms[0]["TOTAL_HYOUTEI_" + examno].value);
        //業績
        achivementPlus = Number(document.forms[0]["ACHIEVEMENT_" + examno].value);
        //同窓
        dousouPlus = Number(document.forms[0]["DOUSOU_PLUS_" + examno].value);
        //自己推薦
        selfRecPlus = Number(document.forms[0]["SELF_REC_PLUS_" + examno].value);
        selfRecMinus = Number(document.forms[0]["SELF_REC_MINUS_" + examno].value);
    }

    //調整点
    var tyouseiPlus = Number(document.forms[0]["TYOUSEI_PLUS_" + examno].value);
    var tyouseiMinus = Number(document.forms[0]["TYOUSEI_MINUS_" + examno].value);

    //各項目ポイント算出
    var scoreTotal9Plus = 0;
    var scoreTotal9Minus = 0;
    var scoreTotal3Plus = 0;
    var scoreTotal3Minus = 0;
    var abTotalValPlus = 0;
    var abTotalValMinus = 0;
    var hyouteGoukei1_2Plus = 0;
    var hyouteGoukei1_2Minus = 0;

    if (testDiv == "1") {
        //実技3教科評定合計
        var result2 = calcPoint(testDiv, "3", scoreTotal3);
        scoreTotal3Plus = Number(result2["PLUS_POINT"]);
        scoreTotal3Minus = Number(result2["MINUS_POINT"]);
    } else if (testDiv == "2") {
        //1・2年次評定合計
        var result2 = calcPoint(testDiv, "2", hyouteGoukei1_2);
        hyouteGoukei1_2Plus = Number(result2["PLUS_POINT"]);
        hyouteGoukei1_2Minus = Number(result2["MINUS_POINT"]);
    }

    //※全ての科目評定が未入力ならポイント算出をしない
    var flg = score1Element.value != "";
    flg = flg || score2Element.value != "";
    flg = flg || score3Element.value != "";
    flg = flg || score4Element.value != "";
    flg = flg || score5Element.value != "";
    flg = flg || score6Element.value != "";
    flg = flg || score7Element.value != "";
    flg = flg || score8Element.value != "";
    flg = flg || score9Element.value != "";
    if (flg) {
        //3年次9教科評定合計
        var result1 = calcPoint(testDiv, "1", scoreTotal9);
        scoreTotal9Plus = Number(result1["PLUS_POINT"]);
        scoreTotal9Minus = Number(result1["MINUS_POINT"]);
    }

    //※全ての学年で欠席数が未入力ならポイント算出をしない
    if (abDay1Element.value != "" || abDay2Element.value != "" || abDay3Element.value != "") {
        //3年間合計欠席日数
        var result3 = calcPoint(testDiv, "4", abTotalVal);
        abTotalValPlus = Number(result3["PLUS_POINT"]);
        abTotalValMinus = Number(result3["MINUS_POINT"]);
    }

    //＋－合計ポイント算出
    var totalPlus = 0;
    totalPlus += scoreTotal9Plus;
    totalPlus += scoreTotal3Plus;
    totalPlus += abTotalValPlus;
    totalPlus += hyouteGoukei1_2Plus;
    totalPlus += achivementPlus;
    totalPlus += reportPlus;
    totalPlus += dousouPlus;
    totalPlus += selfRecPlus;
    totalPlus += tyouseiPlus;

    var totalMinus = 0;
    totalMinus += scoreTotal9Minus;
    totalMinus += scoreTotal3Minus;
    totalMinus += abTotalValMinus;
    totalMinus += hyouteGoukei1_2Minus;
    totalMinus += reportMinus;
    totalMinus += selfRecMinus;
    totalMinus += tyouseiMinus;

    //変更値を画面に反映
    if (dispDiv == "1") {
        document.getElementById("ABSENCE_TOTAL_" + examno).textContent = abTotalVal;
        document.getElementById("TOTAL_ALL_" + examno).textContent = scoreTotal9;
        document.getElementById("TOTAL3_" + examno).textContent = scoreTotal3;
    } else if (dispDiv == "2") {
        document.getElementById("TOTAL_PLUS_" + examno).textContent = totalPlus;
        document.getElementById("TOTAL_MINUS_" + examno).textContent = totalMinus;
    }

    //更新用にhiddenセット
    document.forms[0]["TOTAL9_PLUS_" + examno].value = scoreTotal9Plus;
    document.forms[0]["TOTAL9_MINUS_" + examno].value = scoreTotal9Minus;
    document.forms[0]["TOTAL3_PLUS_" + examno].value = scoreTotal3Plus;
    document.forms[0]["TOTAL3_MINUS_" + examno].value = scoreTotal3Minus;
    document.forms[0]["ABSENCE_TOTAL_PLUS_" + examno].value = abTotalValPlus;
    document.forms[0]["ABSENCE_TOTAL_MINUS_" + examno].value = abTotalValMinus;
    document.forms[0]["HYOUTEI1_2_PLUS_" + examno].value = hyouteGoukei1_2Plus;
    document.forms[0]["HYOUTEI1_2_MINUS_" + examno].value = hyouteGoukei1_2Minus;

    document.forms[0]["ABSENCE_TOTAL_" + examno].value = abTotalVal;
    document.forms[0]["TOTAL_ALL_" + examno].value = scoreTotal9;
    document.forms[0]["TOTAL3_" + examno].value = scoreTotal3;
    document.forms[0]["TOTAL_PLUS_" + examno].value = totalPlus;
    document.forms[0]["TOTAL_MINUS_" + examno].value = totalMinus;
}

function doChangeValFlg() {
    var changeValFlg = (document.forms[0].changeValFlg.value = "1");
}

/* 以下エラー処理 */

//各科目評定
function chkHyoutei(obj) {
    var errMsg = "";

    //基本チェック
    if (!chkCommonErr(obj)) {
        return false;
    }

    //範囲チェック(五段階)
    if (!chkRange(obj.value, 1, 5)) {
        errMsg = "{rval MSG913}" + "\n「1～5」の値を入力してください。" + "\n入力された文字列は削除されます。";
        doErrorProcess(obj, errMsg);
        return false;
    }

    return true;
}

//1・2年評定合計
function chkHyouteiTotal(obj) {
    var errMsg = "";

    //基本チェック
    if (!chkCommonErr(obj)) {
        return false;
    }

    //範囲チェック
    if (!chkRange(obj.value, "", 90)) {
        errMsg = "{rval MSG913}" + "\n90以下の値を入力してください。" + "\n入力された文字列は削除されます。";
        doErrorProcess(obj, errMsg);
        return false;
    }

    return true;
}

//業績
function chkArchivement(obj) {
    var errMsg = "";

    //基本チェック
    if (!chkCommonErr(obj)) {
        return false;
    }

    //包含チェック
    var validValues = new Array("0", "10", "20", "30");
    if (validValues.indexOf(obj.value) === -1) {
        errMsg = "{rval MSG901}" + "\n「0, 10, 20, 30」のいずれかの値を入力してください。" + "\n入力された文字列は削除されます。";
        doErrorProcess(obj, errMsg);
        return false;
    }

    return true;
}

//基本エラーチェック
function chkCommonErr(obj) {
    var convertedValue = toInteger(obj.value);

    //空値チェック
    if (obj.value == "") {
        return false;
    }
    //数値チェック
    if (convertedValue != obj.value) {
        doErrorProcess(obj, "");
        return false;
    }

    return true;
}

//範囲チェック
function chkRange(chkString, start, end) {
    var convertedValue = Number(chkString);
    var convertedStart = Number(start);
    var convertedEnd = Number(end);

    //範囲fromがない場合
    if (start === "") {
        if (convertedValue <= convertedEnd) {
            return true;
        }
    } else if (end === "") {
        if (convertedStart <= convertedValue) {
            return true;
        }
    } else {
        if (convertedStart <= convertedValue && convertedValue <= convertedEnd) {
            return true;
        }
    }
    return false;
}

//エラー時の処理
function doErrorProcess(obj, errMsg) {
    if (errMsg != "") {
        alert(errMsg);
    }
    obj.value = "";
    obj.focus();
}
