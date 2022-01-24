<?php

require_once('for_php7.php');

require_once('knjmp940Model.inc');
require_once('knjmp940Query.inc');

class knjmp940Controller extends Controller {
    var $ModelClassName = "knjmp940Model";
    var $ProgramID      = "KNJMP940";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjmp940":
                case "clear":
                case "search":
                    $sessionInstance->knjmp940Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp940Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp940Ctl = new knjmp940Controller;
?>
