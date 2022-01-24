<?php

require_once('for_php7.php');

require_once('knjp930Model.inc');
require_once('knjp930Query.inc');

class knjp930Controller extends Controller {
    var $ModelClassName = "knjp930Model";
    var $ProgramID      = "KNJP930";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjp930":
                case "clear":
                case "search":
                    $sessionInstance->knjp930Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp930Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp930Ctl = new knjp930Controller;
?>
