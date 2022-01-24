<?php

require_once('for_php7.php');

require_once('knjd614cModel.inc');
require_once('knjd614cQuery.inc');

class knjd614cController extends Controller {
    var $ModelClassName = "knjd614cModel";
    var $ProgramID      = "KNJD614C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                case "knjd614c_2":
                    $sessionInstance->knjd614cModel();
                    $this->callView("knjd614cForm1");
                    exit;
                case "knjd614c":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd614cModel();
                    $this->callView("knjd614cForm1");
                    exit;
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd614cModel();
                    $this->callView("knjd614cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd614cCtl = new knjd614cController;
var_dump($_REQUEST);
?>
