function btn_submit(cmd) {
  document.forms[0].cmd.value = cmd;
  document.forms[0].submit();
  return false;
}

function newwin(SERVLET_URL) {
  //受験校種
  if (document.forms[0].APPLICANTDIV.value == "") {
    alert("{rval MSG310}\n( 受験校種 )");
    return;
  }
  //入試区分
  if (document.forms[0].TESTDIV.value == "") {
    alert("{rval MSG310}\n( 入試区分 )");
    return;
  }
  //専併区分
  if (document.forms[0].SHDIV.value == "") {
    alert("{rval MSG310}\n( 専併区分 )");
    return;
  }
  //入学コース
  if (document.forms[0].ENTER_COURSE.value == "") {
    alert("{rval MSG310}\n( 入学コース )");
    return;
  }
  //合格コース
  if (document.forms[0].PASS_COURSE.value == "") {
    alert("{rval MSG310}\n( 合格コース )");
    return;
  }

  action = document.forms[0].action;
  target = document.forms[0].target;

  //    url = location.hostname;
  //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
  document.forms[0].action = SERVLET_URL + "/KNJL";
  document.forms[0].target = "_blank";
  document.forms[0].submit();

  document.forms[0].action = action;
  document.forms[0].target = target;
}

function OnAuthError() {
  alert("{rval MSG300}");
  closeWin();
}
