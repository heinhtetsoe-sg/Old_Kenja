<?php

require_once('for_php7.php');

require_once('knja190wModel.inc');
require_once('knja190wQuery.inc');

class knja190wController extends Controller {
    var $ModelClassName = "knja190wModel";
    var $ProgramID      = "KNJA190W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "hukusiki":
                case "change_class":
                case "knja190w":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja190wModel();
                    $this->callView("knja190wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja190wCtl = new knja190wController;
var_dump($_REQUEST);
?>
