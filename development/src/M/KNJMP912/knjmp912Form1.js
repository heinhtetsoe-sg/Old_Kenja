//サブミット
function btn_submit(cmd) {
    if (cmd == 'update' && !confirm('{rval MSG101}' + '\nすでに返金実行済の伝票は対象になりません。' + '\n※返金実行後は対象伝票の決裁のキャンセルはできません。')){
        return false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
