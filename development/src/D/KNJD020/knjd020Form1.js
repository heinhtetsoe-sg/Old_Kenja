function getGtreData(gtredata){
   document.forms[0].GTREDATA.value = gtredata;
   document.forms[0].cmd.value = 'main';
   document.forms[0].submit();
   return false;
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}