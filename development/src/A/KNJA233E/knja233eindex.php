<?php

require_once('for_php7.php');

require_once('knja233eModel.inc');
require_once('knja233eQuery.inc');

class knja233eController extends Controller {
    var $ModelClassName = "knja233eModel";
    var $ProgramID      = "KNJA233E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja233e":
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja233eModel();
                    $this->callView("knja233eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja233eCtl = new knja233eController;
//var_dump($_REQUEST);
?>
