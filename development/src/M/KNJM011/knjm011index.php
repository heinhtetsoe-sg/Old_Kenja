<?php

require_once('for_php7.php');

require_once('knjm011Model.inc');
require_once('knjm011Query.inc');

class knjm011Controller extends Controller {
    var $ModelClassName = "knjm011Model";
    var $ProgramID      = "KNJM011";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm011":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm011Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm011Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjm011Model();
                    $this->callView("knjm011Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm011Ctl = new knjm011Controller;
var_dump($_REQUEST);
?>
