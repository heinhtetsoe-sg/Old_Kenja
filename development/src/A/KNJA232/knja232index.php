<?php

require_once('for_php7.php');

require_once('knja232Model.inc');
require_once('knja232Query.inc');

class knja232Controller extends Controller {
    var $ModelClassName = "knja232Model";
    var $ProgramID      = "KNJA232";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "toukei":
                case "":
                    $this->callView("knja232Form1");
                   break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knja232Ctl = new knja232Controller;
//var_dump($_REQUEST);
?>
