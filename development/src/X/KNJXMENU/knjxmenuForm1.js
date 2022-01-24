var newWin;
window.onload = function(){
}

function chengeCss(pid, cid){
    var bpid = document.left.nowpid;
    var bcid = document.left.nowcid;

    if(bpid.value == ""){
        document.getElementById('TOP').className="lefttop";
    }else if(bpid.value == "TOP"){
        document.getElementById('TOP').className="lefttop";
    }else{
        document.getElementById(bpid.value).className="";
        document.getElementById(bcid.value).className="";
    }
    if(pid != 'TOP'){
        document.getElementById(pid).className="choice";
    }else{
        document.getElementById(pid).className="lefttopchoice";
    }
    if(cid != ''){
        document.getElementById(cid).className="active";
    }

    if(pid != 'TOP'){
        bpid.value = pid;
        bcid.value = cid;
        
    } else {
        //TOPÇæÇ¡ÇΩÇÁécÇ≥Ç»Ç¢Ç≈ÉäÉçÅ[ÉhÇµÇƒÇ›ÇÈ
        bpid.value = "";
        bcid.value = "";
        document.left.cmd.value = "retree";
        document.left.submit();
    }
    
}

function changeRadio(id, nextid)
{
    if(id == 'TOP'){
        return;
    }
    var check = document.getElementById('r'+id);
    var hide = document.left.radiovalue.value;
    
    if(hide != id){
        check.checked = true;
        document.left.radiovalue.value = id;
        var cid = id + '0';
        chengeCss(id,cid);
        
        if(id != 'TOOL'){
            window.open('index.php?cmd=main&MENUID='+nextid,'right_frame');
        }else{
            window.open('index.php?cmd=chg_pwd&MENUID='+nextid,'right_frame');
        }
    }else{
        check.checked = false;
        document.left.radiovalue.value = '';
    }
    
}
