<?php

require_once('for_php7.php');

require_once('knje378Model.inc');
require_once('knje378Query.inc');

class knje378Controller extends Controller {
    var $ModelClassName = "knje378Model";
    var $ProgramID      = "KNJE378";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knje378Form1");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":
                    $sessionInstance->getCsv();
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje378Ctl = new knje378Controller;
?>
