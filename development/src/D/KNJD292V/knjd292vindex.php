<?php

require_once('for_php7.php');

require_once('knjd292vModel.inc');
require_once('knjd292vQuery.inc');

class knjd292vController extends Controller {
    var $ModelClassName = "knjd292vModel";
    var $ProgramID      = "KNJD292V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd292v":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd292vModel();
                    $this->callView("knjd292vForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd292vModel();
                    $this->callView("knjd292vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd292vCtl = new knjd292vController;
var_dump($_REQUEST);
?>
