//サブミット
function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//ボタンの使用不可
function OptionUse(obj) {
    var check_flg = false;
    var semesterCnt = document.forms[0].SEMSETERCNT.value;

    for (var i = 1; i <= semesterCnt; i++) {
        if (document.forms[0]['CHECKED' + i].checked == true) {
            check_flg = true;
        }
    }
    if (check_flg == true) {
        document.forms[0].btn_torikomi.disabled = false;
    } else {
        document.forms[0].btn_torikomi.disabled = true;
    }
}
//取込処理
function dataPositionSet(target) {
    var semesterCnt = document.forms[0].SEMSETERCNT.value;

    if (!semesterCnt) {
        return;
    }

    var mainMsg = '';
    var sep = '';
    for (var i = 1; i <= semesterCnt; i++) {
        if (document.forms[0]['CHECKED' + i].checked == true) {
            var val = document.forms[0]['VALUE' + i].value;

            if (val != '') {
                mainMsg += sep + val;
                sep = '　';
            }
        }
    }

    textRange = null;
    parent.document.forms[0][target].focus();
    var textarea = parent.document.forms[0][target];

    //IE11未満のとき
    if (document.selection) {
        textRange = document.selection.createRange();
        textRange.text = mainMsg;

    } else {
        var sentence = textarea.value;
        var len      = sentence.length;
        var pos      = textarea.selectionStart || len;

        var before   = sentence.substr(0, pos);
        var word     = mainMsg;
        var after    = sentence.substr(pos, len);

        sentence = before + word;
        move_pos = sentence.length;
        sentence += after;
        textarea.value = sentence;

        if (textarea.createTextRange) {
            var range = textarea.createTextRange();
            range.move('character', move_pos);
            range.select();
        } else if (textarea.setSelectionRange) {
            textarea.setSelectionRange(move_pos, move_pos);
        }
    }
}
