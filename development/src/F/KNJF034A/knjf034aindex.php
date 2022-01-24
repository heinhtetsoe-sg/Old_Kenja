<?php

require_once('for_php7.php');

require_once('knjf034aModel.inc');
require_once('knjf034aQuery.inc');

class knjf034aController extends Controller {
    var $ModelClassName = "knjf034aModel";
    var $ProgramID      = "KNJF034A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf034a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjf034aModel();      //コントロールマスタの呼び出し
                    $this->callView("knjf034aForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO001
                    $sessionInstance->knjf034aModel();      //コントロールマスタの呼び出し
                    $this->callView("knjf034aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjf034aCtl = new knjf034aController;
//var_dump($_REQUEST);
?>
