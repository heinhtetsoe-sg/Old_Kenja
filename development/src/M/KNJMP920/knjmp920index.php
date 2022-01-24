<?php

require_once('for_php7.php');

require_once('knjmp920Model.inc');
require_once('knjmp920Query.inc');

class knjmp920Controller extends Controller {
    var $ModelClassName = "knjmp920Model";
    var $ProgramID      = "KNJMP920";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjmp920":
                case "clear":
                case "search":
                    $sessionInstance->knjmp920Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp920Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp920Ctl = new knjmp920Controller;
?>
