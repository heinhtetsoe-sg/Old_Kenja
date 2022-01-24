function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return false;

    //更新
    if (cmd == "update" && document.forms[0].HID_EXAMNO.value.length == 0) {
        return false;
    }

    //終了
    if (cmd == 'end') {
        if (!confirm('{rval MSG108}')) {
            return false;
        }
        closeWin();
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_check(obj) {
    var objName = obj.name;
    var examno = objName.replace(/JUDGE_KIND_[0-9]-/, '') ;
    var arrJudgeKindCd = document.forms[0].HID_JUDGEKINDCD.value.split(',');
    for (var i = 1; i <= arrJudgeKindCd.length; i++) {
        var targetObjName = 'JUDGE_KIND_' + i + '-' + examno;
        if (targetObjName != objName) {
            document.forms[0].elements[targetObjName].checked = false;
        }
    }
}
