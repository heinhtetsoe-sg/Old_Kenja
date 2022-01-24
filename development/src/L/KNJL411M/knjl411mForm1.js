function btn_submit(cmd) {
    if (cmd == 'exec') {
        //ヘッダ出力
        if (document.forms[0].OUTPUT[0].checked == true) {
            cmd = 'head';

        //データ取込
        } else if (document.forms[0].OUTPUT[1].checked == true) {
            cmd = 'csvExec';

        //エラー出力
        } else if (document.forms[0].OUTPUT[2].checked == true) {
            cmd = 'csvDownload';
        
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
