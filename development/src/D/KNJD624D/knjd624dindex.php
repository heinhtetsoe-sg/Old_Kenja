<?php

require_once('for_php7.php');

require_once('knjd624dModel.inc');
require_once('knjd624dQuery.inc');

class knjd624dController extends Controller {
    var $ModelClassName = "knjd624dModel";
    var $ProgramID      = "KNJD624D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd624d":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd624dModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd624dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd624dCtl = new knjd624dController;
//var_dump($_REQUEST);
?>
