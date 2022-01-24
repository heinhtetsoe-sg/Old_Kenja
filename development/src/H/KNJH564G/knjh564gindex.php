<?php

require_once('for_php7.php');

require_once('knjh564gModel.inc');
require_once('knjh564gQuery.inc');

class knjh564gController extends Controller {
    var $ModelClassName = "knjh564gModel";
    var $ProgramID      = "KNJH564G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh564g":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjh564gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjh564gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh564gCtl = new knjh564gController;
//var_dump($_REQUEST);
?>
