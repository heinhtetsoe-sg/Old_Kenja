function btn_submit(cmd)
{
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }

    //取消確認
    if (cmd == 'subform6_clear') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }
    }

    if (cmd == 'subform6_update') {
        //登録日チェック
        var sdate   = document.forms[0].SDATE.value.split('/');
        var edate   = document.forms[0].EDATE.value.split('/');
        sdate_show  = document.forms[0].SDATE.value;
        edate_show  = document.forms[0].EDATE.value;

        var seqList = document.forms[0].SEQ_LIST.value.split(',');
        var err     = false;

        for (var i = 0; i < document.forms[0].elements.length; i++) {
            for (var j = 0; j < seqList.length; j++) {
                if (document.forms[0].elements[i].name == "TOROKU_DATE-"+seqList[j]) {
                    //必須チェック
                    if (document.forms[0].elements[i].value == "") {
                        err = true;
                    }

                    //日付範囲チェック
                    var date = document.forms[0].elements[i].value.split('/');
                    if (   (new Date(eval(sdate[0]), eval(sdate[1]) - 1, eval(sdate[2])) > new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2])))
                        || (new Date(eval(edate[0]), eval(edate[1]) - 1, eval(edate[2])) < new Date(eval(date[0]), eval(date[1]) - 1, eval(date[2]))))
                    {
                        err = true;
                    }
                }
            }
        }

        if (err == true) {
            alert('{rval MSG901}\n（登録日：' + sdate_show + '～' + edate_show + 'の範囲内）');
            return true;
        }
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

//受験方式コンボで、NAMESPARE1が立っているもの選択時のみ表示
function changeDispSh(obj)
{
    var shArr = new Array();
    var seq   = obj.name.split('-')[1];
    shArr     = document.forms[0].SH_ARR.value.split(',');

    for (var i=0; i < shArr.length; i++) {
        if (obj.value == shArr[i]) {
            document.getElementById("shDisp-" + seq).style.display = "block";
            return;
        } else {
            document.getElementById("shDisp-" + seq).style.display = "none";
        }
    }
    return;
}

//メッセージ表示
function issueControl(obj)
{
    var n   = obj.name.split('-');
    var org = eval("document.forms[0].ORIGINAL_ISSUE_" + n[1] + ".value");
    if (org == "1") {
        obj.checked = true;
    }
}

//スクロール
function scrollRC()
{
    document.getElementById('trow').scrollLeft = document.getElementById('tbody').scrollLeft;
    document.getElementById('tcol').scrollTop  = document.getElementById('tbody').scrollTop;
}

//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order)
{
    //必須チェック
    if (document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }

    //登録日チェック
    var sdate   = document.forms[0].SDATE.value.split('/');
    var edate   = document.forms[0].EDATE.value.split('/');
    sdate_show  = document.forms[0].SDATE.value;
    edate_show  = document.forms[0].EDATE.value;

    var seqList = document.forms[0].SEQ_LIST.value.split(',');
    var err     = false;

    for (var i = 0; i < document.forms[0].elements.length; i++) {
        for (var j = 0; j < seqList.length; j++) {
            if (document.forms[0].elements[i].name == "TOROKU_DATE-"+seqList[j]) {
                //必須チェック
                if (document.forms[0].elements[i].value == "") {
                    err = true;
                }

                //日付範囲チェック
                var date = document.forms[0].elements[i].value.split('/');
                if (   (new Date(eval(sdate[0]), eval(sdate[1]) - 1,eval(sdate[2])) > new Date(eval(date[0]), eval(date[1]) - 1,eval(date[2])))
                    || (new Date(eval(edate[0]), eval(edate[1]) - 1,eval(edate[2])) < new Date(eval(date[0]), eval(date[1]) - 1,eval(date[2]))))
                {
                    err = true;
                }
            }
        }
    }

    if (err == true) {
        alert('{rval MSG901}\n　　（登録日）');
        return true;
    }

    nextURL = "";

    for (var i = 0; i < parent.left_frame.document.links.length; i++) {
          var search = parent.left_frame.document.links[i].search;
          //searchの中身を&で分割し配列にする。
          arr = search.split("&");

          //学籍番号が一致
          if (arr[1] == "SCHREGNO="+schregno) {
            //昇順
            if (order == 0 && i == parent.left_frame.document.links.length-1) {
                idx = 0;                                           //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            } else if (order == 0) {
                idx = i + 1;                                       //更新後次の生徒へ
            } else if (order == 1 && i == 0) {
                idx = parent.left_frame.document.links.length-1;   //更新後前の生徒へ(データが最初の生徒の時)
            } else if (order == 1) {
                idx = i - 1;                                       //更新後前の生徒へ
            }
            nextURL = parent.left_frame.document.links[idx].href.replace("edit","subform6");    //上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = 'subform6_update';
    //クッキー書き込み
    saveCookie("nextURL", nextURL);
    document.forms[0].submit();
    return false;
}

function NextStudent(cd)
{
    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL) {
        if (cd == '0') {
            //クッキー削除
            deleteCookie("nextURL");
            document.location.replace(nextURL);

            alert('{rval MSG201}');
        } else if (cd == '1') {
            //クッキー削除
            deleteCookie("nextURL");
        }
    }
}
