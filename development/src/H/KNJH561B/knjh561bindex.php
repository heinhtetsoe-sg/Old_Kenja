<?php

require_once('for_php7.php');

require_once('knjh561bModel.inc');
require_once('knjh561bQuery.inc');

class knjh561bController extends Controller {
    var $ModelClassName = "knjh561bModel";
    var $ProgramID      = "KNJH561B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh561b":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh561bModel();       //コントロールマスタの呼び出し
                    $this->callView("knjh561bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh561bCtl = new knjh561bController;
//var_dump($_REQUEST);
?>
