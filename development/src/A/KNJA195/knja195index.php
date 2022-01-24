<?php

require_once('for_php7.php');

require_once('knja195Model.inc');
require_once('knja195Query.inc');

class knja195Controller extends Controller {
    var $ModelClassName = "knja195Model";
    var $ProgramID      = "KNJA195";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "knja195":
                    $sessionInstance->knja195Model();
                    $this->callView("knja195Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja195Ctl = new knja195Controller;
var_dump($_REQUEST);
?>
