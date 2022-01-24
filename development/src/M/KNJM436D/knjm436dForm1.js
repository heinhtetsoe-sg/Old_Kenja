function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//入力チェック
function calc(obj, schregNo){
    var str = obj.value;

    //評定
    if (!str.match(/1|2|3|4|5/)) {
        alert('{rval MSG901}'+'「1～5」を入力して下さい。');
        obj.value = '';
        return;
    }
    return;
}
//エンターキーをTabに変換
function changeEnterToTab(obj, idx) {
    var name     = obj.name;
    var valueArr = document.getElementsByClassName("VALUE");
    var gCreArr = document.getElementsByClassName("GET_CREDIT");
    var cCreArr = document.getElementsByClassName("COMP_CREDIT");

    // Ent13
    var e = window.event;
    if (e.keyCode != 13) {
        return;
    }

    if (name.match(/VALUE-./)) {
        if (gCreArr[idx - 1]) {
            gCreArr[idx - 1].focus();
        }
    }

    if (name.match(/GET_CREDIT-./)) {
        if (cCreArr[idx - 1]) {
            cCreArr[idx - 1].focus();
        }
    }

    if (name.match(/COMP_CREDIT-./)) {
        if (valueArr[idx]) {
            valueArr[idx].focus();
        }
    }
    return;
}
function clickCredit(obj, schregNo) {
    if (document.forms[0]["VALUE-" + schregNo].value == "") {
        obj.checked = false;
    }

    return;
}
