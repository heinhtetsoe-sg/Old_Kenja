function btn_submit(cmd) {

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
            return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
// マスタ作成チェック
function closeCheck(){
        alert('{rval MSG300}'+'\nKNJP700を実行してマスタを作成して下さい。');
        closeWin();
}
//細目ページ移動
function Page_jumper(link) {
    parent.location.href=link;
}
