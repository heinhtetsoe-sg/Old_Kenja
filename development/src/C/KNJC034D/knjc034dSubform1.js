/* Add by HPA for current_cursor start 2020/02/03 */
window.onload = function () {
    document.getElementById('screen_id').focus();
}

/* Add by HPA for current_cursor end 2020/02/20 */
//変更ボタン押し下げ時の処理
function btn_submit() {
    var y = 0;
    var CounterArray = new Array();
    var item = document.forms[0].GET_ITEM.value;

    //行番号取得
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        //選択した生徒のみ対象
        if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked) {
            CounterArray[y] = document.forms[0].elements[i].value;
            y++;
        }
    }

    var checkStr = eval("/^" + item + "_" + "/");

    for (var i = 0; i < parent.document.forms[0].elements.length; i++) {
        if (parent.document.forms[0].elements[i].name.match(checkStr)) {
            for (var j = 0; j < CounterArray.length; j++) {
                var checkStrCounter = eval("/^" + item + "_" + CounterArray[j] + "$/");
                if (parent.document.forms[0].elements[i].name.match(checkStrCounter)) {
                    parent.document.forms[0].elements[i].value = document.forms[0].REP_VALUE.value;
                }
            }
        }
    }

    //画面を閉じる
    parent.closeit();
    /* Add by HPA for current_cursor start 2020/02/03 */
    parent.current_cursor_focus();
    /* Add by HPA for current_cursor end 2020/02/20 */
}

//全チェック操作
function check_all(obj) {
    //チェックon/off
    obj.checked = !obj.checked;

    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

//ボタンの使用不可
function OptionUse(obj, cmd) {
    var check_flg = false;

    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]" && document.forms[0].elements[i].checked == true) {
            check_flg = true;
        }
    }

    if (check_flg == true) {
        document.forms[0].btn_replace.disabled = false;
    } else {
        document.forms[0].btn_replace.disabled = true;
    }
}

//変更対象生徒行選択（チェックボックスon/off）
var selectedRow = 0;
function selectRowList() {
    var list = document.getElementById('list');
    var chk = document.forms[0]["CHECKED\[\]"];

    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }

    selectedRow = event.srcElement.parentElement.rowIndex;

    //チェックon/off
    if (chk.length) {
        if (chk[selectedRow].disabled == false) {
            chk[selectedRow].checked = !chk[selectedRow].checked;
        }
    }

    //チェックボックスが全offなら変更ボタン使用不可
    var check_flg = false;
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            if (document.forms[0].elements[i].checked == true) {
                check_flg = true;
            }
        }
    }
    if (check_flg == true) {
        document.forms[0].btn_replace.disabled = false;
    } else {
        document.forms[0].btn_replace.disabled = true;
    }
}

//全チェック選択（チェックボックスon/off）
function selectRowAll() {
    var chk = document.forms[0]["CHECKALL"];
    var check_flg = false;

    //チェックon/off
    chk.checked = !chk.checked;

    //チェックボックスが全offなら変更ボタン使用不可
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "CHECKED[]") {
            if (document.forms[0].elements[i].disabled == true) {
                document.forms[0].elements[i].checked = false;
            } else {
                document.forms[0].elements[i].checked = chk.checked;
                if (chk.checked == true) {
                    check_flg = true;
                }
            }
        }
    }
    if (check_flg == true) {
        document.forms[0].btn_replace.disabled = false;
    } else {
        document.forms[0].btn_replace.disabled = true;
    }
}
