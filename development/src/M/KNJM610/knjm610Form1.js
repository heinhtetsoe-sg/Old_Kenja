function btn_submit(cmd) {
//  if (cmd == 'delete' || cmd == 'update'){
//      if (document.forms[0].YEAR.value == '') alert('年度を指定して下さい');
//      return false;
//  }
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
