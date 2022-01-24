function btn_submit(cmd) {
    //コピー確認メッセージ
    if (cmd == 'copy' && !confirm('{rval MSG101}')) {
        return false;
    }

    //取消確認メッセージ
    if (cmd == 'clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        } else {
            cmd = "";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//更新
function doSubmit(left_part, right_part, cmd) {
    //必須チェック
    
    if (document.forms[0].PRG_ID.value == '') {
        alert('{rval MSG304}\n　　　( 対象プログラム )');
        return;
    }
    var attribute1 = document.forms[0].selectdata1;
    attribute1.value = "";
    if (document.forms[0][left_part].length == 0 && document.forms[0][right_part].length == 0) {
        alert('{rval MSG916}');
        return false;
    }
    sep = "";
    for (var i = 0; i < document.forms[0][left_part].length; i++) {
        attribute1.value = attribute1.value + sep + document.forms[0][left_part].options[i].value;
        sep = ",";
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//リスト移動
function ClearList(OptionList) {
    OptionList.length = 0;
}
function moveList(side, left, right, sort) {
    var temp1 = new Array();
    var temp2 = new Array();
    var tempa = new Array();
    var tempb = new Array();
    var a = new Array();
    var current1 = 0;
    var current2 = 0;
    var y=0;
    var attribute1;
    var attribute2;
    //assign what select attribute treat as attribute1 and attribute2
    if (side == "right" || side == "sel_del_all") {
        attribute1 = document.forms[0][left];
        attribute2 = document.forms[0][right];
    } else {
        attribute1 = document.forms[0][right];
        attribute2 = document.forms[0][left];
    }

    //fill an array with old values
    for (var i = 0; i < attribute2.length; i++) {
        y=current1++
        temp1[y] = attribute2.options[i].value;
        tempa[y] = attribute2.options[i].text;
        if (sort == "val") {
            a[temp1[y]] = tempa[y];
        } else if (sort == "txt") {
            a[tempa[y]] = temp1[y];
        }
    }

    //assign new values to arrays
    for (var i = 0; i < attribute1.length; i++) {
        if (side == "right" || side == "left") {
            if (attribute1.options[i].selected) {
                y=current1++
                temp1[y] = attribute1.options[i].value;
                tempa[y] = attribute1.options[i].text;
                if (sort == "val") {
                    a[temp1[y]] = tempa[y];
                } else if (sort == "txt") {
                    a[tempa[y]] = temp1[y];
                }
            } else {
                y=current2++
                temp2[y] = attribute1.options[i].value;
                tempb[y] = attribute1.options[i].text;
            }
        } else {
            y=current1++
            temp1[y] = attribute1.options[i].value;
            tempa[y] = attribute1.options[i].text;
            if (sort == "val") {
                a[temp1[y]] = tempa[y];
            } else if (sort == "txt") {
                a[tempa[y]] = temp1[y];
            }
        }
    }
    if (sort) {
        if (sort == "val") {
            //sort
            temp1 = temp1.sort();
            for (var i = 0; i < temp1.length; i++) {
                tempa[i] = a[temp1[i]];
            }
        } else if (sort == "txt") {
            //sort
            tempa = tempa.sort();
            for (var i = 0; i < tempa.length; i++) {
                temp1[i] = a[tempa[i]];
            }
        }
    }

    //generating new options
    for (var i = 0; i < temp1.length; i++) {
        attribute2.options[i] = new Option();
        attribute2.options[i].value = temp1[i];
        attribute2.options[i].text =  tempa[i];
    }

    //generating new options
    ClearList(attribute1);

    if (temp2.length>0) {
        for (var i = 0; i < temp2.length; i++) {
            attribute1.options[i] = new Option();
            attribute1.options[i].value = temp2[i];
            attribute1.options[i].text =  tempb[i];
        }
    }

    attribute3 = document.forms[0].selectdata1;
    attribute3.value = "";
    sep = "";
    for (var i = 0; i < document.forms[0][left].length; i++) {
        attribute3.value = attribute3.value + sep + document.forms[0][left].options[i].value;
        sep = ",";
    }

}
