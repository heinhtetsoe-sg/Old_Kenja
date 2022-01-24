function btn_submit(cmd) {
  if (cmd == "exec") {
    if (!confirm("処理を開始します。よろしいでしょうか？")) {
      return;
    }
  }
  document.forms[0].cmd.value = cmd;
  document.forms[0].submit();
  return false;
}
