function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function checkAttendCd(obj) {
    var checkval;
    if (obj.value) {
        $.ajax({
            url:'knjj144cindex.php',
            type:'POST',
            data:{
                AJAX_ATTEND_CD:obj.value,
                cmd:'checkAttendCd'
            },
            async:false
        }).done(function(data, textStatus, jqXHR) {
            checkval = $.parseJSON(data);
        });
        if (checkval["checkResult"] == false) {
            alert('{rval MSG915}');
            obj.value = "";
        }
    }
}