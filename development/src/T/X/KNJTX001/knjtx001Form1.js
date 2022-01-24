function btn_submit(cmd) {
   if (document.forms[0].year.value == '') {
        alert('{rval MSG304}' + ' （対象年度）');
        return false;
   }
    if (cmd == 'update') {
        if (document.forms[0].GVAL_CALC.length == 1) {
            alert('{rval MSG305}'+'\n『評定計算方法』。\n名称マスタメンテにて設定して下さい。');
            return false;
        }
        if (document.forms[0].ABSENT_COV.value == 5) {
            var absent_cov_late = document.forms[0].ABSENT_COV_LATE.value;
            var amari_kuriage   = document.forms[0].AMARI_KURIAGE.value;
            if (absent_cov_late == '' && amari_kuriage != '') {
                alert('{rval MSG301}\n欠課数換算');
                return false;
            }
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function showConfirm() {
    if(confirm('{rval MSG107}')) {
        document.forms[0].cmd.value = "main";
        document.forms[0].submit();
    }
    return false;
}

function closing_window(flg) {
    if (flg == 1) {
        alert('{rval MSG300}');
    }
    if (flg == 2) {
        alert('{rval MSG305}' + '\n(コントロールマスタまたは評定マスタ)');
    }
    closeWin();
    return true;
}

function add() {
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].year.length;
    var w = document.forms[0].year_add.value

    if (w == "")
        return false;

    for (var i = 0; i < v; i++) {
        if (w == document.forms[0].year.options[i].value) {
            alert("追加した年度は既に存在しています。");
            return false;
        }
    }
    document.forms[0].year.options[v] = new Option();
    document.forms[0].year.options[v].value = w;
    document.forms[0].year.options[v].text = w;

    for (var i = 0; i < document.forms[0].year.length; i++) {
        temp1[i] = document.forms[0].year.options[i].value;
        tempa[i] = document.forms[0].year.options[i].text;
    }
    //sort
    temp1 = temp1.sort();
    tempa = tempa.sort();
    temp1 = temp1.reverse();
    tempa = tempa.reverse();

    //generating new options
    ClearList(document.forms[0].year,document.forms[0].year);
    if (temp1.length>0) {
        for (var i = 0; i < temp1.length; i++) {
            document.forms[0].year.options[i] = new Option();
            document.forms[0].year.options[i].value = temp1[i];
            document.forms[0].year.options[i].text =  tempa[i];
            if(w==temp1[i]) {
                document.forms[0].year.options[i].selected=true;
            }
        }
    }
    //temp_clear();
}

function change_absent(absent_cov_late, amari_kuriage) {
    var absent_cov = document.forms[0].ABSENT_COV.value;
    if(absent_cov == 0) {
        document.forms[0].ABSENT_COV_LATE.value = "";
        document.forms[0].ABSENT_COV_LATE.disabled = true;
    }else{
        document.forms[0].ABSENT_COV_LATE.value = absent_cov_late;
        document.forms[0].ABSENT_COV_LATE.disabled = false;
    }

    if(absent_cov != 5) {
        document.forms[0].AMARI_KURIAGE.value = "";
        document.forms[0].AMARI_KURIAGE.disabled = true;
    }else{
        document.forms[0].AMARI_KURIAGE.value = amari_kuriage;
        document.forms[0].AMARI_KURIAGE.disabled = false;
    }
}
