
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
function apply_finschool(obj) {
    if (obj.selectedIndex >= 0) {
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");
        var finschoolcd = parent.document.forms[0].FS_CD;
        var finschoolname = parent.document.getElementById('label_name');
//        var chikuNameId = parent.document.getElementById('CHIKU_NAME_ID');
//        var ritsuNameId = parent.document.getElementById('RITSU_NAME_ID');
        //学校CD(愛知県中学校コード)
        if (finschoolcd) {
            var tmp = arr[0];
            tmp = tmp.replace('　', '');
            tmp = tmp.replace('　', '');
            finschoolcd.value = tmp;
        }

        //学校名innerHTML
        if (finschoolname) {
            finschoolname.innerHTML = arr[3];
        }
        parent.closeit();
    } else {
        alert("データが選択されていません");
    }

}

function apply_finschoolgetParametr(obj) {
    if (obj.selectedIndex >= 0) {
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");
        var fscdname = document.forms[0].fscdname.value;
        var fsname = document.forms[0].fsname.value;
        var fsRitsuNameId = document.forms[0].fsRitsuNameId.value;
        var fsaddr = document.forms[0].fsaddr.value;
        var fsSchool_div = document.forms[0].school_div.value;
        var fsChikuName = document.forms[0].fsChikuName.value;
        var fszip = document.forms[0].fszip.value;
        var fsaddr1 = document.forms[0].fsaddr1.value;
        var fsaddr2 = document.forms[0].fsaddr2.value;
        var l015 = document.forms[0].l015.value;
        var tell = document.forms[0].tell.value;

        if (fscdname) {
            var finschoolcd = parent.document.forms[0][fscdname];
        }
        if (fsname) {
            var finschoolname = parent.document.getElementById(fsname);
        }
        if (fsChikuName) {
            var chikuNameId = parent.document.getElementById(fsChikuName);
        }
        if (fsRitsuNameId) {
            var ritsuNameId = parent.document.getElementById(fsRitsuNameId);
        }
        if (fsaddr) {
            var fsaddr = parent.document.getElementById(fsaddr);
        }
        if (school_div) {
            var school_div = parent.document.getElementById(fsSchool_div);
        }
        if (fszip) {
            var fszip = parent.document.getElementById(fszip);
        }
        if (fsaddr1) {
            var fsaddr1 = parent.document.getElementById(fsaddr1);
        }
        if (fsaddr2) {
            var fsaddr2 = parent.document.getElementById(fsaddr2);
        }
        if (l015) {
            var l015 = parent.document.getElementById(l015);
        }
        if (tell) {
            var tell = parent.document.getElementById(tell);
        }

        //学校CD
        if (finschoolcd) {
            finschoolcd.value = arr[0];
        }

        //学校名innerHTML
        if (finschoolname) {
            finschoolname.innerHTML = arr[6] + arr[2];
        }

        //学校地区innerHTML
        if (chikuNameId) {
            chikuNameId.innerHTML = arr[5];
        }

        //学校立innerHTML
        if (ritsuNameId) {
            ritsuNameId.innerHTML = arr[6];
        }

        //学校住所innerHTML
        if (fsaddr) {
            fsaddr.innerHTML = arr[3] + arr[4];
        }

        //学校種類innerHTML
        if (school_div) {
            school_div.innerHTML = arr[1];
        }

        //郵便番号innerHTML
        if (fszip) {
            fszip.innerHTML = arr[7];
        }

        //学校住所1innerHTML
        if (fsaddr1) {
            fsaddr1.innerHTML = arr[3];
        }

        //学校住所2innerHTML
        if (fsaddr2) {
            fsaddr2.innerHTML = arr[4];
        }

        //SCHOOL_DIV(tell)innerHTML
        if (tell) {
            tell.innerHTML = arr[8];
        }

        //SCHOOL_DIV(L015)innerHTML
        if (l015) {
            l015.innerHTML = arr[9];
        }

        parent.closeit();
    } else {
        alert("データが選択されていません");
    }
}

function apply_finschoolgetParametr2(obj) {
    if (obj.selectedIndex >= 0) {
        var val = obj.options[obj.selectedIndex].text;
        var arr = val.split(" | ");

        var fscdname = document.forms[0].fscdname.value;
        var fsname = document.forms[0].fsname.value;
        var fsRitsuNameId = document.forms[0].fsRitsuNameId.value;
        var fsaddr = document.forms[0].fsaddr.value;
        var fsSchool_div = document.forms[0].school_div.value;
        var fsChikuName = document.forms[0].fsChikuName.value;
        var fszip = document.forms[0].fszip.value;
        var fsaddr1 = document.forms[0].fsaddr1.value;
        var fsaddr2 = document.forms[0].fsaddr2.value;
        var l015 = document.forms[0].l015.value;
        var tell = document.forms[0].tell.value;

        if (fscdname) {
//            var finschoolcd = parent.document.forms[0][fscdname];
            var finschoolcd = parent.document.getElementById(fscdname);
        }
        if (fsname) {
            var finschoolname = parent.document.getElementById(fsname);
        }
        if (fsChikuName) {
            var chikuNameId = parent.document.getElementById(fsChikuName);
        }
        if (fsRitsuNameId) {
            var ritsuNameId = parent.document.getElementById(fsRitsuNameId);
        }
        if (fsaddr) {
            var fsaddr = parent.document.getElementById(fsaddr);
        }
        if (school_div) {
            var school_div = parent.document.getElementById(fsSchool_div);
        }
        if (fszip) {
            var fszip = parent.document.forms[0][fszip];
        }
        if (fsaddr1) {
            var fsaddr1 = parent.document.forms[0][fsaddr1];
        }
        if (fsaddr2) {
            var fsaddr2 = parent.document.forms[0][fsaddr2];
        }
        if (l015) {
            var l015 = parent.document.forms[0][l015];
        }
        if (tell) {
            var tell = parent.document.forms[0][tell];
        }

        //学校CD
        if (finschoolcd) {
            finschoolcd.value = arr[0];
        }

        //学校名
        if (finschoolname) {
            finschoolname.value = arr[6] + arr[2];
        }

        //学校地区innerHTML
        if (chikuNameId) {
            chikuNameId.innerHTML = arr[5];
        }

        //学校立innerHTML
        if (ritsuNameId) {
            ritsuNameId.innerHTML = arr[6];
        }

        //学校住所innerHTML
        if (fsaddr) {
            fsaddr.innerHTML = arr[3] + arr[4];
        }

        //学校種類innerHTML
        if (school_div) {
            school_div.innerHTML = arr[1];
        }

        //郵便番号innerHTML
        if (fszip) {
            fszip.innerHTML = arr[7];
        }

        //学校住所1innerHTML
        if (fsaddr1) {
            fsaddr1.innerHTML = arr[3];
        }

        //学校住所2innerHTML
        if (fsaddr2) {
            fsaddr2.innerHTML = arr[4];
        }

        //SCHOOL_DIV(tell)innerHTML
        if (tell) {
            tell.innerHTML = arr[8];
        }

        //SCHOOL_DIV(L015)innerHTML
        if (l015) {
            l015.innerHTML = arr[9];
        }

        parent.closeit();
    } else {
        alert("データが選択されていません");
    }
}
//エンターキーをTabに変換
function changeEnterToTab (obj) {
    if (window.event.keyCode == '13') {
        if (obj.name == "FINSCHOOL_DISTCD") {
            document.forms[0].cmd.value = 'search';
            document.forms[0].submit();
            return false;
        }

        //移動可能なオブジェクト
        var textFieldArray = document.forms[0].setTextField.value.split(",");

        for (var i = 0; i < textFieldArray.length; i++) {
            if (textFieldArray[i] == obj.name) {
                targetObject = eval("document.forms[0][\"" + textFieldArray[(i + 1)] + "\"]");
                targetObject.focus();
                return;
            }
        }
    }
    return;
}
