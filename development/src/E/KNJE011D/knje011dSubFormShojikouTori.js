//サブミット
function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//取込処理
function dataPositionSet(n) {
    var i, mi;
    var seq = [];
    if (n) {
        seq.push(n);
    } else {
        for (i = 1; i <= 6; i++) {
            seq.push(i);
        }
    }
    var message;
    var insertPos = false;
    for (i = 0; i < seq.length; i++) {
        var val = document.getElementById("REMARK" + seq[i]).value;

        message = "";
        if (val) {
            message += val;
        }

        var mainMsg = message;

        textRange = null;
        var textarea = parent.document.forms[0]["TRAIN_REF" + seq[i]];
        textarea.focus();

        //IE11未満のとき
        if (document.selection) {
            textRange = document.selection.createRange();
            textRange.text = mainMsg;
        } else {
            var sentence = textarea.value;
            if (insertPos) {
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
            } else {
                var text = sentence && mainMsg ? sentence + "\n" + mainMsg : (sentence ? sentence : mainMsg);
                textarea.value = text;
            }

        }
    }
}
