function btn_submit(cmd) {
    //前年度からコピー
    if (cmd == "copy") {
        if (document.forms[0].PRE_YEAR_CNT.value <= 0) {
            alert('前年度のデータが存在しません。');
            return false;
        }
        if (document.forms[0].THIS_YEAR_CNT.value > 0) {
            if (!confirm('今年度のデータは破棄されます。コピーしてもよろしいですか？')) {
                return false;
            }
        } else {
            if (!confirm('{rval MSG101}')) {
                return false;
            }
        }
    }

    //取消
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) return false;
    }

    //更新
    if (cmd == 'update') {
        if (document.forms[0].use_school_detail_gcm_dat.value == '1' && document.forms[0].COURSE_MAJOR.value == '') {
            alert('{rval MSG304}\n　　　( 対象課程学科 )');
            return;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
