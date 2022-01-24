function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }

    //更新
    var bChkDataExistFlg = false;
    if ((cmd == "update" || cmd == "numbering")) {
        if (document.forms[0].HID_EXAMNO.value.length == 0) {
            alert('{rval MSG303}');
            return false;
        }

        var tmpArr = new Array();
        var setArr = document.forms[0].HID_EXAMNO.value.split(',');
        for (var cnt = 0; cnt < setArr.length; cnt++) {
            tmpArr[cnt] = 'SCHREGNO_'+setArr[cnt];
        }
        for (var cnt = 0; cnt < setArr.length; cnt++) {
            var chkBaseObj = document.getElementById(tmpArr[cnt]);
            if (typeof chkBaseObj == 'object' && chkBaseObj.value != "") {
                bChkDataExistFlg = true;
                for (var cmt = cnt + 1;cmt < setArr.length; cmt++) {
                    var chkObj = document.getElementById(tmpArr[cmt]);
                    if (typeof chkObj == 'object' && chkObj != null && chkObj.value != "") {
                        if (chkBaseObj.value == chkObj.value) {
                            alert('{rval MSG302}');
                            return false;
                        }
                    }
                }
            }
        }

        if (cmd == 'numbering') {
            if (bChkDataExistFlg == true) {
                if (!confirm('{rval MSG104}')) {
                    return true;
                }
            }
            var setArr = document.forms[0].HID_EXAMNO.value.split(',');
            var sCdVal = "";
            if (document.forms[0].SCHOOLKIND.value == 'H') {
                sCdVal = "3";
            } else {
                sCdVal = "2";
            }
            var tmpArr = new Array();
            var setArr = document.forms[0].HID_EXAMNO.value.split(',');
            for (var cnt = 0; cnt < setArr.length; cnt++) {
                tmpArr[cnt] = 'SCHREGNO_'+setArr[cnt];
            }
            for (var cnt = 0; cnt < setArr.length; cnt++) {
                var chgBaseObj = document.getElementById(tmpArr[cnt]);
                chgBaseObj.value = document.forms[0].YEAR.value + sCdVal + ('000' + (cnt + 1)).slice(-3);
            }
        }
    }

    //終了
    if (cmd == 'end') {
        if ((document.forms[0].APPLICANTDIV.disabled)) {
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
        for (var cnt = 0; cnt < setArr.length; cnt++) {
            tmpArr[cnt] = tmp[0]+'_'+setArr[cnt];
        }
        var index = tmpArr.indexOf(obj.id);

        if (window.event.shiftKey) {
            if (index > 0) {
                index--;
            }
            var targetId = tmpArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var cnt = index; cnt > 0; cnt--) {
                    targetId = tmpArr[cnt];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        } else {
            if (index < (tmpArr.length - 1)) {
                index++;
            }
            var targetId = tmpArr[index];
            if (document.getElementById(targetId).disabled == true) {
                for (var cnt = index; cnt < (tmpArr.length - 1); cnt++) {
                    targetId = tmpArr[cnt];
                    if (document.getElementById(targetId).disabled == false) break;
                }
            }
        }

        document.getElementById(targetId).focus();
        document.getElementById(targetId).select();
        return false;
    }
}
