function btn_submit(cmd)
{
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    if (cmd == 'houkoku' || cmd == 'update') {
        if (document.forms[0].SCHOOLCD.value == "") {
            alert('教育委員会統計用学校番号が、未登録です。');
            return false;
        }
    }
    if (cmd == 'houkoku') {
        if (document.forms[0].FIXED_DATA.value == "") {
            alert('{rval MSG304}'+'(報告データ)');
            return false;
        }
        if (document.forms[0].EXECUTE_DATE.value == "") {
            alert('{rval MSG304}'+'(報告日)');
            return false;
        }
        if (!confirm('報告データ日付の集計結果を県へ報告します。よろしいですか。')) return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError()
{
    alert('{rval MSG300}');
    closeWin();
}

function OnClose()
{
    if (!confirm('保存前のデータは破棄されます。よろしいですか？')) {
        return false;
    } else {
        closeWin();
    }
}

function scrollRC()
{
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
}

function fixed(REQUESTROOT)
{
    load  = "loadwindow('"+ REQUESTROOT +"/F/KNJF345/knjf345index.php?cmd=fixedLoad";
    load += "',400,250,450,250)";

    eval(load);
}
