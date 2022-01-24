function check_all(obj) {
    var ii = 0;
    re = new RegExp('RCHECK');
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        if (String(document.forms[0].elements[i].name).match(re)) {
            document.forms[0].elements[i].checked = obj.checked;
            ii++;
        }
    }
}

function doSubmit(cmd) {
    attribute3 = document.forms[0].selectdata;
    attribute3.value = '';
    sep = '';
    if (document.forms[0].left_select.length == 0 && document.forms[0].right_select.length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    for (var i = 0; i < document.forms[0].left_select.length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0].left_select.options[i].value;
        sep = ',';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//サブフォーム
function btn_submit_subform(cmd, counter, semester, chaircd, staffcdField) {
    var staffcd = document.forms[0][staffcdField].value;

    //教職員選択ボタン押し下げ時
    if (cmd == 'substaff') {
        counter = 0;
        loadwindow(
            'knjb103cindex.php?cmd=substaff2&Counter=' + counter + '&SEMESTER=' + semester + '&STAFF_CHAIRCD=' + chaircd + '&STAFF_STAFFCD=' + staffcd,
            event.clientX +
                (function () {
                    var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;
                    return scrollX;
                })(),
            event.clientY +
                (function () {
                    var scrollY = document.documentElement.scrollTop || document.body.scrollTop;
                    return scrollY;
                })(),
            650,
            450
        );
        return true;
    }
}
