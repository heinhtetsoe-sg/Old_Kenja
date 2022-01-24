    function btn_submit(cmd) {
        if (cmd == 'delete') {
            if (confirm('{rval MSG103}')) {
            } else {
                return;
            }
        }
        document.forms[0].cmd.value = cmd;
        document.forms[0].submit();
        return false;
    }

