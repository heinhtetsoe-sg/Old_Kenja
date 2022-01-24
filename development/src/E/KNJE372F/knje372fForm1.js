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
    if (document.forms[0].RANK_CHANGED.value == "1" && !confirm('{rval MSG108}')) {
        return false;
    }
    
    return true;
}

//換算値順位を変更時セルの背景色を変更する
function rankChange(schregNo, textBox) {

    if (textBox.value != textBox.defaultValue) {
        // var bgcolor = "#66CDAA";
        var bgcolor = "#9DCCE0";
        //背景色変更
        var scoreName = "SCORE_" + schregNo;
        document.getElementById(scoreName).style.backgroundColor = bgcolor;
        var totalName = "TOTAL_" + schregNo;
        document.getElementById(totalName).style.backgroundColor = bgcolor;
        var rankName = "RANK_" + schregNo;
        document.getElementById(rankName).style.backgroundColor = bgcolor;
        //換算値順位変更
        document.forms[0].RANK_CHANGED.value = "1";
    }
}

setTimeout(function () {
    window.onload = new function () {
        if (sessionStorage.getItem("KNJE372FForm1_CurrentCursor") != null) {
            document.title = "";
            document.getElementById(sessionStorage.getItem("KNJE372FForm1_CurrentCursor")).focus();
        }
    }
}, 800);

function current_cursor(para) {
    sessionStorage.setItem("KNJE372FForm1_CurrentCursor", para);
}
