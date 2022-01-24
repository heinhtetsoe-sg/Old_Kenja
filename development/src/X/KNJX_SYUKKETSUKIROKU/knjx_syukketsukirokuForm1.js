/* Edit by HPA for PC-talker 読み start 2020/01/20 */
window.onload = function () {
    document.getElementById('screen_id').focus();
    document.title = title;
}
/* Edit by HPA for PC-talker 読み end 2020/01/31 */

//サブミット
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//disable
function OptionUse(obj, checkFlg, semesFlg) {

    var disabled = true;
    if (document.forms[0].CHECK_SUSPEND.checked  == false &&
        document.forms[0].CHECK_MOURNING.checked == false &&
        document.forms[0].CHECK_SICK.checked     == false &&
        document.forms[0].CHECK_LATE.checked     == false &&
        document.forms[0].CHECK_EARLY.checked    == false)
    {
        disabled = true;
    } else {
        disabled = false;
    }
    if (checkFlg == '1' || checkFlg == '3'){
        if (document.forms[0].CHECK_VIRUS.checked == true){
            disabled = false;
        }
    }
    if (checkFlg == '2' || checkFlg == '3'){
        if (document.forms[0].CHECK_KOUDOME.checked == true){
            disabled = false;
        }
    }
    
    if(semesFlg == '1'){
        if(disabled == false){
            disabled = true;
            for (var semes = 1; semes <= document.forms[0].MAX_SEMES.value; semes++) {
                if(document.forms[0]['CHECK_SEMES'+semes].checked == true){
                    disabled = false;
                }
            }
        }
    }
    document.forms[0].btn_torikomi.disabled = disabled ;
}

//取込処理
function dataPositionSet (target, checkFlg) {
    var suspend  = document.forms[0].CHECK_SUSPEND;
    if (checkFlg == '1' || checkFlg == '3'){
        var virus    = document.forms[0].CHECK_VIRUS;
    }
    if (checkFlg == '2' || checkFlg == '3'){
        var koudome  = document.forms[0].CHECK_KOUDOME;
    }
    var mourning = document.forms[0].CHECK_MOURNING;
    var sick     = document.forms[0].CHECK_SICK;
    var late     = document.forms[0].CHECK_LATE;
    var early    = document.forms[0].CHECK_EARLY;
    message = '';

    if (suspend.checked == true && suspend.value.length > 0) {
        message += '出席停止' + suspend.value + '日';
    }
    if (checkFlg == '1' || checkFlg == '3'){
        if (virus.checked == true && virus.value.length > 0) {
            if (message.length > 0) message += '、';
            message += document.forms[0].VIRUS_NAME.value + virus.value + '日';
        }
    }
    if (checkFlg == '2' || checkFlg == '3'){
        if (koudome.checked == true && koudome.value.length > 0) {
            if (message.length > 0) message += '、';
            message += document.forms[0].KOUDOME_NAME.value + koudome.value + '日';
        }
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

//取込処理(学期)
function dataPositionSetSemes (target, checkFlg) {
    var suspend  = 0;
    var mourning = 0;
    var sick     = 0;
    var late     = 0;
    var early    = 0;
    if (checkFlg == '1' || checkFlg == '3'){
        var virus   = 0;
    }
    if (checkFlg == '2' || checkFlg == '3'){
        var koudome = 0;
    }
    for (var semes = 1; semes <= document.forms[0].MAX_SEMES.value; semes++) {
        if(document.forms[0]['CHECK_SEMES'+semes].checked == true){
            suspend  = Number(suspend)  + Number(document.forms[0]['SUSPEND'+semes].value);
            mourning = Number(mourning) + Number(document.forms[0]['MOURNING'+semes].value);
            sick     = Number(sick)     + Number(document.forms[0]['SICK'+semes].value);
            late     = Number(late)     + Number(document.forms[0]['LATE'+semes].value);
            early    = Number(early)    + Number(document.forms[0]['EARLY'+semes].value);
            if (checkFlg == '1' || checkFlg == '3'){
                virus = Number(virus) + Number(document.forms[0]['VIRUS'+semes].value);
            }
            if (checkFlg == '2' || checkFlg == '3'){
                koudome = Number(koudome) + Number(document.forms[0]['KOUDOME'+semes].value);
            }
        }
    }

    message = '';
    if (document.forms[0].CHECK_SUSPEND.checked == true) {
        message += '出席停止' + suspend + '日';
    }
    if (checkFlg == '1' || checkFlg == '3'){
        if (document.forms[0].CHECK_VIRUS.checked == true) {
            if (message.length > 0) message += '、';
            message += document.forms[0].VIRUS_NAME.value + virus + '日';
        }
    }
    if (checkFlg == '2' || checkFlg == '3'){
        if (document.forms[0].CHECK_KOUDOME.checked == true) {
            if (message.length > 0) message += '、';
            message += document.forms[0].KOUDOME_NAME.value + koudome + '日';
        }
    }
    if (document.forms[0].CHECK_MOURNING.checked == true) {
        if (message.length > 0) message += '、';
        message += '忌引' + mourning + '日';
    }
    if (document.forms[0].CHECK_SICK.checked == true) {
        if (message.length > 0) message += '、';
        message += '欠席' + sick + '日';
    }
    if (document.forms[0].CHECK_LATE.checked == true) {
        if (message.length > 0) message += '、';
        message += '遅刻' + late + '回';
    }
    if (document.forms[0].CHECK_EARLY.checked == true) {
        if (message.length > 0) message += '、';
        message += '早退' + early + '回';
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
