function btn_submit(cmd) {   
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) return true;
    }
    if (cmd == 'update') {
        if (document.forms[0].HANDICAP.value == "") {
            alert('{rval MSG310}' + '(区分)');
            return false;
        }
    }

    if (cmd == 'copy') {
        if (!confirm('{rval MSG101}')) {
            return false;
        }
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
