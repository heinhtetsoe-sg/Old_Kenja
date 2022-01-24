<?php

require_once('for_php7.php');

require_once('knjm010Model.inc');
require_once('knjm010Query.inc');

class knjm010Controller extends Controller {
    var $ModelClassName = "knjm010Model";
    var $ProgramID      = "KNJM010";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm010":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm010Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm010Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjm010Model();
                    $this->callView("knjm010Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm010Ctl = new knjm010Controller;
var_dump($_REQUEST);
?>
