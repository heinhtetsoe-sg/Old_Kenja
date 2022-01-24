function btn_submit(cmd) {
    //削除
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    //CSV処理
    if (cmd == "csv") {
        if (
            document.forms[0].OUTPUT[1].checked &&
            document.forms[0].FILE.value == ""
        ) {
            alert("ファイルを指定してください");
            return false;
        }

        if (document.forms[0].OUTPUT[0].checked) {
            cmd = "headerCsv";
        } else if (document.forms[0].OUTPUT[1].checked) {
            cmd = "uploadCsv";
        } else if (document.forms[0].OUTPUT[2].checked) {
            cmd = "downloadCsv";
        } else if (document.forms[0].OUTPUT[3].checked) {
            cmd = "downloadErr";
        } else {
            alert("ラジオボタンを選択してください。");
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
    console.log(vflg);
}

function updateCheckBoxChanged() {
    if (document.forms[0].NAME_FLG.checked || document.forms[0].TESTDIV_FLG.checked || document.forms[0].COURSE_FLG.checked || document.forms[0].STANDARD_EXAM_FLG.checked || document.forms[0].HONOR_FLG.checked || document.forms[0].OTHER_FLG.checked) {
        document.forms[0].btn_update.disabled = false;
    } else {
        document.forms[0].btn_update.disabled = true;
    }
}

//学校検索画面のプログラムで呼び出す関数を空定義
function current_cursor_focus() {}
function current_cursor_list() {}
