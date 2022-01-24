function btn_submit(cmd) {

    if (cmd == 'exec' && document.forms[0].OUTPUT[1].checked == true &&
        document.forms[0].YEAR.value == ""
    ) {
        alert('処理年度を指定して下さい。');
        return false;
    }

    if (cmd == 'exec' && !confirm('処理を開始します。よろしいでしょうか？')) {
        return true;
    }
    if (document.forms[0].OUTPUT[1].checked == true) {
        if (document.forms[0].SHORI_MEI.value == '2' && !confirm('（再確認）削除を開始します。よろしいでしょうか？')) {
            return true;
        }
    } else if (cmd != ""){
        cmd = "output";
    }
    document.forms[0].btn_ok.disabled = true;
    document.forms[0].btn_cancel.disabled = true;

    document.all('marq_msg').style.color = '#FF0000';

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Show_ErrMsg(flg)
{

    if (flg == 1) {
        alert('課程コードまたは学科コードが設定されていません');
    }
    if (flg == 3) {
        alert('テンプレートの書き出しに失敗しました');
        return ;
    }       
    closeWin();
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function OutputFile(filename)
{
    parent.top_frame.location.href=filename;
}

