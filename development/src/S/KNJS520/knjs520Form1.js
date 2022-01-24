function btn_submit(cmd) {
    if (cmd == 'reset'){
        result = confirm('{rval MSG106}');
        if (result == false)
            return false;
    }
    if (cmd == 'yotei'){
        //EVENT_MSTデータチェック
        var getevent = document.forms[0].GETEVENT.value;

        if (getevent > 0 ) {
            result = confirm('{rval MSG104}' + '\n（学校用行事予定マスタを削除します）');
            if (result == false)
                return false;
        } else {
            result = confirm('{rval MSG102}');
            if (result == false)
                return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

// 権限
function closing_window()
{
        alert('{rval MSG300}');
        closeWin();
        return true;
}


//年度追加（コンボのみ追加）
function add(cmd) {
    if (cmd == 'year_add'){
        var temp1 = new Array();
        var tempa = new Array();
        var v = document.forms[0].YEAR.length;
        var w = document.forms[0].year_add.value

        if (w == "")
            return false;

        for (var i = 0; i < v; i++) {
            if (w == document.forms[0].YEAR.options[i].value) {
                alert("追加した年度は既に存在しています。");
                return false;
            }
        }
        document.forms[0].YEAR.options[v] = new Option();
        document.forms[0].YEAR.options[v].value = w;
        document.forms[0].YEAR.options[v].text = w;

        for (var i = 0; i < document.forms[0].YEAR.length; i++) {
            temp1[i] = document.forms[0].YEAR.options[i].value;
            tempa[i] = document.forms[0].YEAR.options[i].text;
        }
        //sort
        temp1 = temp1.sort();
        tempa = tempa.sort();
        temp1 = temp1.reverse();
        tempa = tempa.reverse();

        //generating new options
        ClearList(document.forms[0].YEAR,document.forms[0].YEAR);
        if (temp1.length>0) {
            for (var i = 0; i < temp1.length; i++) {
                document.forms[0].YEAR.options[i] = new Option();
                document.forms[0].YEAR.options[i].value = temp1[i];
                document.forms[0].YEAR.options[i].text =  tempa[i];
                if(w==temp1[i]) {
                    document.forms[0].YEAR.options[i].selected=true;
                }
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


