function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function GoOpener(schregno){
    top.opener.document.forms[0].SEARCH_SCHREGNO.value = schregno;
    top.opener.document.forms[0].cmd.value = 'search';
    top.opener.document.forms[0].submit();
    top.window.close();
    return false;
}

function SearchResult(){
    alert('データは存在していません。');
}
