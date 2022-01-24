function btn_submit(cmd) {
   if (document.forms[0].SCHREGNO.value == ""){
       alert('{rval MSG304}');
       return true;
   }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function endclose() {

    document.forms[0].cmd.value = 'main';
    document.forms[0].firstcnt.value = 0;
//    document.forms[0].num.value = document.forms[0].numkaku.value;
//    alert(document.forms[0].num.value+document.forms[0].numkaku.value);
//    document.forms[0].submit();
    closeWin();
}
