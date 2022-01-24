//ソート選択
function btn_submit(cmd) {
    var output = document.forms[0].OUTPUT.value;
    if (output == 1) document.forms[0].OUTPUT.value = 2;
    if (output == 2) document.forms[0].OUTPUT.value = 1;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//入力および履歴画面へ
function btn_select(URL) {
    var flg = false;
    var hr_name;
    for (var i = 0; i < document.forms[0].HR_NAME.length; i++) {
        if (document.forms[0].HR_NAME.options[i].selected) {
            hr_name = document.forms[0].HR_NAME.options[i].value;
            flg = true;
        }
    }
    if (!flg) {
        alert('クラスを選択して下さい。');
        return;
    }

    wopen(URL + '?hr_name=' + hr_name, 'SUBWIN2', 0, 0, screen.availWidth, screen.availHeight);
}

//生徒検索画面へ
function btn_seito(URL) {
    wopen(URL, 'SUBWIN_SEARCH', 0, 0, screen.availWidth, screen.availHeight);
}

//一覧確認画面へ
function Page_jumper(URL, year, semester) {
    wopen(URL, 'name', 0, 0, screen.availWidth, screen.availHeight);
}
