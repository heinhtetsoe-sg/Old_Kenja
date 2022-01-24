function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        if (document.forms[0].CHANGE_FLG.value == '1') {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
            return false;
        }
        closeWin();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function changeFlg(obj) {
    document.forms[0].CHANGE_FLG.value = '1';

    document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
    document.forms[0].HID_TESTDIV.value = document.forms[0].TESTDIV.options[document.forms[0].TESTDIV.selectedIndex].value;

    document.forms[0].APPLICANTDIV.disabled = true;
    document.forms[0].TESTDIV.disabled = true;
}

//一括チェック
function check_all(obj) {
    var examnoArray = document.forms[0].HID_EXAMNO.value.split(',');
    for (var i = 0; i < examnoArray.length; i++) {
        //一次手続き
        if (obj.name == 'ALLCHECK_BD022_REMARK1') {
            tergetObject = document.getElementById('BD022_REMARK1' + '-' + examnoArray[i]);
            if (tergetObject) {
                tergetObject.checked = obj.checked;
            }
            //二次手続き
        } else if (obj.name == 'ALLCHECK_PROCEDUREDIV') {
            tergetObject = document.getElementById('PROCEDUREDIV' + '-' + examnoArray[i]);
            if (tergetObject) {
                tergetObject.checked = obj.checked;
            }
            //招集日
        } else if (obj.name == 'ALLCHECK_BD022_REMARK3') {
            tergetObject = document.getElementById('BD022_REMARK3' + '-' + examnoArray[i]);
            if (tergetObject) {
                tergetObject.checked = obj.checked;
            }
        }
    }
}

function clearDate(obj) {
    var objName = obj.name.split('-')[0];
    var examno = obj.name.split('-')[1];

    //一次手続きをoffにしたら、一次手続き日は空白とする。
    if (obj.checked == false && objName == 'BD022_REMARK1') {
        tergetObject = eval('document.forms[0]["' + 'BD022_REMARK2' + '-' + examno + '"]');
        if (tergetObject) {
            tergetObject.value = '';
        }
    }
    //二次手続きをoffにしたら、二次手続き日は空白とする。
    //二次手続きをoffにしたら、入辞区分=1の時、入辞区分は空白とする。
    if (obj.checked == false && objName == 'PROCEDUREDIV') {
        tergetObject = eval('document.forms[0]["' + 'PROCEDUREDATE' + '-' + examno + '"]');
        if (tergetObject) {
            tergetObject.value = '';
        }
        tergetObject = eval('document.forms[0]["' + 'ENTDIV' + '-' + examno + '"]');
        if (tergetObject) {
            if (tergetObject.value == '1') {
                tergetObject.value = '';
            }
        }
    }
}
