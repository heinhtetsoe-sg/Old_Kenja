<?php

require_once('for_php7.php');

class knjc030_1Model extends Model
{
    var $cmd;
    var $chaircd;
    var $pericd;	//2006.02.04 alp m-yama
    var $t_Staffcd;
    var $executedate;
    var $semester;
    var $staffcd;
    var $adminMode;
    var $trgtClass;

    //コンストラクタ
    function knjc030_1Model()
    {
    }

    function init()
    {
        $this->cmd = VARS::post("cmd");
		$chrperi = explode('-',VARS::post("chaircombo"));	//2006.02.04 alp m-yama
        $this->chaircd = $chrperi[0];						//2006.02.04 alp m-yama
		$this->pericd  = $chrperi[1];						//2006.02.04 alp m-yama
        if(VARS::get("datekey")) $this->executedate = VARS::get("datekey");
#        if(VARS::get("semes"))   $this->semester    = VARS::get("semes");
#        if(VARS::get("stfcd"))   $this->t_Staffcd   = VARS::get("stfcd");
        if(VARS::get("mode"))    $this->adminMode   = VARS::get("mode");
        if(VARS::get("tr"))      $this->trgtClass   = VARS::get("tr");
        //セキュリティーチェック
        $this->staffcd = STAFFCD;

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
