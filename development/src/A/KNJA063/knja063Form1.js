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
    if (cmd == 'change_hr_class' || cmd == 'update') {
        sep = "";
        for (var i = 0; i < document.forms[0].LEFT_PART.length; i++) {
            attribute3.value = attribute3.value + sep + document.forms[0].LEFT_PART.options[i].value;
            attribute4.value = attribute4.value + sep + document.forms[0].LEFT_PART.options[i].text;
            sep = ",";
        }

        with(document.forms[0]){
            document.getElementById("RIGHT_NUM").innerHTML = LEFT_PART.options.length;
            document.getElementById("LEFT_NUM").innerHTML = RIGHT_PART.options.length;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//生徒移動
function moveStudent(side) {
    move(side,'LEFT_PART','RIGHT_PART',1);

    with(document.forms[0]){
        document.getElementById("RIGHT_NUM").innerHTML = LEFT_PART.options.length;
        document.getElementById("LEFT_NUM").innerHTML = RIGHT_PART.options.length;
    }
}
//子画面へ
function openCheck() {
    alert('複式クラス生徒一覧に生徒が一人も割り振られていません。\n割り振り後は更新ボタンを押してください。');
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

