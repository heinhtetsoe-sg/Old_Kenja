<?php

require_once('for_php7.php');

require_once('knjm500nModel.inc');
require_once('knjm500nQuery.inc');

class knjm500nController extends Controller {
    var $ModelClassName = "knjm500nModel";
    var $ProgramID      = "KNJM500N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjm500nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm500nForm1");
                    exit;
                case "knjm500n":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjm500nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm500nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm500nCtl = new knjm500nController;
//var_dump($_REQUEST);
?>
