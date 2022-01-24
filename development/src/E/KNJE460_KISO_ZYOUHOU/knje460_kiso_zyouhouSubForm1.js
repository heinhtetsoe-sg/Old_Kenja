// Add by PP for loading focus 2020-02-03 start
setTimeout(function () {
     window.onload = new function () {
        // start loading focus
         document.getElementById('screen_id').focus();
         setTimeout(function () {
            document.title = TITLE; 
         }, 1000);
    }
}, 100);
function current_cursor(para) {
    sessionStorage.setItem("KNJE460KisoZyouhouSubForm1_CurrentCursor", para);
}
// Add by PP loading focus 2020-02-20 end
//サブミット
function btn_submit(cmd){
    if (cmd == 'subform1_read') {
        if (document.forms[0].KISO_ZYOUHOU.value != "") {
            if (!confirm('データがあります。上書きしますか？')) {
                return false;
            }
        }
    } 
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//取込処理
function dataPositionSet (target, mainMsg) {

    //対象項目
    textRange = null;
    parent.document.forms[0][target].focus();
    var textarea = parent.document.forms[0][target];

    //チェック
    if (textarea.value != '') {
        if (!confirm('データがあります。上書きしますか？')) {
            return false;
        }
    }
    textarea.value = '';

    //テキストの作成
    mainMsg = '';
    line = '\r\n';    //改行
    for (var idx = 1; idx <= document.forms[0].SELECT_COUNT.value; idx++) {
        var item = document.forms[0]['SPRT_SEQ'+idx];
        tmpData = document.forms[0]['BASE_TITLE'+idx].value;

        if (item.checked == true && tmpData.length > 0) {
            mainMsg += "（" + tmpData + "）" + line + line;
        }
    }

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