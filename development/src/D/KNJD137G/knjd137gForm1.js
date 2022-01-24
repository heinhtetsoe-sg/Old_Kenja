function btn_submit(cmd) {
    if (cmd == 'update') {
        if (!document.forms[0].SCHREGNO.value) {
            alert('{rval MSG304}');
            return;
        }
        if (!document.forms[0].SEMESTER.value) {
            alert('{rval MSG304}');
            return;
        }
    } else if (cmd == 'subform1') {     //部活動参照
        if (!document.forms[0].SCHREGNO.value) {
            alert('{rval MSG304}');
            return;
        } else {
            loadwindow('knjd137gindex.php?cmd=subform1',0,0,700,300);
            return true;
        }
    } else if (cmd == 'subform2') {     //委員会参照
        if (!document.forms[0].SCHREGNO.value) {
            alert('{rval MSG304}');
            return;
        } else {
            loadwindow('knjd137gindex.php?cmd=subform2',0,0,700,300);
            return true;
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
