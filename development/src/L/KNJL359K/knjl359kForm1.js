function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//印刷
function newwin(SERVLET_URL){

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
//一般・附属ラジオのＯＮ／ＯＦＦ
function dis_fzkflg(output_val){

    //NO001
    if (output_val == 1) {
        document.forms[0].SHDIV[0].disabled = false;
        document.forms[0].SHDIV[1].disabled = false;
        document.forms[0].SHDIV[2].disabled = false;
    }
    if (output_val == 2) {
        document.forms[0].SHDIV[0].disabled = true;
        document.forms[0].SHDIV[1].disabled = true;
        document.forms[0].SHDIV[2].disabled = true;
    }
/*** NO001
    if (document.forms[0].TESTDIV.value == "2") return;//前期のみ

    for (var i = 0; i < 2; i++) {
        if (output_val == 1) {
            document.forms[0].FZKFLG1[i].disabled = false;
            document.forms[0].FZKFLG2[i].disabled = true;
            document.forms[0].FZKFLG3[i].disabled = true;
            document.forms[0].FZKFLG4[i].disabled = true;
        }
        if (output_val == 2) {
            document.forms[0].FZKFLG1[i].disabled = true;
            document.forms[0].FZKFLG2[i].disabled = false;
            document.forms[0].FZKFLG3[i].disabled = true;
            document.forms[0].FZKFLG4[i].disabled = true;
        }
        if (output_val == 3) {
            document.forms[0].FZKFLG1[i].disabled = true;
            document.forms[0].FZKFLG2[i].disabled = true;
            document.forms[0].FZKFLG3[i].disabled = false;
            document.forms[0].FZKFLG4[i].disabled = true;
        }
        if (output_val == 4) {
            document.forms[0].FZKFLG1[i].disabled = true;
            document.forms[0].FZKFLG2[i].disabled = true;
            document.forms[0].FZKFLG3[i].disabled = true;
            document.forms[0].FZKFLG4[i].disabled = false;
        }
    }
***/

}
