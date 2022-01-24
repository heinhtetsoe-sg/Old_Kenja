function btn_submit(cmd, arg) {
    if (document.forms[0].SCHREGNO.value == ""){
        alert('{rval MSG304}');
        return true;
    }
    var subclass_selects = document.getElementsByClassName("subclass_select");
    for (var i = 0; i < subclass_selects.length; i++) {
        if (subclass_selects[i].value == '') {
            //console.log(subclass_selects[i]);
            //return true;
        }
    }

    if (cmd == 'reload'){               //成績参照
        return true;
    } else if (cmd == 'reset'){         //取り消し
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    } else if (cmd == "delete") {
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }
    //フレームロック機能（プロパティの値が1の時有効）
    if (document.forms[0].useFrameLock.value == "1") {
        //更新中の画面ロック(全フレーム)
        if (cmd == 'update') {
            updateFrameLocks();
        }
    }
    if (cmd == 'update') {
        //更新ボタン・・・読み込み中は、更新ボタンをグレー（押せないよう）にする。
        document.forms[0].btn_update.disabled = true;
//        document.forms[0].btn_up_next.disabled = true;
//        document.forms[0].btn_up_pre.disabled = true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//モデルでチェックすると KNJXEXP が変わったら面倒なので、
//javascriptで学年を取得する
window.onload = function () {
    if (document.forms[0].LEFT_GRADE.value == '') {
        left_grade = parent.left_frame.document.forms[0].GRADE.value;
        grade_class = new Array();
        grade_class = left_grade.split('-');
        window.location += '&GRADE=' + grade_class[0];
    }
};

//年度追加
function add() {
    var temp1 = new Array();
    var tempa = new Array();
    var v = document.forms[0].YEAR.length;
    var w = document.forms[0].year_add.value;

    if (w == "") {
        alert('{rval MSG901}\n数字を入力してください。');
        document.getElementById('year_add').focus();
        return false;
    }

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
    if (temp1.length > 0) {
        for (var i = 0; i < temp1.length; i++) {
            document.forms[0].YEAR.options[i] = new Option();
            document.forms[0].YEAR.options[i].value = temp1[i];
            document.forms[0].YEAR.options[i].text =  tempa[i];
            if (w == temp1[i]) {
                document.forms[0].YEAR.options[i].selected=true;
            }
        }
    }
    btn_submit('add_year');
}
function ClearList(OptionList, TitleName) {
    OptionList.length = 0;
}

