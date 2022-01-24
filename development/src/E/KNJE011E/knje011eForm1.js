function btn_submit(cmd, arg) {
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }
    var w_size;
    var h_size;
    if (cmd == 'reload'){ //成績参照
        reloadIframe("knje011eindex.php?cmd=reload");
        for (var i = 0; i < document.forms[0]["CHECK\[\]"].length; i++){
            document.forms[0]["CHECK\[\]"][i].checked = false;
        }
        return true;
    }else if (cmd == 'form2_first'){ //特別活動の記録～
        if (document.forms[0].useSyojikou3.value == "1") {
            var w = 1100;
            if (window.outerWidth === undefined) {
            } else {
                w = parseInt((window.outerWidth * 0.75), 10)
            }
            var h = 850;
            if (window.outerHeight === undefined) {
            } else {
                h = parseInt((window.outerHeight * 0.75), 10)
            }
            w_size = (w > 1100) ? 1100 : w;
            h_size = (h > 850) ? 850 : h;
        } else if (document.forms[0].tyousasyoTokuBetuFieldSize.value == "1") {
            w_size = 780;
            h_size = 650;
        } else {
            w_size = 670;
            h_size = 500;
        }
        loadwindow('knje011eindex.php?cmd=form2_first',0,document.documentElement.scrollTop || document.body.scrollTop,w_size,h_size);
        return true;
    }else if (cmd == 'formSeiseki_first'){ //成績参照
        loadwindow('knje011eindex.php?cmd=formSeiseki_first',0,document.documentElement.scrollTop || document.body.scrollTop,600,540);
        return true;
    }else if (cmd == 'formYorokuSanshou_first'){ //指導要録参照
        loadwindow('knje011eindex.php?cmd=formYorokuSanshou_first',0,document.documentElement.scrollTop || document.body.scrollTop,710,290);
        return true;
    }else if (cmd == 'formYorokuSanshou2_first'){ //指導要録参照
        loadwindow('knje011eindex.php?cmd=formYorokuSanshou2_first',0,document.documentElement.scrollTop || document.body.scrollTop,730,430);
        return true;
    }else if (cmd == 'hrShojikouTorikomi') { // 指導上参考となる諸事項 年組一括取込
        var hrname = arg["HR_NAME"] || "";
        var message = hrname + "全生徒の指導上参考となる諸事項を一括取込みします。";
        var countYears = arg["COUNT_YEARS"];
        if (countYears && countYears.length > 0) {
            message += "\n\n" + "※以下の入力済みの指導上参考となる諸事項を削除して取込みます。";
            for (var i = 0; i < countYears.length; i++) {
                message += "\n  " + countYears[i];
            }
        }
        message += "\n\n処理を続行しますか？";
        if (!confirm(message)) {
            return false;
        }
    }else if (cmd == 'reset'){ //取り消し
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }
    //更新中の画面ロック(全フレーム)
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }
    if (cmd == 'update') {
        //更新ボタン・・・読み込み中は、更新ボタンをグレー（押せないよう）にする。
        document.forms[0].btn_update.disabled = true;
        document.forms[0].btn_up_next.disabled = true;
        document.forms[0].btn_up_pre.disabled = true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function reloadIframe(url){
    document.getElementById("cframe").src=url
}

//備考チェックボックス
function CheckRemark() {
    if (document.forms[0].REMARK_NO_COMMENTS.checked == true) {
        document.forms[0].REMARK.value = document.forms[0].NO_COMMENTS_LABEL.value;
        document.forms[0].REMARK.disabled =true;
    } else {
        document.forms[0].REMARK.disabled = false;
    }
}

function newwin(SERVLET_URL){
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }

    var gradehrclass = parent.left_frame.document.forms[0].GRADE.value.split('-');
    parent.right_frame.document.forms[0].GRADE_HR_CLASS.value = gradehrclass[0] + gradehrclass[1];
    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJE";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

function Page_jumper(link) {
    if (document.forms[0].SCHREGNO.value == "") {
        alert('リストから生徒を選択してください。');
        return;
    }
    if (!confirm('{rval MSG108}')) {
        return;
    }
    parent.location.href=link;
}