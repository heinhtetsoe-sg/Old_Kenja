<?php

require_once('for_php7.php');

require_once('knja190Model.inc');
require_once('knja190Query.inc');

class knja190Controller extends Controller {
    var $ModelClassName = "knja190Model";
    var $ProgramID      = "KNJA190";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "hukusiki":
                case "change_class":
                case "knja190":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja190Model();
                    $this->callView("knja190Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja190Ctl = new knja190Controller;
var_dump($_REQUEST);
?>
