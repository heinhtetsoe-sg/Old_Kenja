function btn_submit(cmd) {
    if(cmd == "update") {
        var lowInps = document.getElementsByName("LOWER_LIMIT[]");
        for(var idx =0; lowInps != null && idx < lowInps.length; idx++) {
            if(validateNumInput(lowInps[idx]) == false) {
                lowInps[idx].focus();
                lowInps[idx].select();
                return false;
            }
        }

        for(var idx =0; lowInps != null && idx < lowInps.length; idx++) {
            var upperScore = new Number(lowInps[idx].value);
            for(var lwrIdx = idx + 1; lowInps != null && lwrIdx < lowInps.length; lwrIdx++) {
                var targetScore = new Number(lowInps[lwrIdx].value);
                if (targetScore > upperScore) {
                    alert('{rval MSG914}');
                    lowInps[lwrIdx].focus();
                    lowInps[lwrIdx].select();
                    return false;
                }
                if (targetScore.toString() == upperScore.toString()) {
                    alert('{rval MSG302}');
                    lowInps[lwrIdx].focus();
                    lowInps[lwrIdx].select();
                    return false;
                }
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].target = "_self";
    document.forms[0].action = "knjh562aindex.php";
    document.forms[0].submit();
    return false;
}

function validateNumInput(txtbox) {
    if(txtbox == null) {
        alert("引数エラー");
        return false;
    }

    var before = txtbox.value;
    txtbox.value = toInteger(txtbox.value);
    if(before != txtbox.value) {
        //メッセージは「toInteger」にて出力されている
        return false;
    }

    if(txtbox.value.length < 1) {
        alert('{rval MSG301}' + "\n( 下限値 )");
        return false;
    }

    var val = Number(txtbox.value);
    var min = Number(txtbox.getAttribute("min"));
    var max = Number(txtbox.getAttribute("max"));
    
    if(val < min || max < val) {
        alert('{rval MSG914}' + "\n最小：" + min + " ～ 最大：" + max);
        return false;
    }

    return true;
}
