function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//画面の切替
function Page_jumper(link) {
    if (document.forms[0].OUTPUT_DIV[0].checked) {
        prg  = "KNJL351W";
    } else if (document.forms[0].OUTPUT_DIV[1].checked) {
        prg  = "KNJL351W_1";
    } else if (document.forms[0].OUTPUT_DIV[2].checked) {
        prg  = "KNJL351W_2";
    }

    link = link + "/L/" + prg + "/" + prg.toLowerCase() + "index.php";
    parent.location.href=link;
}

