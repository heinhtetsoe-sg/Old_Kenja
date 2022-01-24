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
    } else if (cmd == 'subform1_clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkDecimal(obj) {
    var decimalValue = obj.value;
    var check_result = false;

    if (decimalValue != '') {
        //空じゃなければチェック
        if (decimalValue.match(/^[0-9]+(\.[0-9]+)?$/)) {
            check_result = true;
        }
    } else {
        check_result = true;
    }

    if (!check_result) {
        alert('数字を入力して下さい。');
        obj.value = '';
    }

    //正しい値ならtrueを返す
    return check_result;
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

    //テキストの後ろに文言表示
    if (obj.name == 'SINCE_WHEN') {
        var setTmp = document.getElementById(obj.name);
        if (obj.name == 'SINCE_WHEN' && obj.value == '4') {
            setTmp.innerHTML = '限目頃から';
        } else {
            setTmp.innerHTML = '';
        }
    }
}

//disabled
function OptionUse2(obj, target, val) {
    var useText = document.forms[0][target + '_OPT'].value.split(',');
    re = new RegExp('^' + target);

    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var el = document.forms[0].elements[i];
        if (el.name.match(re) || el.name == 'CONDITION12_TEXT') {
            if (el.name == 'CONDITION12_TEXT') {
                for (var j = 0; j < useText.length; j++) {
                    if (obj.value == '1' && document.forms[0][target + '_' + useText[j]].checked) {
                        el.disabled = false;
                        el.style.backgroundColor = '';
                        break;
                    } else {
                        el.disabled = true;
                        el.style.backgroundColor = '#D3D3D3';
                    }
                }
            } else {
                if (obj.value == '1') {
                    if (document.forms[0][target + '_BLANK'].checked && el.name != target + '_BLANK') {
                        el.disabled = true;
                    } else {
                        el.disabled = false;
                    }
                } else {
                    el.disabled = true;
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
