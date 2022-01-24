<?php

require_once('for_php7.php');

require_once('knja230Model.inc');
require_once('knja230Query.inc');

class knja230Controller extends Controller {
    var $ModelClassName = "knja230Model";
    var $ProgramID      = "KNJA230";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "toukei":
                case "":
                    $this->callView("knja230Form1");
                   break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knja230Ctl = new knja230Controller;
//var_dump($_REQUEST);
?>
