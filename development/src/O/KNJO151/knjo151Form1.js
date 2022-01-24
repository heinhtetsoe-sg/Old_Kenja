function btn_submit(cmd) {

    if (cmd == 'top_delete') {
        var flag;
        flag = "";
        for (var i = 0; i < document.forms[0].elements.length; i++) {
            var e = document.forms[0].elements[i];
            if (e.type == "checkbox" && e.checked && e.name == "DELCHK[]"){
                var val = e.value;
                if (val != ''){
                    flag = "on";
                }
            }
        }
        if (flag == ''){
            alert("チェックボックスが選択されておりません。");
            return;
        }
        if (confirm('{rval MSG103}')) {
        } else {
            return;
        }
    }

    //追加ボタン
    if (cmd == 'update'){
        param = document.forms[0].SCHREGNO.value;
        loadwindow('knjo151index.php?cmd=insert&cmdSub=insert&SCHREGNO='+param,0,0,600,450);
        return true;
    }

    //パターン更新ボタン
    if (cmd == 'update'){
        if (document.forms[0].PAYMENT_DATE.value == '' || document.forms[0].PAYMENT_MONEY.value == ''){
            alert('{rval MSG301}');
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function btn_submit2(cmd, dataCnt, dubble) {
    if(dubble != 0){
        alert('複数の連携データを1つの科目に割り当てることは出来ません。');
        return false;
    }else if(dataCnt != 0){
        if(!confirm('すでに作成されたデータが存在します。削除して取り込みなおしますがよろしいですか？')){
            return false;
        }
    }
    
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

var selectedRow = 0;
function selectRow() {
    if (event.srcElement.parentElement.rowIndex == null) {
        return;
    }
    list.rows[selectedRow].bgColor = "white";
    selectedRow = event.srcElement.parentElement.rowIndex;
    list.rows[selectedRow].bgColor = "#ccffcc";

    var data = document.forms[0]["ROWDATA"];
    var rowData = data.value.split(",");
    
    parent.edit_frame.location.href = "knjo151index.php?cmd=edit_select&DATAROW="+rowData[selectedRow];

    
    /*var chk = document.forms[0]["DELCHK[]"];
    var chkData = chk[selectedRow] === undefined ? chk.value.split(":") : chk[selectedRow].value.split(":");
    if (chkData[2] != "1") {
        alert('前籍校データのみ修正可能です。');
        return;
    }
    if (chk.length){
        parent.edit_frame.location.href = "knjo151index.php?cmd=edit_select&CHECKED="+chk[selectedRow].value;
    }else if (chk){
        parent.edit_frame.location.href = "knjo151index.php?cmd=edit_select&CHECKED="+chk.value;
    }*/
}

function check_all(obj) {
    for (var i=0; i < document.forms[0].elements.length; i++) {
        if (document.forms[0].elements[i].name == "DELCHK[]" && !document.forms[0].elements[i].disabled) {
            document.forms[0].elements[i].checked = obj.checked;
        }
    }
}

