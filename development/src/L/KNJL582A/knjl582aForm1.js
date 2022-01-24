function btn_submit(cmd) {

    //ページ切り替え
    if ((cmd == "back" || cmd == "next") && document.forms[0].HID_EXAMNO.value.length != 0) {
        if(!confirm('{rval MSG106}')) return false;
    }

    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_EXAMNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if (document.forms[0].TESTDIV.disabled) {
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

//Enterキーで移動
function keyChangeEntToTab(obj) {
    if (window.event.keyCode == '13') {
        var setArr = document.forms[0].HID_EXAMNO.value.split(',');
        var tmp = obj.id.split('_');
        var tmpArr = new Array();
        for (var i = 0; i < setArr.length; i++) {
            tmpArr[i] = tmp[0]+'_'+setArr[i];
        }
        var index = tmpArr.indexOf(obj.id);

        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
            var targetId = tmpArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i > 0; i--) {
                    targetId = tmpArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        } else {
            if (index < (tmpArr.length - 1)) {
                index++;
            }
            var targetId = tmpArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var i = index; i < (tmpArr.length - 1); i++) {
                    targetId = tmpArr[i];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        }

        document.getElementById(targetId).focus();
        document.getElementById(targetId).select();
        return false;
    }
}

function procedureChk(div,examno) {
    if (div == '2') {
        if (document.forms[0]['PROCEDUREDIV_'+examno].checked == true) {
            document.forms[0]['PROCEDUREDIV_'+examno].checked == true;
            document.forms[0]['REMARK1_'+examno].disabled == false;
            document.forms[0]['REMARK1_'+examno].checked == true;
            document.forms[0]['REMARK1_'+examno].disabled == true;
            document.forms[0]['HID_REMARK1_'+examno].value = '1';

            cmd = 'reload';
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }
}