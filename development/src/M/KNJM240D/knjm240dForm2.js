function btn_submit(cmd) {
    if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        } else {
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function check(obj, semes) {
    var repoMaxCnt = (document.forms[0]["REPO_MAX_CNT-" + semes].value != "") ? Number(document.forms[0]["REPO_MAX_CNT-" + semes].value): null;
    var repoLimCnt = (document.forms[0]["REPO_LIMIT_CNT-" + semes].value != "") ? Number(document.forms[0]["REPO_LIMIT_CNT-" + semes].value): null;
    var lingMaxCnt = (document.forms[0]["SCHOOLING_MAX_CNT-" + semes].value != "") ? Number(document.forms[0]["SCHOOLING_MAX_CNT-" + semes].value): null;
    var lingLimCnt = (document.forms[0]["SCHOOLING_LIMIT_CNT-" + semes].value != "") ? Number(document.forms[0]["SCHOOLING_LIMIT_CNT-" + semes].value): null;

    if (repoMaxCnt != null && repoLimCnt != null) {
        if (repoMaxCnt < repoLimCnt) {
            alert('\t回数より最低提出回数の値が大きいです。\n\t(レポート)');
            obj.value = '';
            obj.focus();
        }
    }
    if (lingMaxCnt != null && lingLimCnt != null) {
        if (lingMaxCnt < lingLimCnt) {
            alert('\t回数より最低出席回数の値が大きいです。\n\t(スクーリング)');
            obj.value = '';
            obj.focus();
        }
    }

    return false;
}
//エンターキーをTabに変換
function changeEnterToTab(obj) {
    // Ent13
    var e = window.event;
    if (e.keyCode != 13) {
        return;
    }

    for (var f = 0; f < document.forms[0].length; f++) {
        if (document.forms[0][f].name == obj.name) {
            var targetObject = document.forms[0][(f + 1)];
            if (targetObject.type != 'text') {//テキスト以外はフォーカスしない
                return;
            }
            targetObject.focus();
            return;
        }
    }
    return;
}
