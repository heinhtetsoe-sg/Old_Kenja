function btn_submit(cmd) {   
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) return true;
    }
    //サブミット時、一旦、左リストをクリア
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    attribute4 = document.forms[0].selectdataLabel;
    attribute4.value = "";
    //右クラス変更と更新時、左リストを保持
    if (cmd == 'change_menu' || cmd == 'update') {
        sep = "";
        for (var i = 0; i < document.forms[0].LEFT_PART.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].LEFT_PART.options[i].value;
            attribute4.value = attribute4.value + sep + document.forms[0].LEFT_PART.options[i].text;
            sep = ",";
        }

    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//タイトル追加
function titleAdd() {

    var option = document.createElement('option');

    option.setAttribute('value', 'TITLE');
    option.innerHTML = '【TITLE】' + document.forms[0].TITLE_NAME.value;

    document.forms[0].LEFT_PART.appendChild(option);

    return false;
}
//生徒移動
function moveStudent(side, sort) {
    move(side,'LEFT_PART','RIGHT_PART',sort);

}
//子画面へ
function openCheck() {
    alert('複式クラス生徒一覧に生徒が一人も割振られていません。\n割り振り後は更新ボタンを押してください。');
    return;
}

function openCheck2() {
    if (!confirm('{rval MSG108}')) return true;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}
function closecheck() {
    parent.window.close();
}
