<?php

require_once('for_php7.php');

require_once('knjf034bModel.inc');
require_once('knjf034bQuery.inc');

class knjf034bController extends Controller {
    var $ModelClassName = "knjf034bModel";
    var $ProgramID      = "KNJF034B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf034b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjf034bModel();      //コントロールマスタの呼び出し
                    $this->callView("knjf034bForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO001
                    $sessionInstance->knjf034bModel();      //コントロールマスタの呼び出し
                    $this->callView("knjf034bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjf034bCtl = new knjf034bController;
//var_dump($_REQUEST);
?>
