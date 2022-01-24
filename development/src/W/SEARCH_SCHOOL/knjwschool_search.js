function btn_submit(cmd) {
    if (cmd == "search" &&
        document.forms[0].FINSCHOOL_TYPE.value == "" &&
        document.forms[0].NAME.value == "" &&
        document.forms[0].ADDR.value == "" &&
        document.forms[0].ZIPCD.value == ""
    ) {
        alert("検索項目を一つ以上選択して下さい。");
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function apply_school(obj, targetname) {
    if (obj.selectedIndex >= 0) {
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");

        var frameName = parent.document.forms[0];

        if (eval("frameName." + targetname + "_SCHOOL") != undefined) {
            eval("frameName." + targetname + "_SCHOOL.value = arr[2]");
        }
        if (eval("frameName." + targetname + "_ADDR") != undefined) {
            eval("frameName." + targetname + "_ADDR.value = arr[3]");
        }
        if (eval("frameName." + targetname + "_SCHOOLCD") != undefined) {
            eval("frameName." + targetname + "_SCHOOLCD.value = arr[0]");
        }

        parent.closeit();
    } else {
        alert("データが選択されていません");
    }
}
