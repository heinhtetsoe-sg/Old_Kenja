function btn_submit(cmd) {

    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//参照画面
function sansyouWindow(cmd, target) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    } else if (cmd == 'club') {         //部活動選択
        loadwindow('knjd132findex.php?cmd=club&target='+target,event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(),800,350);
        return true;
    } else if (cmd == 'committee') {    //委員会選択
        loadwindow('knjd132findex.php?cmd=committee&target='+target,event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(),750,350);
        return true;
    } else if (cmd == 'qualified') {    //検定選択
        var sizeW = 670;
        if (document.forms[0].useQualifiedMst.value == "1") {
            sizeW = 800;
        }
        loadwindow('knjd132findex.php?cmd=qualified&target='+target,event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(),sizeW,550);
        return true;
    } else if (cmd == 'clubhdetail') {  //記録備考選択
        loadwindow('knjd132findex.php?cmd=clubhdetail&target='+target,event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(),800,350);
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
