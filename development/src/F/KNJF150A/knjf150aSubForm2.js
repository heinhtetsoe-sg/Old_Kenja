function btn_submit(cmd) {
    //チェック
    if (cmd == 'update' || cmd == 'insert') {
        if (document.forms[0].SCHREGNO.value == '') {
            alert('{rval MSG304}');
            return true;
        } else if (document.forms[0].VISIT_DATE.value == '' || document.forms[0].VISIT_HOUR.value == '' || document.forms[0].VISIT_MINUTE.value == '') {
            alert('来室日時が入力されていません。\n　　　　（必須入力）');
            return true;
        }
    } else if (cmd == 'subform2_clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {
    alert('登録された内容が印刷されます。');

    action = document.forms[0].action;
    target = document.forms[0].target;

    // url = location.hostname;
    // document.forms[0].action = 'http://' + url + '/cgi-bin/printenv.pl';
    document.forms[0].action = SERVLET_URL + '/KNJF';
    document.forms[0].target = '_blank';
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//disabled
function OptionUse(obj, array_name, target) {
    var useText = document.forms[0][array_name].value.split(',');

    if (useText.length > 0) {
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            if (document.forms[0].elements[i].name == target) {
                for (var j = 0; j < useText.length; j++) {
                    if (obj.value == useText[j]) {
                        document.forms[0][target].disabled = false;
                        document.forms[0][target].style.backgroundColor = '';
                        break;
                    } else {
                        document.forms[0][target].disabled = true;
                        document.forms[0][target].style.backgroundColor = '#D3D3D3';
                    }
                }
            }
        }
    }
}

function OptionUse3(obj, array_name, target) {
    var aryName = document.forms[0][array_name + '_OPT'].value.split(',');

    if (document.forms[0][array_name + '_BLANK'].checked) {
        if (target != '') {
            document.forms[0][target].disabled = true;
            document.forms[0][target].style.backgroundColor = '#D3D3D3';
        }

        for (var i = 0; i < aryName.length; i++) {
            document.forms[0][array_name + '_' + aryName[i]].disabled = true;
        }
        return;
    } else {
        for (var i = 0; i < aryName.length; i++) {
            document.forms[0][array_name + '_' + aryName[i]].disabled = false;
        }
    }

    if (target != '') {
        var useTextVal = document.forms[0][array_name + '_USE_TEXT'].value;

        if (document.forms[0][array_name + '_' + useTextVal].checked) {
            document.forms[0][target].disabled = false;
            document.forms[0][target].style.backgroundColor = '';
        } else {
            document.forms[0][target].disabled = true;
            document.forms[0][target].style.backgroundColor = '#D3D3D3';
        }
    }
}

//カレンダーによる再読込
function calendarSubmit(cmd, submit) {
    document.forms[0].cmd.value = cmd;
    if (submit == 'on') {
        document.forms[0].submit();
        return false;
    }
}
