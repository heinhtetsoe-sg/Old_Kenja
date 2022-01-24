function btn_submit(cmd) {
    //実行
    if (cmd == "create") {
        if (document.forms[0].YEAR_SEMESTER.value == '') {
            alert("年度学期が未入力です。");
            return;
        }
        if (document.forms[0].RIREKI_CODE.value == "") {
            alert('履修登録日が選択されていません。');
            return;
        }

        executed_cnt = document.forms[0].EXECUTED_CNT.value;
        if (executed_cnt > 0) {
            alert("出欠済みの時間割があるので処理できません。");
            return;
        }

        std_cnt = document.forms[0].STD_CNT.value;
        if (std_cnt > 0) {
            if (!confirm('講座名簿データが存在しています。\n削除して更新していいですか？')) {
                return true;
            }
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
