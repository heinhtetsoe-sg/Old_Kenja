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

function setSchoolcd(){
    var schoolcd = document.forms[0].SCHOOLCD.value;
    if (schoolcd != "") {
        var result = ('0000000' + schoolcd).slice(-7);
    } else {
        var result = schoolcd;
    }
    return result;
}