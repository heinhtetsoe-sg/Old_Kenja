<?php

require_once('for_php7.php');

require_once('knjd616gModel.inc');
require_once('knjd616gQuery.inc');

class knjd616gController extends Controller {
    var $ModelClassName = "knjd616gModel";
    var $ProgramID      = "KNJD616G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd616g":
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd616gModel();
                    $this->callView("knjd616gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd616gCtl = new knjd616gController;
//var_dump($_REQUEST);
?>
