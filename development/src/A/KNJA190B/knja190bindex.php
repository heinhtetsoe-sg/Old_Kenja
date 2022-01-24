<?php

require_once('for_php7.php');

require_once('knja190bModel.inc');
require_once('knja190bQuery.inc');

class knja190bController extends Controller {
    var $ModelClassName = "knja190bModel";
    var $ProgramID      = "KNJA190B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "hukusiki":
                case "change_class":
                case "knja190b":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja190bModel();
                    $this->callView("knja190bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja190bCtl = new knja190bController;
var_dump($_REQUEST);
?>
