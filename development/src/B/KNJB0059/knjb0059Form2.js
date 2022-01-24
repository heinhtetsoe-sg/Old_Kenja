function btn_submit(cmd) {

    if (cmd == "update") {
        if (document.forms[0].PATTERN_CD.value == ""){
            alert('{rval MSG301}\n　　（履修パターンコード）');
            return true;
        }
        if (document.forms[0].PATTERN_NAME.value == ""){
            alert('{rval MSG301}\n　　（履修パターン名称）');
            return true;
        }
    }

    if (cmd == "delete") {
        result = confirm('{rval MSG103}');
        if (result == false) {
            return false;
        }
    }
    if (cmd == "reset") {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


function credit_keisan(CheckVal, credits) {
    //単位数NULLの場合は0をセット
    if (credits == "") {
        credits = 0;
    }

    //単位計NULLの場合は0をセット
    if (document.forms[0].CREDITS_SUM.value == "") {
        credits_sum = 0;
    } else {
        credits_sum = parseInt(document.forms[0].CREDITS_SUM.value, 10);
    }

    var PatternCheckFlg = CheckVal.checked;
    if (PatternCheckFlg) {
        document.forms[0].CREDITS_SUM.value = credits_sum + parseInt(credits, 10);
    } else {
        document.forms[0].CREDITS_SUM.value = credits_sum - parseInt(credits, 10);
    }
    document.getElementById("CREDITS_SUM_ID").innerHTML = document.forms[0].CREDITS_SUM.value;
}
