function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function closing_window(cd){
  if(cd){
      alert('{rval MSG305}');
  }else{
     alert('{rval MSG300}');
 }
    closeWin();
}

function ClearList(OptionList, TitleName)
{
    OptionList.length = 0;
}
