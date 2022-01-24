
function btn_submit(cmd) {
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function apply_name() {
    top.document.forms[0]['FS_CD'].value = '000001';
    top.closeit();
}
function getFrame() {
    return document.forms[0].frame.value;
}
function apply_prischool(obj) {
    if (obj.selectedIndex >= 0) {
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");

        var prischoolcd = parent.document.forms[0].PRISCHOOLCD;
        var prischoolClassCd = parent.document.forms[0].PRISCHOOL_CLASS_CD;
        var prischoolname = parent.document.getElementById('label_priName');
        var prischoolClassName = parent.document.getElementById('label_priClassName');

        //塾CD
        if (prischoolcd) {
            prischoolcd.value = arr[0];
        }

        //教室CD
        if (prischoolClassCd) {
            prischoolClassCd.value = arr[1];
        }

        //塾名innerHTML
        if (prischoolname) {
            prischoolname.innerHTML = arr[2];
        }

        //教室名innerHTML
        if (prischoolClassName) {
            prischoolClassName.innerHTML = arr[3];
        }

        var submitFlg = document.forms[0].submitFlg.value;
        if (submitFlg == "1") {
            parent.document.forms[0].submit();
        }
        parent.closeit();
    } else {
        alert("データが選択されていません");
    }

}
function apply_prischoolGetParameter1(obj) {
    if (obj.selectedIndex >= 0) {
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");
        var priCd        = document.forms[0].prischool_cd.value;
        var priName      = document.forms[0].prischool_name.value;
        var priClassCd   = document.forms[0].prischool_class_cd.value;
        var priClassName = document.forms[0].prischool_class_name.value;

        if (priCd) {
            var prischoolcd = parent.document.forms[0][priCd];
        }
        if (priClassCd) {
            var prischoolclasscd = parent.document.forms[0][priClassCd];
        }
        if (priName) {
            var prischoolname = parent.document.getElementById(priName);
        }
        if (priClassName) {
            var prischoolclassname = parent.document.getElementById(priClassName);
        }

        //塾CD
        if (prischoolcd) {
            prischoolcd.value = arr[0];
        }

        //教室CD
        if (prischoolclasscd) {
            prischoolclasscd.value = arr[1].trim();
        }

        //塾名innerHTML
        if (prischoolname) {
            prischoolname.innerHTML = arr[2];
        }

        //教室名innerHTML
        if (prischoolclassname) {
            prischoolclassname.innerHTML = arr[3];
        }

        var submitFlg = document.forms[0].submitFlg.value;
        if (submitFlg == "1") {
            parent.document.forms[0].submit();
        }

        parent.closeit();
    } else {
        alert("データが選択されていません");
    }
}
