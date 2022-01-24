//サブミット
function btn_submit(cmd) {
    //割り振り処理
    if (cmd == 'execute') {
        var hr = document.forms[0].HR_CLASS_LIST.value.split(',');
        var cs = document.forms[0].COURSE_LIST.value.split(',');
        var cn = document.forms[0].COURSE_NAME_LIST.value.split(',');

        var hr_cnt = [];
        var tg = [];

        //コース一覧
        courseFlg = false;
        for (var c=0; c < cs.length; c++) {
            //クラス数が入力されているコースが処理対象
            if (document.forms[0]['CLASS_CNT:'+cs[c]].value > 0) {
                courseFlg = true;

                //必須チェック
                if (document.forms[0]['START_CLASS:'+cs[c]].value == "") {
                    alert('{rval MSG310}' + '\n（開始クラス）');
                    return false;
                }

                cnt = 0;
                startFlg = false;
                hr_cnt[cs[c]] = [];
                tg[cs[c]] = [];

                //クラス一覧
                for (var h=0; h < hr.length; h++) {
                    //開始クラス
                    if (document.forms[0]['START_CLASS:'+cs[c]].value == hr[h]) {
                        startFlg = true;
                    }

                    if (startFlg == true) {
                        //割り振り可能クラス数
                        hr_cnt[cs[c]].push(hr[h]);

                        //対象クラス
                        if (cnt < document.forms[0]['CLASS_CNT:'+cs[c]].value) {
                            tg[cs[c]].push(hr[h]);
                            cnt++;
                        }
                    }
                }

                //クラス数チェック
                if (hr_cnt[cs[c]].length < document.forms[0]['CLASS_CNT:'+cs[c]].value) {
                    alert('{rval MSG915}' + '（' + cn[c] + '）\nクラス数には' + hr_cnt[cs[c]].length + '以下を指定してください。');
                    return false;
                }

                //対象クラスをセット
                target_class = '';
                sep = '';
                for (var i = 0; i < tg[cs[c]].length; i++) {
                    target_class += sep + tg[cs[c]][i];
                    sep = ',';
                }
                document.forms[0]['TARGET_CLASS:'+cs[c]].value = target_class;
            }
        }

        //必須チェック（クラス数）
        if (courseFlg == false) {
            alert('クラス数が入力されていません。');
            return false;
        }

        //処理の確認
        if (!confirm('{rval MSG101}')) {
            return false;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//権限チェック
function OnAuthError() {
    alert('{rval MSG300}');
    closeWin();
}

//最終学期チェック
function OnMaxSemError(){
    alert('{rval MSG311}');
    closeWin();
}
