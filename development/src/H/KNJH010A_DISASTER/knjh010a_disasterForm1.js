function btn_submit(cmd) {
    if (cmd != 'reset' && document.forms[0].SCHREGNO.value == "") {
        alert('{rval MSG304}');
        return true;
    }
    if (cmd == 'delete') {
        if (!confirm('{rval MSG103}'))
            return false;
    }else if (cmd == 'reset') {
        if (!confirm('{rval MSG106}')) {
            return false;
        }else{
            document.forms[0].cmd.value = cmd;
            document.forms[0].submit();
            return false;
        }
    }

    //送信前に全て入力可の状態にしないとdisabledのパーツが送信されない。
    formElemet = document.forms[0];
    for (i = 0; i < formElemet.length; i++) {
        formElemet[i].disabled = false;
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
function Page_jumper(link) {
    parent.location.href=link;
}

//通学経路登録
function Page_jumper2(prg) {
    requestroot = document.forms[0].REQUESTROOT.value;
    schregno = document.forms[0].SCHREGNO.value;
    var schoolbus = document.forms[0].SCHOOL_BUS.value.split(",");

    if (schoolbus.indexOf(document.forms[0].HOWTOCOMMUTECD.value) != -1) {
        cmd = "subform3";
        haba    = 600;
        takasa  = 400;
    } else {
        cmd = "subform2";
        haba    = 700;
        takasa  = 500;
    }

    link = requestroot+"/H/KNJH010A_DISASTER/knjh010a_disasterindex.php?cmd="+cmd+"&SCHREGNOSUB="+schregno+"&PRG="+prg;
    loadwindow(link, 0, 200, haba, takasa);
}

function to_Integer(obj) {
    var checkString = obj.value;
    var newString = "";
    var count = 0;
    
    for (i = 0; i < checkString.length; i++) {
        ch = checkString.substring(i, i+1);
        if (ch >= "0" && ch <= "9") {
            newString += ch;
        }
    }
    if (checkString != newString) {
        alert("入力された値は不正な文字列です。\n数値を入力してください。");
        obj.value="";
        return false;
    }

    switch(obj.name) {
        case "BEDTIME":
        case "RISINGTIME":
        if((1 > obj.value || obj.value > 24) && (obj.value!="")) {
            alert("1から24の値を入力してください。");
            obj.value="";
            return false;
        }
    }
    
    return true;
}
//更新後次の生徒のリンクをクリックする
function updateNextStudent(schregno, order) {
   if (document.forms[0].SCHREGNO.value == "") {
       alert('{rval MSG304}');
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
                idx = 0;                                         //更新後次の生徒へ(データが最後の生徒の時、最初の生徒へ)
            }else if (order == 0) {
                idx = i+1;                                       //更新後次の生徒へ
            }else if (order == 1 && i == 0) {
                idx = parent.left_frame.document.links.length-1; //更新後前の生徒へ(データが最初の生徒の時)
            }else if (order == 1) {
                idx = i-1;                                       //更新後前の生徒へ
            }
            nextURL = parent.left_frame.document.links[idx].href;//上記の結果
            break;
        }
    }
    document.forms[0].cmd.value = 'update';
    //クッキー書き込み
    saveCookie("nextURL", nextURL);
    document.forms[0].submit();
    return false;
}

function NextStudent(cd) {
    var nextURL;
    nextURL = loadCookie("nextURL");
    if (nextURL) {
        if(cd == '0') {
                //クッキー削除
                deleteCookie("nextURL");
                document.location.replace(nextURL);
            alert('{rval MSG201}');
        }else if(cd == '1') {
                //クッキー削除
                deleteCookie("nextURL");

        }
    }
}

