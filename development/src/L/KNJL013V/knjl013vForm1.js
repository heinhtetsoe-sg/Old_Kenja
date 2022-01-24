function btn_submit(cmd) {
    if (cmd == "reset") {
        if (!confirm("{rval MSG106}")) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function chagenCheck(counter) {
    var cnt = 0;
    for (var i = 1; i <= 10; i++) {
        if (document.getElementsByName("SPECIAL_ACT" + i + "-" + counter)[0].checked) {
            cnt++;
        }
    }
    document.getElementsByName("ACT_TOTAL" + "-" + counter)[0].value = cnt;
}

function kamokuChange(idx, counter) {
    var cnt = 0;
    if (document.forms[0].TOTAL3.value != "") {
        var list = document.forms[0].TOTAL3.value.split(",");
        for (var i = 0; i < list.length; i++) {
            var val = parseInt(document.getElementsByName(list[i] + idx + "-" + counter)[0].value);
            if (!isNaN(val)) {
                cnt += val;
            }
        }
    }
    document.getElementsByName("TOTAL3" + idx + "-" + counter)[0].value = cnt;

    var cnt = 0;
    if (document.forms[0].TOTAL3.value != "") {
        var list = document.forms[0].TOTAL5.value.split(",");
        for (var i = 0; i < list.length; i++) {
            var val = parseInt(document.getElementsByName(list[i] + idx + "-" + counter)[0].value);
            if (!isNaN(val)) {
                cnt += val;
            }
        }
    }
    document.getElementsByName("TOTAL5" + idx + "-" + counter)[0].value = cnt;

    var cnt = 0;
    if (document.forms[0].TOTAL9.value != "") {
        var list = document.forms[0].TOTAL9.value.split(",");
        for (var i = 0; i < list.length; i++) {
            var val = parseInt(document.getElementsByName(list[i] + idx + "-" + counter)[0].value);
            if (!isNaN(val)) {
                cnt += val;
            }
        }
    }
    document.getElementsByName("TOTAL9" + idx + "-" + counter)[0].value = cnt;
}
function Page_jumper(link) {
    if (!confirm("{rval MSG108}")) {
        return;
    }
    parent.location.href = link;
}
window.onload = function () {
    chagenCheck();
    kamokuChange(1);
    kamokuChange(2);
    kamokuChange(3);
};
