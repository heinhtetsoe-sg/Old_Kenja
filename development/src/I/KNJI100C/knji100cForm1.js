//KNJI100C_01コール
function Page_jumper(link, schoolKind) {
    link = link + "/I/KNJI100C_01/knji100c_01index.php?SUBSYSTEM=01&SEND_selectSchoolKind=" + schoolKind;

    parent.location.href=link;
}
