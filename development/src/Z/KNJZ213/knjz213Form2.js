function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'update'){
        var total = 0;
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            if (e.type == "text" && e.name == "WEIGHTING[]"){
                var val = e.value;
                if (val != "") {
                    total += parseInt(val);
                }
            }
        }
        if (total != 100) {
            alert('重みは、合計で100です。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
