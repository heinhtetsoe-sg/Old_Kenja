function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function selectOutputTyp(radio) {
    if (radio == null) {
        return;
    }
    var radios = document.getElementsByName(radio.name);
    for (var idx = 0; radios != null && idx < radios.length; idx++) {
        var typNo = idx + 1;
        var subRadios = document.getElementsByName(
            "OUTPUT_TYP" + typNo + "_SUB"
        );
        for (
            var idxSub = 0;
            subRadios != null && idxSub < subRadios.length;
            idxSub++
        ) {
            subRadios[idxSub].disabled =
                subRadios[idxSub].name.indexOf(radio.id) < 0;
        }
    }
}

//印刷
function newwin(SERVLET_URL) {
    action = document.forms[0].action;
    target = document.forms[0].target;

    //url = location.hostname;
    //document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL + "/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
