<!--kanji=漢字-->
<!-- <?php # $RCSfile: knje320Form1.js,v $ ?> -->
<!-- <?php # $Revision: 56587 $ ?> -->
<!-- <?php # $Date: 2017-10-22 21:54:51 +0900 (日, 22 10 2017) $ ?> -->

function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function newwin(SERVLET_URL) {
    action = document.forms[0].action;
    target = document.forms[0].target;

//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJE";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
