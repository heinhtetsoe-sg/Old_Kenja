function btn_submit(cmd) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear1') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function tutihyo_yomikomi() {
    var yomikomi = document.forms[0].YOMIKOMI.value;

    var inputs = document.getElementsByTagName("input");

    for (i = 0; i < inputs.length; i++) {
        if (inputs[i].getAttribute("type") == "checkbox") {
            inputs[i].checked = false;
        }
    }

    if (yomikomi) {
        yomikomiArray = yomikomi.split(":");
        for(i = 0; i < yomikomiArray.length; i++) {
            eval("document.forms[0]." + yomikomiArray[i] + ".checked=true");
        }
    }
}
