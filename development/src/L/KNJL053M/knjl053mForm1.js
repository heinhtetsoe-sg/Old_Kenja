function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_val(obj) {
    if (!obj.value.match(/^0$|^1$/)) {
        alert('入力可能は 0 または 1 のみです。');
        obj.value = '0';
    }
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}
