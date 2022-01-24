function btn_submit(cmd)
{
    var str = new Object();

    if(cmd == 'reset' && !confirm('{rval MSG106}'))  return true;
    if(cmd == 'read' || cmd == 'next' || cmd == 'back' || cmd == 'update' || cmd == 'reset') {
        if (document.forms[0].EXAMNO.value == '' || eval(document.forms[0].EXAMNO.value) == 0) {
            alert('{rval MSG901}' + '\n 受験番号には 1 以上を入力してください');
            return false;
        }
    }

    if (cmd == 'read' || cmd == 'next' || cmd == 'back') {
        if (document.all("SEX[]") != null && change_flg && !confirm('{rval MSG108}')) {
            return false;
        }
    }
    if (cmd == 'update' && document.all("SEX[]") == null) {
        return false;
    }
    if (cmd == 'update'){
        var flg = false;
        for (var i = 0; i < document.forms[0].elements.length; i++){
            var el = document.forms[0].elements[i];
            var v = document.forms[0].elements[i].value;
            if (el.name == "SEX[]" && v != "" && typeof sex_name[v] == "undefined"){
                document.forms[0].elements[i].style.background="red";
                flg = true;
            }else if (el.name == "FS_NATPUBPRIDIV[]" && v != "" && typeof fs_natpub_name[v] == "undefined"){
                document.forms[0].elements[i].style.background="red";
                flg = true;
            }else if (el.name == "FS_CD[]" && v != "" && typeof fs_name[v] == "undefined"){
                if (v.length < 7){
                    var setval = '000000' + v;
                    v = setval.substr((v.length - 1), 7);
                    el.value = v;
                }
                if (typeof fs_name[v] == "undefined"){
                    document.forms[0].elements[i].style.background="red";
                    flg = true;
                }
            }else{
                continue;
            }
        }
        if (flg){
            alert('{rval MSG901}');
            return false;
        }
    }
    if (cmd == 'update' && !confirm('{rval MSG102}' + '\n\n注意！！\n\n出身学校と国公私立と推薦受験番号の項目以外は必須入力です。\n\n未入力の場合は削除され登録されません。\n\n')) {
        return false;
    }
     document.forms[0].cmd.value = cmd;
     document.forms[0].submit();
     return false;
}

function showConfirm()
{
    if(confirm('{rval MSG106}')) return true;
    return false;
}

function setName(obj, rowid, flg)
{
    obj.style.background="";
    Setflg(obj);
    
    var idx = obj.value;
    if (obj.value == '') {
        if (flg == '0') {
            outputLAYER('SEX_NAME' + rowid, '');
        }else if (flg == '2') {
            outputLAYER('FS_NATPUB_NAME' + rowid, '');
        }else if (flg == '5') {
            outputLAYER('FS_NAME' + rowid, '');
        }
        return;
    }
    if (flg == '0') {
        if (typeof sex_name[idx] != "undefined") {
            outputLAYER('SEX_NAME' + rowid, sex_name[idx]);
        } else {
            alert('{rval MSG901}');
            outputLAYER('SEX_NAME' + rowid, '');
            obj.value = '';
        }
   }else if (flg == '2') {
        if (typeof fs_natpub_name[idx] != "undefined") {
            outputLAYER('FS_NATPUB_NAME' + rowid, fs_natpub_name[idx]);
        } else {
            alert('{rval MSG901}');
            outputLAYER('FS_NATPUB_NAME' + rowid, '');
            obj.value = '';
        }
   }else if (flg == '5') {
        if (idx.length < 7){
            var setval = '000000' + idx;
            idx = setval.substr((idx.length - 1), 7);
            window.status = idx;
            obj.value = idx;
        }
        if (typeof fs_name[idx] != "undefined") {
            outputLAYER('FS_NAME' + rowid, fs_name[idx]);
        } else {
            alert('{rval MSG901}');
            outputLAYER('FS_NAME' + rowid, '');
            obj.value = '';
        }
    }
}

function Setflg(obj)
{
    if (obj.value != obj.defaultValue || obj.type == 'checkbox'){
        change_flg = true;
        document.forms[0].HID_APPLICANTDIV.value = document.forms[0].APPLICANTDIV.options[document.forms[0].APPLICANTDIV.selectedIndex].value;
        document.forms[0].APPLICANTDIV.disabled = true;
        if (obj.id){
            if (obj.type == 'checkbox') {
                var n = obj.id.split('-');
                if (n[2] == '1') {
                    obj.style.background="yellow";
                    document.getElementById('ROWID' + n[1]).style.background="yellow";
                }
            } else {
                obj.style.background="yellow";
                document.getElementById('ROWID' + obj.id).style.background="yellow";
            }
        }
    }
}
