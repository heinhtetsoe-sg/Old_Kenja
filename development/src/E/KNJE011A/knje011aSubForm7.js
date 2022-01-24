//サブミット
function btn_submit(cmd){
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//取込処理
function dataPositionSet(target) {

    var val = document.getElementById("REMARK").value;

    var message = "";
    if (val) {
        message += val;
    }

    var mainMsg = '';
    if (mainMsg.length > 0 && message.length > 0) mainMsg += '\n';
    mainMsg += message;

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
