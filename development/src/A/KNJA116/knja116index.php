<?php

require_once('for_php7.php');

require_once('knja116Model.inc');
require_once('knja116Query.inc');

class knja116Controller extends Controller {
    var $ModelClassName = "knja116Model";
    var $ProgramID      = "KNJA116";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                case "year":
                case "clear":
                    $sessionInstance->getMainModel();
                    $this->callView("knja116Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja116Ctl = new knja116Controller;
?>
