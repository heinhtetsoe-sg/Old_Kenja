<?php

require_once('for_php7.php');

require_once('knjd625bModel.inc');
require_once('knjd625bQuery.inc');

class knjd625bController extends Controller {
    var $ModelClassName = "knjd625bModel";
    var $ProgramID      = "KNJD625B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd625b":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd625bModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd625bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd625bCtl = new knjd625bController;
//var_dump($_REQUEST);
?>
