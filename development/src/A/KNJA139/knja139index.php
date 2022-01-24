<?php

require_once('for_php7.php');

require_once('knja139Model.inc');
require_once('knja139Query.inc');

class knja139Controller extends Controller {
    var $ModelClassName = "knja139Model";
    var $ProgramID      = "KNJA139";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja139":
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja139Model();
                    $this->callView("knja139Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja139Ctl = new knja139Controller;
var_dump($_REQUEST);
?>
