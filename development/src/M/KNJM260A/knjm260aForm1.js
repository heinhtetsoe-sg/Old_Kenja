//漢字
function btn_submit(cmd) {

    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    if (cmd == 'update'){
        checkVal = document.forms[0].CHECK_MAX.value;
        var subclassArray = checkVal.split(",");
        errFlg = false;
        subclassArray.forEach( function(value) {
            var subValArray = value.split(":");
            var r1Val = document.forms[0]["R_VAL1_" + subValArray[0]].value * 1;
            var r2Val = document.forms[0]["R_VAL2_" + subValArray[0]].value * 1;
            var r3Val = document.forms[0]["R_VAL3_" + subValArray[0]].value * 1;
            var totalRval = r1Val + r2Val + r3Val;
            if (subValArray[1] != totalRval) {
                alert("【" + subValArray[2] + "】" + "\nレポート回数(" + subValArray[1] + ")と、提出回数(" + totalRval + ")が不一致です。");
                errFlg = true;
            }
        })
        if (errFlg) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}
