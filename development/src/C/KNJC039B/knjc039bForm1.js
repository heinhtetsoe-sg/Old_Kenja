function btn_submit(cmd) {
    if (cmd == "update") {
        if (document.forms[0].GRADE.value == '') {
            alert('学年を指定してください。');
            return false;
        }
        if (document.forms[0].COLLECTION_CD.value == '') {
            alert('集計単位を指定してください。');
            return false;
        }

        if (confirm('実行しますか？')) {
            if (document.forms[0].DATA_CNT.value > 0) {
                if (confirm('指定した学年、集計単位のデータが既に存在します。上書きしますか？')) {
                    document.all('marq_msg').style.color = '#FF0000';
                } else {
                    return;
                }
            } else {
                document.all('marq_msg').style.color = '#FF0000';
            }
        } else {
            return;
        }
    }

    //読み込み中は、更新系ボタンをグレー（押せないよう）にする。
    document.forms[0].btn_update.disabled = true;
    document.forms[0].btn_collection.disabled = true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//確認画面へ
function callCollection(URL) {
    wopen(URL, 'SUBWIN2', 0, 0, screen.availWidth, screen.availHeight);
}

function closing_window(){
    alert('{rval MSG300}');
    closeWin();
    return true;
}
