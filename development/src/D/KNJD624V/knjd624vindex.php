<?php

require_once('for_php7.php');

require_once('knjd624vModel.inc');
require_once('knjd624vQuery.inc');

class knjd624vController extends Controller {
    var $ModelClassName = "knjd624vModel";
    var $ProgramID      = "KNJD624V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd624v":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd624vModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd624vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd624vCtl = new knjd624vController;
//var_dump($_REQUEST);
?>
