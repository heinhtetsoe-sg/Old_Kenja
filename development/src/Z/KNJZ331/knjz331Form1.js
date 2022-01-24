function btn_submit(cmd) {
    if (cmd === 'update') {
        selectMenu = document.forms[0].selectMenu;
        selectMenu.value = "";
        sep = "";
        for (var i = 0; i < document.forms[0].CATEGORY_SELECTED.length; i++) {
            selectMenu.value = selectMenu.value + sep + document.forms[0].CATEGORY_SELECTED.options[i].value;
            sep = ",";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

