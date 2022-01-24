<?php

require_once('for_php7.php');

require_once('knja431sModel.inc');
require_once('knja431sQuery.inc');

class knja431sController extends Controller {
    var $ModelClassName = "knja431sModel";
    var $ProgramID      = "KNJA431S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja431s":
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja431sModel();
                    $this->callView("knja431sForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja431sCtl = new knja431sController;
var_dump($_REQUEST);
?>
