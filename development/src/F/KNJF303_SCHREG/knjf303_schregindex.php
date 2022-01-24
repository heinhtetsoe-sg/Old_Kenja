<?php

require_once('for_php7.php');

require_once('knjf303_schregModel.inc');
require_once('knjf303_schregQuery.inc');

class knjf303_schregController extends Controller {
    var $ModelClassName = "knjf303_schregModel";
    var $ProgramID      = "KNJF303";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf303_schreg":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjf303_schregModel();       //コントロールマスタの呼び出し
                    $this->callView("knjf303_schregForm1");
                    exit;
                case "update_hanei":
                case "update":
                    if (!$sessionInstance->getUpdateModel()){
                        $this->callView("knjf303_schregForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf303_schregCtl = new knjf303_schregController;
//var_dump($_REQUEST);
?>
