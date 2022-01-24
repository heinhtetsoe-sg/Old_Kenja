<?php

require_once('for_php7.php');

require_once('knje351Model.inc');
require_once('knje351Query.inc');

class knje351Controller extends Controller {
    var $ModelClassName = "knje351Model";
    var $ProgramID      = "KNJE351";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje351":
                case "gakki":
                    $sessionInstance->knje351Model();
                    $this->callView("knje351Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje351Ctl = new knje351Controller;
var_dump($_REQUEST);
?>
