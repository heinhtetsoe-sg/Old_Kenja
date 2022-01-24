<?php

require_once('for_php7.php');

require_once('knjl362aModel.inc');
require_once('knjl362aQuery.inc');

class knjl362aController extends Controller {
    var $ModelClassName = "knjl362aModel";
    var $ProgramID      = "KNJL362A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl362a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl362aModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl362aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl362aCtl = new knjl362aController;
//var_dump($_REQUEST);
?>
