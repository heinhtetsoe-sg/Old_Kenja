function btn_submit(cmd) {
    if (cmd == 'reset'){
        if (confirm("{rval MSG106}")){
            cmd = "form2";
        }else{
            return true;
        }    
    }

    if (cmd == 'subform3'){         //部活動参照
        loadwindow('knje010cindex.php?cmd=subform3',0,document.documentElement.scrollTop || document.body.scrollTop,650,350);
        return true;
    } else if (cmd == 'subform4'){  //委員会参照
        loadwindow('knje010cindex.php?cmd=subform4',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);
        return true;
    } else if (cmd == 'subform5'){  //委員会参照
        loadwindow('knje010cindex.php?cmd=subform5',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);
        return true;
    }

    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}
