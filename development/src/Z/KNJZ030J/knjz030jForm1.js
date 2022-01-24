function btn_submit(cmd) {

    if (cmd == 'copy') {
        var value = eval(document.forms[0].year.value) + 1;
        var message = document.forms[0].year.value + '年度のデータから、' + value + '年度に存在しないデータのみコピーします。\n\n注意：'+ value + '年度の課程、学科マスタを事前に作成しておいて下さい。';
        if (!confirm('{rval MSG101}\n\n' + message)) {
            return false;
        }
    }

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
