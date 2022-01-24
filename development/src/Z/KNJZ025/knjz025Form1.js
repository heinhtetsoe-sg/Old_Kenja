function btn_submit(cmd) {
    if (document.forms[0].year.value == '') {
        alert('{rval MSG304}' + ' （対象年度）');
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//取消確認
function showConfirm() {
    if(confirm('{rval MSG107}')) {
        document.forms[0].cmd.value = "main";
        document.forms[0].submit();
    }
    return false;
}

//画面を閉じる（権限・事前設定なし）
function closing_window(flg) {
    if (flg == 1) {
        alert('{rval MSG300}');
    }
    if (flg == 2) {
        alert('{rval MSG305}' + '\n(コントロールマスタ)');
    }
    closeWin();
    return true;
}

//年度新規追加
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
}
