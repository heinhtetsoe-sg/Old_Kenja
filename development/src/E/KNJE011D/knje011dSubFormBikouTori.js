//サブミット
function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック操作
function check_all(all) {
    var years = document.getElementsByName("years")[0].value.split(",");
    var i;
    for (i = 0; i < years.length; i++) {
        document.getElementById("CHECK_" + years[i]).checked = all.checked;
    }
}

//ボタンの使用不可
function OptionUse(obj) {
    var check_flg1 = false;

    //選択チェック
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked == true) {
            check_flg1 = true;
        }
    }

    document.forms[0].btn_torikomi.disabled = !check_flg1;
}

//取込処理
function dataPositionSet(target) {

    var rows = document.getElementsByClassName("rowselect");

    var mainMsg = '';
    var i, val, j, item, row, year, data;
    for (i = 0; i < rows.length; i++) {
        var row = rows[i];
        if (!row.checked) {
            continue;
        }
        year = row.id.split("_")[1];
        data = document.getElementById("REMARK_" + year).value;
        if (data) {
            if (mainMsg) {
                mainMsg += "\n";
            }
            mainMsg += data;
        }
    }

    textRange = null;
    var textarea = parent.document.forms[0][target];
    textarea.focus();

    //IE11未満のとき
    if (document.selection) {
        textRange = document.selection.createRange();
        textRange.text = mainMsg;
    } else {
        var sentence = textarea.value;
        var before   = sentence.substr(0, textarea.selectionStart);
        var after    = sentence.substr(textarea.selectionStart, sentence.length);
        var move_pos = (before + mainMsg).length;
        textarea.value = before + mainMsg + after;

        if (textarea.createTextRange) {
            var range = textarea.createTextRange();
            range.move('character', move_pos);
            range.select();
        } else if (textarea.setSelectionRange) {
            textarea.setSelectionRange(move_pos, move_pos);
        }
    }
}
