//サブミット
function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全チェック操作
function check_all(obj) {
    var checks = document.getElementsByName('CHECK_LINE');
    if (!checks) {
        return;
    }
    for (let index = 0; index < checks.length; index++) {
        const check = checks[index];
        check.checked = obj.checked;
    }
}

//ボタンの使用不可
function OptionUse(obj) {
    var check_flg1 = check_flg2 = false;

    document.forms[0].btn_torikomi.disabled = true;

    var checks = document.getElementsByName('CHECK_SEMESTER');
    if (!checks) {
        return;
    }
    for (let index = 0; index < checks.length; index++) {
        const check = checks[index];
        if (check.checked) {
            check_flg1 = true;
        }
    }

    var checks = document.getElementsByName('CHECK_LINE');
    if (!checks) {
        return;
    }
    for (let index = 0; index < checks.length; index++) {
        const check = checks[index];
        if (check.checked) {
            check_flg2 = true;
        }
    }
    // 行・列にチェックがあれば［取込］ボタンの使用可
    if (check_flg1 && check_flg2) {
        document.forms[0].btn_torikomi.disabled = false;
    }

}

//取込処理
function dataPositionSet (target) {
    var mainMsg = '';
    var checkSemester = document.getElementsByName('CHECK_SEMESTER');
    if (!checkSemester) {
        return;
    }
    var checkLine = document.getElementsByName('CHECK_LINE');
    if (!checkLine) {
        return;
    }
    // 行・列 から取込む対象のテキストエリアから対象文字列を取得
    for (let index = 0; index < checkSemester.length; index++) {
        const semester = checkSemester[index];
        if (semester.checked) {
            var message = '';

            for (let i = 0; i < checkLine.length; i++) {
                const line = checkLine[i];
                if (line.checked) {
                    var semeVal = semester.value;
                    var lineVal = line.value;
                    var text = document.getElementsByName(semeVal + '_' + lineVal)[0];
                    if (text && text.value.length > 0) {
                        if (message.length > 0) message += ' '; 
                        message += text.value;
                    }
                }
            }
            if (mainMsg.length > 0 && message.length > 0) mainMsg += '\n';
            mainMsg += message;
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
        var pos      = textarea.selectionStart;

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

// キーダウンイベントでコピーを可能にする
function checkKeyDown(event) {
    // コピーのみ許可する
    if (event.ctrlKey && event.key == 'c') {
        return true;
    }
    return false;
}