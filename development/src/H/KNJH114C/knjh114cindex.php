<?php

require_once('for_php7.php');

require_once('knjh114cModel.inc');
require_once('knjh114cQuery.inc');

class knjh114cController extends Controller {
    var $ModelClassName = "knjh114cModel";
    var $ProgramID      = "KNJH114C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh114cModel();
                    $this->callView("knjh114cForm1");
                    exit;
                case "knjh114c":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh114cModel();       //コントロールマスタの呼び出し
                    $this->callView("knjh114cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjh114cCtl = new knjh114cController;
//var_dump($_REQUEST);
?>
