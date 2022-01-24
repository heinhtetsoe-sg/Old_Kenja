function btn_submit(cmd){
    if(cmd == 'list'){
        parent.right_frame.location.href='knjz040kindex.php?cmd=edit&year='+document.forms[0].year.value+
                                         '&COURSECD=""&MAJORCD=""&EXAMCOURSECD=""&SHDIV=""&JUDGEMENT=""'+
                                         '&CMP_COURSECD=""&CMP_MAJORCD=""&CMP_EXAMCOURSECD=""';
    }
    if(cmd == 'copy'){
        var value = eval(document.forms[0].year.value) + 1;
        var message = document.forms[0].year.value + '年度のデータから、' + value + '年度に存在しないデータのみコピーします。\n\n注意：'+ value + '年度の受験コースマスタと名称マスタを事前に作成しておいて下さい。';
        if (!confirm('{rval MSG101}\n\n' + message)) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
