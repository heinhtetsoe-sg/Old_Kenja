function btn_submit(cmd)
{
    if (cmd == 'update' && document.forms[0].STARTNUMBER.value == ""){
        alert('{rval MSG301}' + '\n（開始番号）');
        return true;
    }

   if (cmd == 'update' || cmd == 'clear') {
       if (!confirm('{rval MSG101}')) {
           return false;
       }
   }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
