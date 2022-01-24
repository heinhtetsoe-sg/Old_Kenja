//サブミット
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//disable
function OptionUse(obj) {
    if (document.forms[0].CHECK_SUSPEND.checked  == false &&
        document.forms[0].CHECK_MOURNING.checked == false &&
        document.forms[0].CHECK_SICK.checked     == false &&
        document.forms[0].CHECK_LATE.checked == false &&
        document.forms[0].CHECK_EARLY.checked == false) 
    {
        document.forms[0].btn_torikomi.disabled = true;
    } else {
        document.forms[0].btn_torikomi.disabled = false;
    }
}

//取込処理
function dataPositionSet (target) {
    var suspend  = document.forms[0].CHECK_SUSPEND;
    var mourning = document.forms[0].CHECK_MOURNING;
    var sick     = document.forms[0].CHECK_SICK;
    var late     = document.forms[0].CHECK_LATE;
    var early    = document.forms[0].CHECK_EARLY;
    message = '';

    if (suspend.checked == true && suspend.value.length > 0) {
        message += '出席停止' + suspend.value + '日';
    }
    if (mourning.checked == true && mourning.value.length > 0) {
        if (message.length > 0) message += '、';
        message += '忌引' + mourning.value + '日';
    }
    if (sick.checked == true && sick.value.length > 0) {
        if (message.length > 0) message += '、';
        message += '欠席' + sick.value + '日';
    }
    if (late.checked == true && late.value.length > 0) {
        if (message.length > 0) message += '、';
        message += '遅刻' + late.value + '回';
    }
    if (early.checked == true && early.value.length > 0) {
        if (message.length > 0) message += '、';
        message += '早退' + early.value + '回';
    }

    textRange = null;
    parent.document.forms[0][target].focus();
    var textarea = parent.document.forms[0][target];

    //IE11未満のとき
    if (document.selection) {
        textRange = document.selection.createRange();
        textRange.text = message;
    } else {
        var sentence = textarea.value;
        var len      = sentence.length;
        var pos      = textarea.selectionStart;

        var before   = sentence.substr(0, pos);
        var word     = message;
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
