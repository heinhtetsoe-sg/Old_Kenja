function btn_submit(cmd)
{
    if (cmd == 'update') {
        //入力チェック
        if (document.forms[0].DIARY_DATE.value == "") {
            alert("日付が未入力です。");
            document.forms[0].DIARY_DATE.focus();
            return false;
        }
    } else if ((cmd == 'delete') && !confirm('{rval MSG103}')){
        return true;
    }

    //入力チェック
    if (document.forms[0].DIARY_DATE.value == "") {
        alert("日付が未入力です。");
        document.forms[0].DIARY_DATE.focus();
        return false;
    }
    diary_date = document.forms[0].DIARY_DATE.value;
    if (cmd == 'kesseki') {
        loadwindow('knjg045index.php?cmd='+cmd+'&diary_date='+diary_date,1000,0,350,700);
        return true;
    } else if (cmd == 'chikoku') {
        loadwindow('knjg045index.php?cmd='+cmd+'&diary_date='+diary_date,1000,0,350,700);
        return true;
    } else if (cmd == 'soutai') {
        loadwindow('knjg045index.php?cmd='+cmd+'&diary_date='+diary_date,1000,0,350,700);
        return true;
    } else if (cmd == 'shuchou') {
        loadwindow('knjg045index.php?cmd='+cmd+'&diary_date='+diary_date,1000,0,350,700);
        return true;
    } else if (cmd == 'hoketsu') {
        grade_hr_class = document.forms[0].GRADE_HR_CLASS.value;
        if (grade_hr_class == "") {
            alert("年組を選択してください。");
            return false;
        } else {
            loadwindow('knjg045index.php?cmd='+cmd+'&grade_hr_class='+grade_hr_class+'&diary_date='+diary_date,1000,0,450,700);
            return true;
        }
    } else if (cmd == 'etc_hoketsu') {
        loadwindow('knjg045index.php?cmd='+cmd+'&diary_date='+diary_date,1000,0,450,700);
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
//欠席状況の計算
function calc(obj, di_cd) {
    //数値チェック
    obj.value = toInteger(obj.value);

    //加算
    var total_cnt = 0;
    re = new RegExp("^CNT_"+di_cd );
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name.match(re)) {
            var val = parseInt(document.forms[0].elements[i].value);
            if (val > 0) {
                total_cnt += val;
            }
        }
    }

    //加算した値を貼り付け
    id = 'total_'+di_cd;
    var total = document.getElementById(id);
    total.innerHTML = total_cnt;
}
