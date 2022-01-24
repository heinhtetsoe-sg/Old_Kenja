function btn_submit(cmd) {
    var title;
    if (cmd == 'teikei1' || cmd == 'teikei2' || cmd == 'teikei3') {
        title = "";
        if (cmd == "teikei1") {
            title = "学習";
        } else if (cmd == "teikei2") {
            title = "生活";
        } else {
            title = "性格";
        }
        loadwindow('knjd132cindex.php?cmd=' + cmd + '&TEIKEI_CMD=' + cmd + "&TITLE=" + title, event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 450);
        return true;
    }
    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //更新
    if (cmd == 'update') {
        if (!document.forms[0].SCHREGNO.value) {
            alert('{rval MSG304}');
            return;
        }
        if (!document.forms[0].SEMESTER.value) {
            alert('{rval MSG304}');
            return;
        }
    }

    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLocks();
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

    //行動の記録
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
    if (event.preventDefault) {
        myObj = document.getElementById('myID_Menu').style;
    } else {
        myObj = document.forms[0].all["myID_Menu"].style;
    }
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

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab2(obj, setTextField) {
    //移動可能なオブジェクト
    var textFieldArray = setTextField.split(",");
    // Ent13 Tab9 ←37 ↑38 →39 ↓40
    var e = window.event;
    if (e.keyCode != 13) {
        return;
    }
    var moveEnt = 40;
    var i;
    for (i = 0; i < textFieldArray.length; i++) {
        if (textFieldArray[i] == obj.name) {
            if (textFieldArray.length - 1 == i) {
                if (e.preventDefault) {
                    event.preventDefault();
                }
            } else {
                targetname = textFieldArray[i + 1];
                targetObjects = document.getElementsByName(targetname);
                if (targetObjects) {
                    targetObjects[0].focus();
                }
            }
            break;
        }
    }
}
