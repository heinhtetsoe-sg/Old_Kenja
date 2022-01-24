//全チェック操作
function check_all(obj) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == 'CHECKED[]') {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

//ボタンの使用不可
function OptionUse(obj) {
    var check_flg1 = (check_flg2 = false);

    //選択チェック
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == 'CHECKED[]' && document.forms[0].elements[i].checked == true) {
            check_flg1 = true;
        }
    }

    //出力項目チェック
    var itemArray = document.forms[0].item.value.split(',');
    for (var j = 0; j < itemArray.length; j++) {
        if (document.forms[0]['CHECK_' + itemArray[j]].checked == true) {
            check_flg2 = true;
        }
    }

    if (check_flg1 == true && check_flg2 == true) {
        document.forms[0].btn_torikomi.disabled = false;
    } else {
        document.forms[0].btn_torikomi.disabled = true;
    }
}

//取込処理
function dataPositionSet(target) {
    var itemArray = document.forms[0].item.value.split(',');

    mainMsg = '';
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == 'CHECKED[]' && document.forms[0].elements[i].checked == true) {
            var val = document.forms[0].elements[i].value;

            message = '';
            for (var j = 0; j < itemArray.length; j++) {
                var item = document.forms[0]['CHECK_' + itemArray[j]];
                tmpData = document.forms[0][item.value + ':' + val].value;
                if (item.checked == true && tmpData.length > 0) {
                    if (message.length > 0 && tmpData.length > 0) message += '　';
                    message += tmpData;
                }
            }

            if (mainMsg.length > 0 && message.length > 0) mainMsg += '\n';
            mainMsg += message;
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
        var len = sentence.length;
        var pos = textarea.selectionStart;

        var before = sentence.substr(0, pos);
        var word = mainMsg;
        var after = sentence.substr(pos, len);

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
