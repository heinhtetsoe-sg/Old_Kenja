//サブミット
function btn_submit(cmd) {
    if (cmd == 'update' && !confirm('{rval MSG101}' + '\n本締めの実行後は、締めた年度のデータは修正できません。')){
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
