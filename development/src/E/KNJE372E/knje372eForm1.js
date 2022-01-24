function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }
    //サブミット
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//CSV処理画面を開く前処理
function csvConfirm() {
    if (document.forms[0].ADJUSTMENT_SCORE_CHANGED.value == "1" && !confirm('{rval MSG108}')) {
        return false;
    }
    
    return true;
}

//換算値順位を変更時セルの背景色を変更する
function adjustChange(schregNo, textBox) {

    //換算値合計を再計算
    var attendName = "ATTEND_" + schregNo;
    var attend = document.getElementById(attendName).innerText;
    var adjustTotalName = "ADJUST_TOTAL_" + schregNo;
    total = Number(attend) + Number(textBox.value);
    document.getElementById(adjustTotalName).innerText = total.toFixed(0);

    var scoreName = "SCORE_" + schregNo;
    var score = document.getElementById(scoreName).innerText;
    var totalName = "TOTAL_" + schregNo;
    var total = document.getElementById(scoreName).innerText;
    total = Number(score) + Number(attend) + Number(textBox.value);
    document.getElementById(totalName).innerText = total.toFixed(1);

    if (textBox.value != textBox.defaultValue) {
        //換算値順位変更
        document.forms[0].ADJUSTMENT_SCORE_CHANGED.value = "1";
    }
}

setTimeout(function () {
    window.onload = new function () {
        if (sessionStorage.getItem("KNJE372EForm1_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJE372EForm1_CurrentCursor")).focus();
        }
    }
}, 800);

function current_cursor(para) {
    sessionStorage.setItem("KNJE372EForm1_CurrentCursor", para);
}
