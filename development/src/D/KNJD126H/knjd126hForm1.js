function btn_submit(cmd) {
    //編集中フラグON
    if (cmd == 'calc') {
        setupFlgOn();
    } else {
        //編集中かどうかの確認
        if (cmd != 'update' && !setupFlgCheck()) return;
    }
    //編集中フラグOFF
    if (cmd == 'reset') {
        setupFlgOff();
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//編集フラグ 1:編集中
function setupFlgOn() {
    document.forms[0].setupFlg.value = "1";
}
function setupFlgOff() {
    document.forms[0].setupFlg.value = "";
}
function setupFlgCheck() {
    setupFlg = document.forms[0].setupFlg.value;
    if (setupFlg == "1" && !confirm('{rval MSG108}')) {
        return false;
    }
    setupFlgOff();
    return true;
}

//終了
function btnEnd() {
    if (!setupFlgCheck()) return;
    closeWin();
}

//スクロール
function scrollRC(){
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
    document.getElementById('trowFoot').scrollLeft = document.getElementById('tbody').scrollLeft;
}
