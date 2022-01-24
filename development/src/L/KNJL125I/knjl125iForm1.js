function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //終了
    if (cmd == 'end') {
        closeWin();
    }

    if (cmd == 'update') {
        //どこか選択されているかをチェック
        var wkFlg = false;
        var nodeList = document.querySelectorAll(".check-elems");
        for (var i = 0; i < nodeList.length; i++) {
            var checkElem = nodeList[i];
            if (checkElem.checked) {
                wkFlg = true;
            }
        }
        if (!wkFlg) {
            alert('{rval MSG304}'+"どこにもチェックが付いていません。");
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkCheck() {
    var nodeList = document.querySelectorAll(".check-elems");
    for (var i = 0; i < nodeList.length; i++) {
        var checkElem = nodeList[i];
        if (checkElem.checked) {

        }
    }
}

function checkAll(obj) {
    var checkVal = obj.checked;
    var nodeList = document.querySelectorAll(".check-elems");
    for (var i = 0; i < nodeList.length; i++) {
        var checkElem = nodeList[i];
        checkElem.checked = checkVal;
        changeColor(checkElem);
    }
}

function changeColor(obj) {
    var checkVal = obj.checked;
    var targetRow = document.getElementById("ROWID" + obj.value);
    if (checkVal) {
        targetRow.style.backgroundColor = "#ffff00";
    } else {
        targetRow.style.backgroundColor = "#ffffff";
    }
}
