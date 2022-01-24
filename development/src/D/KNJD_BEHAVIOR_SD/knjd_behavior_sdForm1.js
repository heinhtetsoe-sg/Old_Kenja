/* Edit by HPA for current_cursor start 2020/02/03  */
window.onload = new function () {
  if (sessionStorage.getItem("KNJD_behavior_sdForm1_CurrentCursor") != null) {
    document.title = "";
    setTimeout(function () {
      document.getElementById(sessionStorage.getItem("KNJD_behavior_sdForm1_CurrentCursor")).focus();
      sessionStorage.removeItem("KNJD_behavior_sdForm1_CurrentCursor");
      document.title = title;
    }, 500);
  } else {
    setTimeout(function () {
      document.getElementById('screen_id').focus();
      document.title = title;
    }, 500);
  }
}
function current_cursor(para) {
  sessionStorage.setItem("KNJD_behavior_sdForm1_CurrentCursor", para);
}

function current_cursor_focus() {
  document.getElementById(sessionStorage.getItem("KNJD_behavior_sdForm1_CurrentCursor")).focus();
}
/* Edit by HPA for current_cursor end 2020/02/20 */
//サブミット
function btn_submit(cmd) {

/* Edit by HPA for PC-talker 読み start 2020/02/03 */
  document.title = "";
  document.getElementById(sessionStorage.getItem("KNJD_behavior_sdForm1_CurrentCursor")).blur();
  /* Edit by HPA for PC-talker 読み end 2020/02/20 */
    if (cmd == 'clear'){
        if (!confirm('{rval MSG106}')){
            return false;
        }
    }

    //更新中の画面ロック(全フレーム)
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLocks();
            parent.parent.left_frame.updateFrameLock();
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//入力チェック
function calc(obj) {
    var str = obj.value;
    //空欄
    if (str == '') {
        return;
    }
    var findMoji = document.forms[0].CHECK_VAL.value;
    //学校生活のようす
    if (!str.match(eval(findMoji))) {
        alert('{rval MSG901}\n' + document.forms[0].CHECK_ERR_MSG.value + '\nを入力して下さい。');
        obj.focus();
        return;
    }
}

//データ挿入用オブジェクトを入れる
var setObj;

function kirikae2(obj, showName) {
    event.cancelBubble = true
    event.returnValue = false;
    clickList(obj, showName);
}

function clickList(obj, showName) {

    setObj = obj;
    myObj = document.forms[0].all["myID_Menu"].style;
    myObj.left = window.event.clientX + document.body.scrollLeft + "px";
    myObj.top  = window.event.clientY + document.body.scrollTop + "px";
    myObj.visibility = "visible";
}

function myHidden() {
    document.all["myID_Menu"].style.visibility = "hidden";
}

function setClickValue(val) {
    if (val != '999') {
        setObj.value = val;
    }
    myHidden();
    setObj.focus();
}

