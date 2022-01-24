<?php

require_once('for_php7.php');

require_once('knjh564fModel.inc');
require_once('knjh564fQuery.inc');

class knjh564fController extends Controller {
    var $ModelClassName = "knjh564fModel";
    var $ProgramID      = "KNJH564F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh564f":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh564fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjh564fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh564fCtl = new knjh564fController;
//var_dump($_REQUEST);
?>
