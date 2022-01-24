function btn_submit(cmd) {
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }
    if (cmd == 'add' || cmd == 'update'){
        re = new RegExp("-" );
        if (document.forms[0].TARGETNAME1.value.match(re) ||
            document.forms[0].TARGETNAME2.value.match(re) ||
            document.forms[0].TARGETNAME3.value.match(re)
        ) {
            alert('名称に[-]は、使えません。');
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
