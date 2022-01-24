function btn_submit(cmd) {
    var typechk_0_flg;
    var tyouhyou_pattern;
    typechk_0_flg = document.forms[0].TYPECHK_0_FLG.value;
    tyouhyou_pattern = document.forms[0].TYOUHYOU_PATTERN[1].checked ? true : false;

    //取消
    if (cmd == "clear") {
        if (!confirm('{rval MSG106}')) {
            return false;
        } 
    }

    //削除
    if (cmd == "delete") {
        if (typechk_0_flg == "1" && !tyouhyou_pattern) {
            $cutstr = document.forms[0].COMPOSITION_TYPE.value.split('-');
            if ($cutstr[0] == "02") {
                alert("変更可能な項目はありません。");
                return false;
            }
        }
        if (!confirm('{rval MSG103}')) {
            return false;
        }
    }

    //更新
    if (cmd == "update") {
        if (typechk_0_flg == "1") {
            if (!tyouhyou_pattern) {
                $cutstr = document.forms[0].COMPOSITION_TYPE.value.split('-');
                if ($cutstr[0] == "02") {
                    alert("変更可能な項目はありません。");
                    return false;
                }
            }
        } else {
            if (document.forms[0].COMPOSITION_NAME.value.length > 15) {
                alert('{rval MSG901}' + "構成項目名の入力文字数は15文字までです。");
                return false;
            }
            //確定した項目数で処理
            $cutstr = document.forms[0].COMPOSITION_TYPE.value.split('-');
            if ($cutstr[0] != "08") {
                if ($cutstr[1] != "" && $cutstr[0] != "10") {
                    if (document.forms[0].COMPVAL.value != document.forms[0].COMPCNT.value) {
                        document.forms[0].COMPCNT.value = document.forms[0].COMPVAL.value;
                    }
                }
            }

            //updateするなら、文字色をクリアする。
            document.forms[0].COMPOSITIONNAME_FLG.value = "0";
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//項目数チェック
function level(cnt) {
    var level;
    level = document.forms[0].COMPCNT.value;
    if (level > 0) {
    } else {
        alert('{rval MSG913}'+'\n　　　　　　　　　( 段階数：1～10 )');
        document.forms[0].COMPCNT.focus();
        return false;
    }

    if (level == cnt) {
        return false;
    }
    if (Number(level) > Number(document.forms[0].DEFSEL_CMBCNT.value)) {
        alert('{rval MSG913}'+'\n項目数は'+document.forms[0].DEFSEL_CMBCNT.value+'を超えてはいけません。');
        document.forms[0].COMPCNT.focus();
        return false;
    }

    document.forms[0].cmd.value = 'level';
    document.forms[0].submit();
    return false;
}

function resetcolor(obj) {
    obj.style.color = "black";
    document.forms[0].COMPOSITIONNAME_FLG.value = "0";
}

function resetAssessLevelcolor(obj,i) {
    obj.style.color = "black";
    switch (i.value) {
    case "1":
        document.forms[0].ASSESSLEVEL_INFLG_1.value = "0";
        break;
    case "2":
        document.forms[0].ASSESSLEVEL_INFLG_2.value = "0";
        break;
    case "3":
        document.forms[0].ASSESSLEVEL_INFLG_3.value = "0";
        break;
    case "4":
        document.forms[0].ASSESSLEVEL_INFLG_4.value = "0";
        break;
    case "5":
        document.forms[0].ASSESSLEVEL_INFLG_5.value = "0";
        break;
    case "6":
        document.forms[0].ASSESSLEVEL_INFLG_6.value = "0";
        break;
    case "7":
        document.forms[0].ASSESSLEVEL_INFLG_7.value = "0";
        break;
    case "8":
        document.forms[0].ASSESSLEVEL_INFLG_8.value = "0";
        break;
    default:
        break;
    }
}

//印刷
function newwin(SERVLET_URL) {
    //必須チェック
    if (document.forms[0].CONDITION.value == "") {
        alert('{rval MSG301}\n（状態区分）');
        return true;
    }
    if (document.forms[0].GUIDANCE_PATTERN.value == "") {
        alert('{rval MSG301}\n（指導計画帳票パターン）');
        return true;
    }

    action = document.forms[0].action;
    target = document.forms[0].target;

//    url = location.hostname;
//    document.forms[0].action = "http://" + url +"/cgi-bin/printenv.pl";
    document.forms[0].action = SERVLET_URL +"/KNJD";
    document.forms[0].target = "_blank";
    document.forms[0].submit();

    document.forms[0].action = action;
    document.forms[0].target = target;
}
