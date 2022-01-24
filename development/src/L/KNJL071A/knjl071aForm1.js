function btn_submit(cmd) {
    //取消
    if (cmd == 'reset' && !confirm('{rval MSG106}')) return true;

    //更新
    if (cmd == "update" && document.forms[0].HID_RECEPTNO.value.length == 0) {
        alert('更新データがありません。');
        return false;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//事前チェックエラー
function errorPreCheck() {
    alert('{rval MSG305}'+'\n特待区分が設定されていません。');
}

//値チェック(特待区分)
function checkValueHonordiv(obj, receptno) {
    var valArray = document.forms[0].HONORDIV_LIST.value.split(',');
    var clubFlgArray = document.forms[0].HONORDIV_CLUB_FLG_LIST.value.split(',');

    var honordivObjectArray = [
        document.forms[0]["JIZEN_TOKUTAI-" + receptno],
        document.forms[0]["TOKUTAI_SINSEI-" + receptno],
        document.forms[0]["SIKAKU_KATSUYO-" + receptno]
    ];

    //クラブコンボ
    //特待区分がクラブ特待(CLUB_FLG=1)の時のみ活性化する。
    disFlg = true;
    for (var j = 0; j < honordivObjectArray.length; j++) {
        honordivObject = honordivObjectArray[j];

        for (var i = 0; i < valArray.length; i++) {
            if (honordivObject.value == valArray[i] && clubFlgArray[i] == '1') {
                disFlg = false;
            }
        }
    }
    document.forms[0]["CLUB_CD-"+receptno].disabled = disFlg;
}

function Setflg(obj, receptno) {
    //背景色
    document.getElementById("ROWID-" + receptno).style.backgroundColor = "yellow";
    var hidReceptno = document.forms[0].HID_RECEPTNO;
    if (hidReceptno.value == '') {
        hidReceptno.value = receptno;
    } else {
        hidReceptno.value += ',' + receptno;
    }
}

function check(obj) {
    if (getByte(obj.value) > 20) {
        alert("全角１０、半角２０文字以内で入力してください。");
        obj.focus();
    }
}

// Enterキーが押されたときに「TABキーが押された」イベントにするメソッド
function keyChangeEntToTab2(obj, setTextField, cnt) {
    //受験番号リスト
    var receptnoArray = document.forms[0].HID_RECEPTNO.value.split(",");
    //移動可能なオブジェクト
    var textFieldArray = setTextField.split(",");
    //行数
    var lineCnt = document.forms[0].COUNT.value;
    //1行目の生徒
    var isFirstStudent = cnt == 0 ? true : false;
    //最終行の生徒
    // var isLastStudent = cnt == lineCnt - 1 ? true : false;
    var isLastStudent = cnt == receptnoArray[lineCnt - 1] ? true : false;

    // Ent13 Tab9 ←37 ↑38 →39 ↓40
    var e = window.event;
    //方向キー
    var moveEnt = e.keyCode;
    // if (e.keyCode != 13 || e.keyCode != 9) {
    if (e.keyCode != 13) {
        return;
    }
    // var moveEnt = 40;
    for (var i = 0; i < textFieldArray.length; i++) {
        if (textFieldArray[i] + cnt == obj.name) {
            var isFirstItem = i == 0 ? true : false;
            var isLastItem = i == textFieldArray.length - 1 ? true : false;
            if (moveEnt == 37) {
                if (isFirstItem && isFirstStudent) {
                    obj.focus();
                    return;
                }
                if (isFirstItem) {
                    targetname = textFieldArray[(textFieldArray.length - 1)] + (cnt - 1);
                    document.forms[0].elements[targetname].focus();
                    return;
                }
                targetname = textFieldArray[(i - 1)] + cnt;
                document.forms[0].elements[targetname].focus();
                return;
            }
            if (moveEnt == 38) {
                if (isFirstStudent) {
                    obj.focus();
                    return;
                }
                targetname = textFieldArray[i] + (cnt - 1);
                document.forms[0].elements[targetname].focus();
                return;
            }
            if (moveEnt == 39 || moveEnt == 13) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                if (isLastItem) {
                    for (var j = 0; j < receptnoArray.length; j++) {
                        if (receptnoArray[j] == cnt) {
                            cnt = receptnoArray[j+1];
                            break;
                        }
                    }

                    // targetname = textFieldArray[0] + (cnt + 1);
                    targetname = textFieldArray[0] + (cnt);
                    document.forms[0].elements[targetname].focus();
                    return;
                }

                targetname = textFieldArray[(i + 1)] + cnt;

                // disableの場合
                if (document.forms[0].elements[targetname].disabled) {
                    targetname = textFieldArray[(i + 2)] + cnt;
                    document.forms[0].elements[targetname].focus();
                    return;
                }

                document.forms[0].elements[targetname].focus();
                return;
            }
            if (moveEnt == 40) {
                if (isLastItem && isLastStudent) {
                    obj.focus();
                    return;
                }
                if (isLastStudent) {
                    targetname = textFieldArray[(i + 1)] + 0;
                    document.forms[0].elements[targetname].focus();
                    return;
                }
                targetname = textFieldArray[i] + (cnt + 1);
                document.forms[0].elements[targetname].focus();
                return;
            }
        }
    }
}
