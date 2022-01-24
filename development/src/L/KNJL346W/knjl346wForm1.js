function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//画面の切替
function Page_jumper(link) {
    if (document.forms[0].CSV_PRG[0].checked) {
        prg  = "KNJL341W";
    } else if (document.forms[0].CSV_PRG[1].checked) {
        prg  = "KNJL342W";
    } else if (document.forms[0].CSV_PRG[2].checked) {
        prg  = "KNJL343W";
    } else if (document.forms[0].CSV_PRG[3].checked) {
        prg  = "KNJL344W";
    } else if (document.forms[0].CSV_PRG[4].checked) {
        prg  = "KNJL345W";
    } else if (document.forms[0].CSV_PRG[5].checked) {
        prg  = "KNJL346W";
    }

    link = link + "/L/" + prg + "/" + prg.toLowerCase() + "index.php";
    parent.location.href=link;
}

//印刷
function newwin(SERVLET_URL){
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJL";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
