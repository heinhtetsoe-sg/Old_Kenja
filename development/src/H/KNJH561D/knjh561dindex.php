<?php

require_once('for_php7.php');

require_once('knjh561dModel.inc');
require_once('knjh561dQuery.inc');

class knjh561dController extends Controller {
    var $ModelClassName = "knjh561dModel";
    var $ProgramID      = "KNJH561D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh561d":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh561dModel();        //コントロールマスタの呼び出し
                    $this->callView("knjh561dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh561dCtl = new knjh561dController;
//var_dump($_REQUEST);
?>
