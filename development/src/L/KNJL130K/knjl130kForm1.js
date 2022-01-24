function btn_submit(cmd) 
{
    if (cmd == 'reset' && !confirm('{rval MSG106}')) {
        return true;
    }
    if (cmd == 'reference' || cmd == 'back1' || cmd == 'next1') {
        if (document.forms[0].EXAMNO.value == '') {
            alert('{rval MSG301}\n( 受験番号 )');
            return true;
        }
        cflg = document.forms[0].cflg.value;
        if (vflg == true || cflg == 'true') {
            if (!confirm('{rval MSG108}')) {
                return true;
            }
        }
    }

    if (cmd == 'change' || cmd == 'change_judge') {
        document.forms[0].cflg.value = 'true';
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}


//ボタンを押し不可にする
function btn_disabled() 
{
    document.forms[0].btn_udpate.disabled = true;
    document.forms[0].btn_up_pre.disabled = true;
    document.forms[0].btn_up_next.disabled = true;
}

//入力された値が正しいかチェックする
function toCheck(obj, jname)
{
    var str = obj.value;

    if (str == "") {
        return false;
    }

    //小文字を大文字に変換する
    obj.value = str.toUpperCase();

    var num = jname.split(",");
    for (i = 0; i < num.length; i++) {
        if (obj.value == num[i]) {
            return false;
        }
    }
    alert('{rval MSG901}\n正しい値を入力して下さい。');
    obj.value = "";
    return false;
}

function all_update_view()
{
    SearchDialog.forms = document.forms[0];
    SearchDialog.src = "{rval REQUESTROOT}/L/KNJL130K/knjl130kindex.php?cmd=open";
    var url = "{rval REQUESTROOT}/common/iframe.php";
    showModalDialog(url, SearchDialog,"status:no;help:no;dialogWidth:560px;dialogHeight:300px;dialogTop:230px;dialogLeft:440px;")
    return;    // user canceled search
}

function SearchDialog() {
    var src;
    var forms;
}

//フォームの値が変更されたか判断する
function change_flg() {
    vflg = true;
}
