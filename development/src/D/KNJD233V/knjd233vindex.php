<?php

require_once('for_php7.php');

require_once('knjd233vModel.inc');
require_once('knjd233vQuery.inc');

class knjd233vController extends Controller {
    var $ModelClassName = "knjd233vModel";
    var $ProgramID      = "KNJD233V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd233v":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd233vModel();
                    $this->callView("knjd233vForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd233vModel();
                    $this->callView("knjd233vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd233vCtl = new knjd233vController;
var_dump($_REQUEST);
?>
