function btn_submit(cmd) {
    if (cmd == 'teikei1' || cmd == 'teikei2') {
        loadwindow('knjd132dindex.php?cmd=' + cmd + '&TEIKEI_CMD=' + cmd, event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 450);
        return true;
    }

    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0].left_select.length; i++)
    {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check_all(obj, col) {
    var ii = 0;
    re = new RegExp("CHECK");
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (String(document.forms[0].elements[i].name).match(re)) {
            if (String(document.forms[0].elements[i].name).substr(5,1) === col) {
                document.forms[0].elements[i].checked = obj.checked;
                ii++;
            }
        }
    }
}

function doSubmit(cmd) {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = "";
    sep = "";

    if (document.forms[0].left_select.length==0 && document.forms[0].right_select.length==0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
