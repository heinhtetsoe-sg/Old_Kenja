function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL){

    if (document.forms[0].TESTDIV.value == ""){
        alert("入試区分を指定して下さい");
        return;
    }
    action = document.forms[0].action;
    target = document.forms[0].target;

//  url = location.hostname;
//  document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
function chkOutputDiv() {
    var outputDivDisabled = document.getElementById("OUTPUT_KIND4").checked;
    document.forms[0].OUTPUT_DIV1.disabled = outputDivDisabled;
    document.forms[0].OUTPUT_DIV2.disabled = outputDivDisabled;
    document.forms[0].OUTPUT_DIV3.disabled = outputDivDisabled;
    document.forms[0].OUTPUT_DIV4.disabled = outputDivDisabled;
    
    if (document.getElementById("OUTPUT_KIND1").checked) {
        document.forms[0].OUTPUT_DIV4.disabled = false;
    } else {
        document.forms[0].OUTPUT_DIV4.disabled = true;
    }
}
