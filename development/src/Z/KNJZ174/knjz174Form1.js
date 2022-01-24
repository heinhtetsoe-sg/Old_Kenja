function btn_submit(cmd) {
    if (cmd=="clear") {
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        } 
    }

    if (cmd == "copy" ) {
        if (document.forms[0].COPY_KEY.value == "") {
            alert('コピー対象データを選択して下さい。');
            return false;
        }
        if (document.forms[0].MONTHCD.value == "") {
            alert('対象月を選択して下さい。');
            return false;
        }
        if (document.forms[0].COPY_KEY.value == document.forms[0].CT_YEAR.value + "-" + document.forms[0].MONTHCD.value) {
            alert('同一データのコピーは出来ません。');
            return false;
        }
        if (document.forms[0].lessonFlg.value) {
            if (!confirm('データが存在しますが、上書きしても宜しいですか？')) {
                return false;
            } 
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function ShowConfirm(){
    result = confirm('{rval MSG107}');
    if (result == false) {
        return false;
    }
}

function closing_window(){
    alert('{rval MSG300}');
    closeWin();
    return true;
}
function allCheck(obj)
{
    for (var i=0; i < document.forms[0].elements.length; i++) {
        re = new RegExp("^AUTO_CHECK" );
        if (document.forms[0].elements[i].name.match(re)) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
    return false;
}
