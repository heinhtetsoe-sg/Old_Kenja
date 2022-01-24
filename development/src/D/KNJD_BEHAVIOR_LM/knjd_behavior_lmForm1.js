//サブミット
function btn_submit(cmd) {

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
    if (event.preventDefault) {
        event.preventDefault();
    }
    event.cancelBubble = true
    event.returnValue = false;
    clickList(obj, showName);
}

function clickList(obj, showName) {

    setObj = obj;
    myObj = document.getElementById("myID_Menu").style;
    myObj.left = window.event.clientX + document.body.scrollLeft + "px";
    myObj.top  = window.event.clientY + document.body.scrollTop + "px";
    myObj.visibility = "visible";
}

function myHidden() {
    document.getElementById("myID_Menu").style.visibility = "hidden";
}

function setClickValue(val) {
    if (val != '999') {
        setObj.value = val;
    }
    myHidden();
    setObj.focus();
}

