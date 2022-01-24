<?php

require_once('for_php7.php');

require_once('knjmp930Model.inc');
require_once('knjmp930Query.inc');

class knjmp930Controller extends Controller {
    var $ModelClassName = "knjmp930Model";
    var $ProgramID      = "KNJMP930";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjmp930":
                case "clear":
                case "search":
                    $sessionInstance->knjmp930Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp930Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp930Ctl = new knjmp930Controller;
?>
