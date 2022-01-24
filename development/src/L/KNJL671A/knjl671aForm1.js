function btn_submit(cmd) {

    //取消確認
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    //終了
    if (cmd == 'close') {
        var confirmFlg = false;
        // 特別奨学生コード(コンボボックス)
        var scholarList = document.forms[0]['SP_SCHOLAR_CD\[\]'];
        // 特別奨学生コード(登録済み)
        var hidScholarList = document.forms[0]['HID_SP_SCHOLAR_CD\[\]'];
        if (scholarList && hidScholarList) {
            for (let i = 0; i < scholarList.length; i++) {
                const option = scholarList[i];
                const regScholarCd = hidScholarList[i];
                // 値が変更されている場合
                if (option.value != regScholarCd.value) {
                    confirmFlg = true;
                }
            }
        }
        if (confirmFlg) {
            if (confirm('{rval MSG108}')) {
                closeWin();
            }
        } else {
            closeWin();
        }
        return false;
    }

    //更新
    if (cmd == 'update') {

        // 2重 更新のチェック
        // 特別奨学生コード(コンボボックス)
        var scholarList = document.forms[0]['SP_SCHOLAR_CD\[\]'];
        // 特別奨学生コード(登録済み)
        var hidScholarList = document.forms[0]['HID_SP_SCHOLAR_CD\[\]'];

        if (scholarList && hidScholarList) {
            for (let i = 0; i < scholarList.length; i++) {
                const option = scholarList[i];
                const regScholarCd = hidScholarList[i];
                // 登録済みの場合
                if (regScholarCd && option) {
                    if (regScholarCd.value && option.value) {
                        if (option.value != regScholarCd.value) {
                            alert('{rval MSG901}' +'\n登録済みの特別奨学生コードと異なります。');
                            return false;
                        }
                    }
                }
            }
        }

    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function postSort(column) {
    document.forms[0].SORT_COLUMN.value = column;
    btn_submit('sort');
}
