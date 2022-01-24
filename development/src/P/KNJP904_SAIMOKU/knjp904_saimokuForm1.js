function btn_submit(cmd) {

    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}'))
            return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//全てチェックボックス
function allCheck(obj) {
    re = new RegExp("CHECK_BOX:");

    for (var i=0; i < document.forms[0].elements.length; i++) {
        var obj_updElement = document.forms[0].elements[i];
        if (obj_updElement.name.match(re)) {
            obj_updElement.checked = obj.checked;
            checkOn(obj_updElement);
        }
    }

    return true;
}
//チェックボックス
function checkOn(obj) {
    var sCd = obj.getAttribute('data-name').split(':')[1];
    var checkBoxName = 'CHECK_BOX:' + sCd;
    var textBoxName  = 'BUDGET_MONEY:' + sCd;
    var setMoney = document.forms[0]["HID_LASTYEAR_BUDET:" + sCd].value;

    if (document.forms[0][checkBoxName].checked == true) {
        document.forms[0][textBoxName].value = setMoney;
    } else {
        document.forms[0][textBoxName].value = '';
    }

    return true;
}
//戻る
function Page_jumper(link) {
    parent.location.href=link;
}
