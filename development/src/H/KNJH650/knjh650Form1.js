function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function nextFocus(nextFocus) {
    if (event.keyCode == 13) {
        if (document.forms[0]["NEXT_FOCUS" + nextFocus] !== undefined) {
            nextText = document.forms[0]["NEXT_FOCUS" + nextFocus].value;
            if (document.forms[0][nextText] !== undefined) {
                document.forms[0][nextText].focus();
            }
        }
    }
}

function checkPerfect(obj, perfect) {
    if (parseInt(obj.value) > parseInt(perfect)) {
        alert(perfect + '点を越えています。');
        obj.focus();
    }
}

function chengeChecked(obj, headName, subclassCd) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var checkObj = document.forms[0].elements[i];
        re = new RegExp("^" + headName );
        if (checkObj.name.match(re)) {
            if (obj.checked) {
                if (checkObj.name != obj.name) {
                    checkObj.checked = false;
                }
            }
        }
    }
}
