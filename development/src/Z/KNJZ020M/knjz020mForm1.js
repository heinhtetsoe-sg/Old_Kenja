function btn_submit(cmd) {
    var perfect        = new Array();
    var detail_perfect = new Array();
    var subclass_cd = new Array();
    var subclass_paper_cd = new Array();
    var total = 0;
    if (cmd == 'update') {
        for (var i = 0; i < document.forms[0].length; i++) {
            if (document.forms[0][i].name.match(/^PERFECT/)) {
                subclass_cd = document.forms[0][i].name.split("_");
                perfect[subclass_cd[1]] = document.forms[0][i];
                if (document.forms[0][i].value == "") {
                    alert('{rval MSG301}');
                    document.forms[0][i].focus();
                    return false;
                }
            }
            if (document.forms[0][i].name.match(/^RATE/)) {
                if (document.forms[0][i].value == "") {
                    alert('{rval MSG301}');
                    document.forms[0][i].focus();
                    return false;
                }
            }
            if (document.forms[0][i].name.match(/^DETAIL_PERFECT/)) {
                subclass_paper_cd = document.forms[0][i].name.split("_");
                if (typeof(detail_perfect[subclass_paper_cd[2]]) == "undefined") {
                    detail_perfect[subclass_paper_cd[2]] = new Array();
                }
                detail_perfect[subclass_paper_cd[2]][subclass_paper_cd[3]] = document.forms[0][i];
                if (document.forms[0][i].value == "") {
                    alert('{rval MSG301}');
                    document.forms[0][i].focus();
                    return false;
                }
            }
        }

        for (perfect_cnt = 1; perfect_cnt < perfect.length; perfect_cnt++) {
            total = 0;
            for (detail_cnt = 1; detail_cnt < detail_perfect[perfect_cnt].length; detail_cnt++) {
                total += parseInt(detail_perfect[perfect_cnt][detail_cnt].value);
            }
            if (total != perfect[perfect_cnt].value) {
                alert('満点の合計があいません');
                perfect[perfect_cnt].focus();
                return false;
            }
        }
    }
    if (cmd == 'delete'){
        if (!confirm('{rval MSG103}'))
            return false;
    }
    if (cmd == 'reset'){
        if (!confirm('{rval MSG106}'))
            return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

