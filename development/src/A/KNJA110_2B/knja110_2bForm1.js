function btn_submit(cmd) {

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    return false;
}
function openEdit(linkpath){

    parent.location.href=linkpath;

    return false;
}
function closing_window(linkpath){

    alert('{rval MSG305}' + '\n( 名称マスタ:性別、続柄、地区コード )');
    parent.location.href=linkpath;

    return false;
}