function btn_submit(cmd) {
    //必須チェック
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }
    //取消
    if (cmd == 'subform1_clear'){
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    /*if (cmd == 'subform2'){
        if (!confirm('{rval MSG108}')) {
            return false;
        }
    }*/

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//disabled
function OptionUse(obj) {
    var flg;
    if (obj.checked == true) {
        flg = false;
	} else {
        flg = true;
    }

    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "01_REMARK2") {
            document.forms[0].elements[i].disabled = flg;
        }
    }
}
function btn_reset() {
    //ポートフォリオ画面が動かなくなるのを回避
    window.opener.document.forms[0].cmd.value = 'radio';
    window.opener.document.forms[0].submit();
    closeWin();
    
}
