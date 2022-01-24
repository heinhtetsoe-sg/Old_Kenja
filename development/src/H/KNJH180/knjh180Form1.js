var marq_msg;
function btn_submit(cmd) {
    if (document.forms[0].csvfile.value == '') {
        alert('ファイルが指定されていません');
        return false;
    }
    if (cmd == 'execute' && !confirm('{rval MSG101}'+'\nこの処理は数分かかる場合があります。')){
        return true;
    }
    marq_msg = document.getElementById("marq_msg");
    marq_msg.style.display = "block";

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}