function btn_submit(cmd) {

    if (cmd == 'copy' && !confirm('{rval MSG101}')){
        return false;
    }
    if (cmd == 'fromcopy') {
        if (document.forms[0].PROFICIENCYCD.value == "") {
            alert('{rval MSG304}');
            return false;
        }
        if (document.forms[0].PROFICIENCYCD_FROMCOPY.value == "") {
            alert('{rval MSG304}' + '\nコピー元が選択されていません。');
            return false;
        }
        if (!confirm('{rval MSG101}' + '\nデータが既に登録されている場合はコピーできません。')){
            return false;
        }
    }
    if (cmd == 'list_gakki') {
        cmd = 'list';
        document.forms[0].TEST.value = "";
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
