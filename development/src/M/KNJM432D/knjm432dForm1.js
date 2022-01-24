function btn_submit(cmd) {
    if (cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//入力チェック
function calc(obj, schregNo){
    var str = obj.value;

    //空欄
    if (str == '') {
        document.getElementById("GET_CREDIT-" + schregNo).checked = false;
        return;
    }

    //評定
    if (!str.match(/1|2|3|4|5/)) {
        alert('{rval MSG901}'+'「1～5」を入力して下さい。');
        obj.value = '';
        document.getElementById("GET_CREDIT-" + schregNo).checked = false;
        return;
    }
    return;
}
//エンターキーをTabに変換
function changeEnterToTab(obj, idx) {
    var name     = obj.name;
    var scoreArr = document.getElementsByClassName("SCORE");
    var valueArr = document.getElementsByClassName("VALUE");

    // Ent13
    var e = window.event;
    if (e.keyCode != 13) {
        return;
    }

    if (name.match(/SCORE-./)) {
        if (valueArr[idx - 1]) {
            valueArr[idx - 1].focus();
        }
    }

    if (name.match(/VALUE-./)) {
        if (scoreArr[idx]) {
            scoreArr[idx].focus();
        }
    }
    return;
}
function clickCredit(obj, schregNo, m025cnt) {
    if (m025cnt == 0 && document.forms[0]["VALUE-" + schregNo].value == "") {
        obj.checked = false;
    }

    return;
}
