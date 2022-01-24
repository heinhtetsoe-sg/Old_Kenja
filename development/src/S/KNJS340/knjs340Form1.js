/* Add by HPA for CurrentCursor 2020-01-10 start */
  window.onload = function () {
      if (sessionStorage.getItem("KNJS342Form1_CurrentCursor") != null) {
        document.title = "";
        document.getElementById(sessionStorage.getItem("KNJS342Form1_CurrentCursor")).focus();
      }
  }

function current_cursor(para) {
  sessionStorage.setItem("KNJS342Form1_CurrentCursor", para);

}
/* Add by HPA for CurrentCursor 2020-01-17 end */

function btn_submit(cmd) {

  /* Add by HPA for CurrentCursor 2020-01-10 start */
  if (sessionStorage.getItem("KNJS342Form1_CurrentCursor") != null) {
    document.title = "";
    document.getElementById(sessionStorage.getItem("KNJS342Form1_CurrentCursor")).blur();
  }
/* Add by HPA for CurrentCursor 2020-01-17 end */

  document.forms[0].cmd.value = cmd;
  document.forms[0].submit();
  return false;
} 

//印刷
function newwin(SERVLET_URL) {
  //対象月、対象日、月末日をセット
  var targetDay = parseInt(document.forms[0].TARGET_DAY.value);
  var setMonth = parseInt(document.forms[0].SETMONTH.value);
  var MonthLastDay = parseInt(document.forms[0].LASTDAY.value);
  //学期マスタの月と日をセット
  var Smonth = parseInt(document.forms[0].SMONTH.value);
  var Sday = parseInt(document.forms[0].SDAY.value);
  var Emonth = parseInt(document.forms[0].EMONTH.value);
  var Eday = parseInt(document.forms[0].EDAY.value);

  if (document.forms[0].GRADE_HR_CLASS.value == "") {
    alert('{rval MSG304}');
    return false;
  }
  if (document.forms[0].TARGET_MONTH.value == "") {
    alert('{rval MSG304}');
    return false;
  }
  if (document.forms[0].TARGET_DAY.value == "") {
    alert('{rval MSG301}');
    return false;
  }
  if (targetDay == "") {
    alert('{rval MSG301}');
    return false;
  }
  if ((setMonth == Smonth) && (targetDay < Sday)) {
    alert('{rval MSG901}' + '\n\n日付が学期をまたいでいます。開始日は' + Sday + '日です');
    return false;
  }
  if ((setMonth == Emonth) && (targetDay > Eday)) {
    alert('{rval MSG901}' + '\n\n日付が学期をまたいでいます。終了日は' + Eday + '日です');
    return false;
  }
  if (targetDay > MonthLastDay) {
    alert('{rval MSG901}');
    return false;
  }
  action = document.forms[0].action;
  target = document.forms[0].target;

  //    url = location.hostname;
  //    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
  document.forms[0].action = SERVLET_URL + "/KNJS";
  document.forms[0].target = "_blank";
  document.forms[0].submit();

  document.forms[0].action = action;
  document.forms[0].target = target;
}

