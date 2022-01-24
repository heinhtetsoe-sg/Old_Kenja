<?php

require_once('for_php7.php');

require_once('knjmp910Model.inc');
require_once('knjmp910Query.inc');

class knjmp910Controller extends Controller {
    var $ModelClassName = "knjmp910Model";
    var $ProgramID      = "KNJMP910";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjmp910":
                case "clear":
                case "search":
                    $sessionInstance->knjmp910Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp910Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp910Ctl = new knjmp910Controller;
?>
