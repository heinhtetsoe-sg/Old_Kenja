function btn_submit(cmd){
    if(cmd == 'list'){
        parent.right_frame.location.href='knjz020kindex.php?cmd=edit&year='+document.forms[0].year.value+'&COURSE='+document.forms[0].COURSE.value+'&TESTDIV=""&TESTSUBCLASSCD=""';
    }
    if(cmd == 'copy'){
        var value = eval(document.forms[0].year.value) + 1;
        var message = document.forms[0].year.value + '年度のデータから、' + value + '年度に存在しないデータのみコピーします。\n\n注意：'+ value + '年度の受験コースマスタと名称マスタを事前に作成しておいて下さい。';
        if (!confirm('{rval MSG101}\n\n' + message)) {
//            alert('{rval MSG203}');
            return false;
        }
    }
    if(cmd == "delete"){
        result = confirm('{rval MSG103}');
        if(result == false){
            return false;
        }
    }
    if(cmd == "ado"){
        loadwindow('knjz020kindex.php?cmd=ado&COURSE='+document.forms[0].COURSE.value+'',30,280,350,210);
        return true;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function Btn_reset(cmd) {
    result = confirm('{rval MSG106}');
    if (result == false) {
        return false;
    }
}
