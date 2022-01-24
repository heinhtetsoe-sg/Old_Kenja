//サブミット
function btn_submit(cmd){

    //更新
    if (cmd == 'update') {
        //必須チェック
        if (document.forms[0].SDATE.value == "") {
            alert('{rval MSG310}'+'\n（ 来客日付範囲 ）');
            return true;
        }
        if (document.forms[0].EDATE.value == "") {
            alert('{rval MSG310}'+'\n（ 来客日付範囲 ）');
            return true;
        }
    }

    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}'))
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//印刷
function newwin(SERVLET_URL) {

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJF";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}

//日付変更
function tmp_list(cmd, submit) {
    if(document.forms[0].SDATE.value != ""){
        var from = document.forms[0].SDATE;
        var to   = document.forms[0].EDATE;
        document.forms[0].CTRL_DATE.value = document.forms[0].CTRL_DATE.value.replace("-", "/");
        document.forms[0].CTRL_DATE.value = document.forms[0].CTRL_DATE.value.replace("-", "/");
        var ctrldate = document.forms[0].CTRL_DATE;
        if(to.value != ""){
            if(from.value > to.value){
                alert('不正な日付です。');
                return false;
            }
        }

        if(from.value > ctrldate.value){
            var date = ctrldate.value.replace("/", "年");
                date = date.replace("/", "月");
                date = date+"日";
            alert(date+'以前の日付を選択してください。');
            return false;
        }

        //「yyyy/mm/dd」⇒「yyyy-mm-dd」
        document.forms[0].CTRL_DATE.value = document.forms[0].CTRL_DATE.value.replace("/", "-");
        document.forms[0].CTRL_DATE.value = document.forms[0].CTRL_DATE.value.replace("/", "-");
    }
    document.forms[0].cmd.value = cmd;
    if (submit == 'on') {
        document.forms[0].submit();
        return false;
    }
}
