function btn_submit(cmd)
{
    if (cmd == 'update' || cmd == 'confirm') {
        if(!checkHidden('on'))      return false;
        if (!checkOutPutCtrlDate()) return false;
        if(mltiCtrl('','check')){
            detailopen('change');
            return false;
        }
        if (cmd == 'update' &&
            document.forms[0].showDialog.value != "0" &&
            document.forms[0].btnShukketsu.value == "1"
        ) {
            show();
            return false;
        }
    }
    document.forms[0].cmd.value = cmd;
    document.forms[0].submit();
    return false;
}

function show() {
    $('#dialogdemo1').dialog('open');
    return false;
}

function jumpClick() {
    document.forms[0].btn_jump.onclick();
    return false;
}

function closing_window(no)
{
    var msg;
    if(no == 'year'){
        msg = '{rval MSG305}';
    }else if(no == 'cm'){
        msg = '{rval MSG300}';
    }else if(no == 'sf'){
        msg = '職員データを取得できませんでした。';
    }
    alert(msg);
    closeWin();
    return true;
}

function celcolchan(src,ID)
{

    if(src == ''){
        if(ID == '') return false;
        src = new  Object();
        src = document.getElementById(ID);
        if(src == null){
            return false;
        }
    }
    var sepa2 = src.id.split(',');
    document.forms[0].showDialog.value = src.id2;
    if(!checkUser(sepa2[1])){
        return false;
    }

    var lock = document.forms[0].locker.value;

    if (lock == 'first')
    {
        var first_idno = document.forms[0].first_idno.value;
        first_time = new  Object();
        first_time = document.getElementById(first_idno);

        if(src.id == first_time.id)
        {
            Style_Color(src.id);
            src.bgColor = color_changer(src.bgColor);
            document.forms[0].backupper.value = src.id;
            document.forms[0].ID_NO.value = src.id;
            document.forms[0].locker.value = 'on';
            make_key(first_time);
        }else{
            src.bgColor = color_changer(src.bgColor);
            Style_Color(src.id);
            backup_obj = new Object();
            backup_obj = src;
            document.forms[0].backupper.value = src.id;
            document.forms[0].ID_NO.value = src.id;
            document.forms[0].locker.value = 'on';
            make_key(src);
        }

    }else if(lock == 'off'){

        backup_obj = new Object();
        backup_obj = src;
        document.forms[0].ID_NO.value = src.id;
        src.bgColor = color_changer(src.bgColor);
        Style_Color(src.id);
        document.forms[0].locker.value = 'on';
        make_key(src);
        document.forms[0].backupper.value = src.id;

    }else if(lock == 'on'){

        var idno = document.forms[0].ID_NO.value;
        bk = new Object();
        bk = document.getElementById(idno);

        if(src.id == bk.id)
        {
            src.bgColor = color_changer(src.bgColor);
            Style_Color(src.id);
            document.forms[0].locker.value = 'off';
            document.forms[0].chosen_id.value = '';
            document.forms[0].ID_NO.value = '';
        }else{
            var thisis = document.forms[0].cmd.value;
            document.forms[0].locker.value = 'on';
            bk.bgColor = color_changer(bk.bgColor);
            src.bgColor = color_changer(src.bgColor);
            Style_Color(src.id);
            Style_Color(bk.id);
            document.forms[0].ID_NO.value = src.id;
            bk = src;
            make_key(src);
        }
    }

    mltiCtrl(src,'change');

}

function color_changer(color)
{
    var color_blue    = '#3399ff';
    var color_red     = '#ff0099';
    var color_yellow  = '#ffff00';
    var blue_change   = '#99ccff';
    var red_change    = '#ffccff';
    var yellow_change = '#ffffcc';
    var color_black   = '#000000';
    var color_white   = '#ffffff';
    var return_color;

    switch(color){
        case color_blue :
            return_color = blue_change ;
            chg_cap(true);
            break;
        case color_red :
            return_color = red_change ;
            chg_cap(false);
            break;
        case color_yellow :
            return_color = yellow_change ;
            chg_cap(true);
            break;
        case blue_change :
            return_color = color_blue ;
            break;
        case red_change :
            return_color = color_red ;
            break;
        case yellow_change :
            return_color = color_yellow ;
            break;
        case color_black :
            return_color = color_white;
            break;
        case color_white :
            return_color = color_black;
            break;
        default :
            return_color = color_black;
            break;
    }
    return return_color ;
}

function chg_cap(flg)
{
    if(flg == true){
        document.forms[0].btn_update.value = '入力取消';
        document.forms[0].btnShukketsu.value = '0';
    }else{
        document.forms[0].btn_update.value = '全員出席';
        document.forms[0].btnShukketsu.value = '1';
    }
}

function make_key(src)
{
    var color_blue   = '#99ccff';
    var color_yellow = '#ffffcc';
    var src_color    = src.bgColor;
    if (color_blue == src_color || color_yellow == src_color) {
        src_color = 'blue';
    } else {
        src_color = 'red';
    }

    var chaircd;
    if (isUpperIe9()) {
        chaircd = src.attributes && src.attributes["value"] ? src.attributes["value"].value : null;
    } else {
        chaircd = src.value;
    }
    document.forms[0].stock_chaircd.value = chaircd;
    document.forms[0].chosen_id.value = src.id + ',' + src_color;
}

function Page_jumper(jump,i)
{
    if(i == '0'){
        //window.location.replace(jump);
        var hash = jump.substring(jump.indexOf("#"));
        parent.main_frame.location.hash = hash;
        return;
    }

    if(!checkHidden('on'))    return false;
    var daycut;
    var sepa;
    sepa = document.forms[0].chosen_id.value.split(',');
    daycut = document.forms[0].executedate.value.replace('/','-');
    daycut = daycut.replace('/','-');

//    IsUserOK_ToJump(jump,daycut,sepa[0],sepa[1]);
    IsUserOK_ToJump(jump,daycut,sepa[0],sepa[1],sepa[2]);     //2005/05/06 賢者-作業依頼書20050506_01
    return ;
}

//function IsUserOK_ToJump(URL, syoribi, period, clsstfcd)
function IsUserOK_ToJump(URL, syoribi, period, clsstfcd, staffcd)     //2005/05/06 賢者-作業依頼書20050506_01
{


    if(!checkHidden('off'))       return false;
    if(!checkUser(clsstfcd))      return false;
    if(!checkOutPutCtrlDate())    return false;

    if(mltiCtrl('','check')){
        if(!checkOutPutCtrlDate()) return false;
        detailopen('change');
        return false;
    }

    var chaircd;
    if (isUpperIe9()) {
        var ccd = document.getElementById(period + ','+ clsstfcd+','+staffcd);
        if(ccd == null)  return false;
        chaircd = ccd.attributes["value"].value
    } else {
    //    var ccd = document.all(period + ','+ clsstfcd);
        var ccd = document.all(period + ','+ clsstfcd+','+staffcd);       //2005/05/06 賢者-作業依頼書20050506_01
        if(ccd == null)  return false;
        chaircd = ccd.value;
    }

//    window.open(URL+'?syoribi='+syoribi+'&periodcd='+period+'&STAFFCD='+clsstfcd+'&chaircd='+ccd.value);
//    window.open(URL+'?syoribi='+syoribi+'&periodcd='+period+'&STAFFCD='+staffcd+'&chaircd='+ccd.value);      //2005/05/06 賢者-作業依頼書20050506_01
    wopen(URL+'?syoribi='+syoribi+'&periodcd='+period+'&STAFFCD='+staffcd+'&chaircd='+chaircd,'name',0,0,screen.availWidth,screen.availHeight);      //2006/03/15 alp
    onWopenWindowClosed('name', function() { btn_submit(''); }); // 「appletのWindowをclose」でsubmit
}

function isUpperIe9() {
    var userAgent = window.navigator.userAgent.toLowerCase();
    var appVersion = window.navigator.appVersion.toLowerCase();
    if (userAgent.indexOf("msie") != -1) {
        if (appVersion.indexOf("msie 9.") != -1) {
            return true;
        } else if (appVersion.indexOf("msie 8.") != -1) {
            return false;
        } else if (appVersion.indexOf("msie 7.") != -1) {
            return false;
        } else if (appVersion.indexOf("msie 6.") != -1) {
            return false;
        } else {
            return true;
        }
    } else if (userAgent.indexOf("trident") != -1) {
        return true;
    } else {
        // alert("ブラウザがInternetExplorerではありません。");
    }
}

// change rgb format "rgb(255,127,255)" to "#FF7FFF" 
function rgbStrToHex(rgbStr) {
    var itohex = function (i) { var hex = i.toString(16); return (hex.length == 1) ? ('0' + hex) : hex;};
    var regex = /^rgb\(([ \d]+),([ \d]+),([ \d]+)\)/i; // ex) rgb(0, 0, 0), rgb( 0, 100, 200), rgb(255,127,255)
    var result = regex.exec(rgbStr);
    if (result) {
        var r = parseInt(result[1]);
        var g = parseInt(result[2]);
        var b = parseInt(result[3]);
        return "#" + itohex(r) + itohex(g) + itohex(b);
    }
    return null;
}

function onWopenWindowClosed(chkwinname, f) {
    var win = newWin;
    openedWinname = chkwinname; /* global */
    var chkOpened = function() {
        if (win) {
            if (win.closed) {
                if (f) {
                    f();
                }
            } else {
                if (openedWinname == chkwinname) {
                    setTimeout(chkOpened, 1000);
                }
            }
        }
    };
    chkOpened();
}

function Style_Color(set_id)
{
    var color = "";
        color = document.getElementById(set_id).style.color ;
        document.getElementById(set_id).style.color = isUpperIe9() ? color_changer(rgbStrToHex(color)) : color_changer(color);
}

function Update_check(str)
{
    switch(str){
        case '':
            break;
        case 'on':
            if(confirm('{rval MSG105}')){
                document.forms[0].update_flg.value = 'off';
                document.forms[0].cmd.value = 'update';
                document.forms[0].submit();
                break;
            }
            document.forms[0].update_flg.value = '';
            document.forms[0].cmd.value = 'main';
            document.forms[0].submit();
            break;
        case 'off':
            document.forms[0].update_flg.value = '';
            break;
    }
}

function detailopen(mode)
{
     if(mode == 'direct'){
        if(!checkHidden('off')) return ;
        var check = document.forms[0].chosen_id.value.split(',');
        alert(check[1]);
        if(!checkUser(check[1])) return false;
        if(!checkOutPutCtrlDate()) return false;
        mode = 'change';
    }else{
        if(!checkHidden('on')) return ;
    }
    var cd = document.forms[0].chosen_id.value.split(',');
    var URL = URLS;
    URL = URL + '?datekey=' +document.forms[0].cntl_dt_key.value;
    URL = URL + '&carcd=' +document.forms[0].stock_chaircd.value;	//2006.02.02 alp m-yama
    URL = URL + '&pd=' + cd[0] + '&stfcd=' + cd[1] + '&chg=off';
    URL = (mode == 'change')?  URL + '&md=on' : URL + '&md=off' ;
    wopen(URL,'',100,100,800,400);
    onWopenWindowClosed('');
    return;
}

function checkHidden(msgMode)
{
    var sepa = document.forms[0].chosen_id.value.split(',');
    var returnValue = true;
    if(sepa[0] == "") returnValue = false;
    if(document.forms[0].stock_chaircd.value == "" || document.forms[0].cntl_dt_key.value == "") returnValue = false;
    if(returnValue){
        return true;
    }else{
        if(msgMode == 'on') alert('{rval MSG304}');
        return false;
    }
}

function checkUser(t_Staffcd)
{
    var sec_chk = document.forms[0].Security.value;
    sec_chk = sec_chk.split(',');
    if (sec_chk[0] == '' || sec_chk[1] == '') return false;
    switch(sec_chk[0]){
        case '4':
            break;
        case '3':
            if (sec_chk[0] == '3' && sec_chk[1] != t_Staffcd) return false;
            break;
        default:
            return false;
    }
    return true;
}

function checkOutPutCtrlDate()
{
    var DateStr = document.forms[0].output_CtrlDate.value;
    var sec_chk = document.forms[0].Security.value;
    sec_chk = sec_chk.split(',');
    if (sec_chk[0] == '' || sec_chk[1] == '') return false;
    if (sec_chk[0] == '4') return true;
    DateStr = DateStr.split('/');
    DateStr = (DateStr[0] + '-' + DateStr[1] + '-' + DateStr[2]);

    if(document.forms[0].cntl_dt_key.value <= DateStr){
        alert('{rval MSG300}'+ '\r\n（ 出欠制御日付: ' + DateStr + '以前のデータ ）');
        return false;
    }
    return true;
}

function mltiCtrl(src,mode)
{
    var rval = false;

    if(mode == 'check'){
        if(document.forms[0].multi.value == 'on'){
            rval = "true";
        }
    }else if(mode == 'change' && src != ''){
        var sepa = src.id.split(',');

//        if(sepa[2] == 'm'){
        if(sepa[3] == 'm'){         //2005/05/06 賢者-作業依頼書20050506_01
            document.forms[0].multi.value = 'on'
        }else{
            document.forms[0].multi.value = 'off'
        }
        rval = true;
    }

    return rval;
}