function btn_submit(cmd)
{
    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check(obj) {
    var getname = obj.name;
    var setname_array = getname.split("-");
    if (obj.value == 1) {
        document.getElementById("REMARK2-" + setname_array[1]).checked = false;
        document.getElementById("REMARK3-" + setname_array[1]).checked = false;
        document.getElementById("REMARK4-" + setname_array[1]).checked = false;
        document.getElementById("REMARK5-" + setname_array[1]).checked = false;
    }
    if (obj.value == 2) {
        document.getElementById("REMARK1-" + setname_array[1]).checked = false;
        document.getElementById("REMARK3-" + setname_array[1]).checked = false;
        document.getElementById("REMARK4-" + setname_array[1]).checked = false;
        document.getElementById("REMARK5-" + setname_array[1]).checked = false;
    }
    if (obj.value == 3) {
        document.getElementById("REMARK1-" + setname_array[1]).checked = false;
        document.getElementById("REMARK2-" + setname_array[1]).checked = false;
        document.getElementById("REMARK4-" + setname_array[1]).checked = false;
        document.getElementById("REMARK5-" + setname_array[1]).checked = false;
    }
    if (obj.value == 4) {
        document.getElementById("REMARK1-" + setname_array[1]).checked = false;
        document.getElementById("REMARK2-" + setname_array[1]).checked = false;
        document.getElementById("REMARK3-" + setname_array[1]).checked = false;
        document.getElementById("REMARK5-" + setname_array[1]).checked = false;
    }
    if (obj.value == 9) {
        document.getElementById("REMARK1-" + setname_array[1]).checked = false;
        document.getElementById("REMARK2-" + setname_array[1]).checked = false;
        document.getElementById("REMARK3-" + setname_array[1]).checked = false;
        document.getElementById("REMARK4-" + setname_array[1]).checked = false;
    }
    return;
}