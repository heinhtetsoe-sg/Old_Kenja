
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

        var finschoolcd = parent.document.forms[0].FINSCHOOLCD;
        var finschoolname = parent.document.getElementById('label_name');
        var chikuNameId = parent.document.getElementById('CHIKU_NAME_ID');
        var ritsuNameId = parent.document.getElementById('RITSU_NAME_ID');

        //学校CD
        if (finschoolcd) {
            finschoolcd.value = arr[0];
        }

        //学校名innerHTML
        if (finschoolname) {
            finschoolname.innerHTML = arr[2];
        }

        //学校地区innerHTML
        if (chikuNameId) {
            chikuNameId.innerHTML = arr[5];
        }

        //学校立innerHTML
        if (ritsuNameId) {
            ritsuNameId.innerHTML = arr[6];
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

        //学校CD
        if (finschoolcd) {
            finschoolcd.value = arr[0];
        }

        //学校名innerHTML
        if (finschoolname) {
            finschoolname.innerHTML = arr[2];
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

        parent.closeit();
    } else {
        alert("データが選択されていません");
    }
}
