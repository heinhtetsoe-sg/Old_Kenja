function btn_submit(cmd) {
    if (cmd === 'update') {
        if (document.forms[0].SEMESTER.value == "") {
            alert('�w�����I������Ă��܂���B');
            return;
        }
        if (document.forms[0].RIREKI_CODE.value == "") {
            alert('���C�o�^�����I������Ă��܂���B');
            return;
        }
        if (document.forms[0].SCH_PTRN.value == "") {
            alert('��{���Ԋ����I������Ă��܂���B');
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();

    return false;
}
//���
function newwin(SERVLET_URL) {
    if (document.forms[0].SEMESTER.value == "") {
        alert('�w�����I������Ă��܂���B');
        return;
    }
    if (document.forms[0].RIREKI_CODE.value == "") {
        alert('���C�o�^�����I������Ă��܂���B');
        return;
    }
    if (document.forms[0].SCH_PTRN.value == "") {
        alert('��{���Ԋ����I������Ă��܂���B');
        return;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;
//    document.forms[0].action = "/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJB";
    document.forms[0].target = "_blank";
    document.forms[0].submit();
    document.forms[0].action = action;
    document.forms[0].target = target;
}

function scrollRC() {
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop = document.getElementById('tbody').scrollTop;
}

function ClickValAll(obj) {
    for (var i = 0; i < document.forms[0].elements.length; i++) {
        var obj_updElement = document.forms[0].elements[i];
        re = new RegExp("^CHAIR" );
        if (obj_updElement.name.match(re) && obj_updElement.value == obj.value) {
            obj_updElement.checked = obj.checked;
        }
    }
}

