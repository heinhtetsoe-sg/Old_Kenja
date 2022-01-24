<?php

require_once('for_php7.php');

require_once('knja061Model.inc');
require_once('knja061Query.inc');

class knja061Controller extends Controller {
    var $ModelClassName = "knja061Model";
    var $ProgramID      = "KNJA061";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja061":
                    $sessionInstance->knja061Model();
                    $this->callView("knja061Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knja061Ctl = new knja061Controller;
var_dump($_REQUEST);
?>
