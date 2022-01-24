function btn_submit(cmd) {

    if (cmd == 'copy' && !confirm('{rval MSG101}')){
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function EnableBtns(){
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_del.disabled = true;
}

function ShowConfirm(){
    if (!confirm('{rval MSG106}'))
        return false;
}

function closing_window(cd){
  if(cd){
      alert('{rval MSG305}教科マスタが登録されていません。');
  }else{
     alert('{rval MSG300}');
 }
    closeWin();
}

function check_Val(that){
    that.value = toInteger(that.value);
    return false;
}
function doSubmit(cmd) {
    if (cmd == 'delete' && !confirm('{rval MSG103}')){
        return false;
    }
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    if (document.forms[0].L_SUBCLASSCD.length==0 && document.forms[0].R_SUBCLASSCD.length==0) {
        alert("データは存在していません。");
        return false;
    }
    for (var i = 0; i < document.forms[0].L_SUBCLASSCD.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].L_SUBCLASSCD.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
