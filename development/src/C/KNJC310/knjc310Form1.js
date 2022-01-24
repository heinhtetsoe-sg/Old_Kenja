// 漢字

function btn_submit(cmd)
{
    
    if (cmd == "delete") {
        //チェックがあるかどうか
        var delChk = true;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var el = document.forms[0].elements[i];
            if (el.name == "DELCHK[]" && el.checked) {
                delChk = false;
            }
        }
        if (delChk) {
           alert('ひとつも選択されていません。');
            return false;
        }
    }
        
    if (cmd == "delete" || cmd == "deleteAll") {
        if (confirm('{rval MSG103}')) {
        } else {
            return;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//全件チェックボックスクリック
function check_chg(obj){
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var el = document.forms[0].elements[i];
        if (el.name == "DELCHK[]" && obj.checked) {
            el.checked = true;
        }
        if (el.name == "DELCHK[]" && obj.checked == false) {
            el.checked = false;
        }
    }
}

