function btn_submit(cmd) {

  if (cmd == 'change' || cmd == 'changeTestDiv') {
    document.forms[0].RECEPTNO.value = '';
  }

  if (cmd == 'reset' && !confirm('{rval MSG106}')) {
    return true;
  }

  if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
    if (document.forms[0].RECEPTNO.value == '') {
        alert('{rval MSG301}\n( 受験番号 )');
        return true;
    }
  }

  document.forms[0].cmd.value = cmd;
  document.forms[0].submit();
  return false;
}

//ボタンを押し不可にする
function btn_disabled() {
  document.forms[0].btn_udpate.disabled = true;
  document.forms[0].btn_up_pre.disabled = true;
  document.forms[0].btn_up_next.disabled = true;
}
