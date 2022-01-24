function btn_submit(cmd) {
    if (cmd == 'reset'){
        result = confirm('{rval MSG106}');
        if (result == false) {
            return false;
        }
    }
    if (cmd == 'update' || cmd == 'shokitiyear' || cmd == 'shokitimonth' || cmd == 'sch_shokiti') {
        if (document.forms[0].GRADE_HR.value == "") {
            alert('年組を指定して下さい。');
            return false;
        }
        if (document.forms[0].YEAR.value == "") {
            alert('年度を指定して下さい。');
            return false;
        }
        if (cmd == 'shokitiyear'){
            //EVENT_DATデータチェック(年度）
            if (document.forms[0].GETEVENT.value > 0 ) {
                if (!confirm('{rval MSG104}')) {
                    return false;
                }
            }
        }
        if (cmd == 'shokitimonth'){
            //EVENT_DATデータチェック(指定月）
            if (document.forms[0].GETEVENTMONTH.value > 0 ) {
                if (!confirm('{rval MSG104}')) {
                    return false;
                }
            }
        }
        if ((cmd == 'update' || cmd == 'sch_shokiti') && document.forms[0].use_visitor.value == true) {
            if (document.forms[0].VISITOR.value == "") {
                alert('訪問生を指定して下さい。');
                return false;
            }

            if (cmd == 'sch_shokiti') {
                //EVENT_SCHREG_DATデータチェック(指定月）
                if (document.forms[0].GETEVENTSCHREGMONTH.value > 0 ) {
                    if (!confirm('{rval MSG104}')) {
                        return false;
                    }
                }
            }
        }

        if (!confirm('{rval MSG102}')) {
            return false;
        }
    }

    //学年別一括反映
    if (cmd == 'batch') {
        if (!confirm('{rval MSG108}')) {
            return false;
        }
        document.forms[0].batch.value = "1";
    }
    //学年別一括反映解除
    if (cmd == 'back') {
        document.forms[0].batch.value = "";
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
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
