<?php

require_once('for_php7.php');

require_once('knjd623bModel.inc');
require_once('knjd623bQuery.inc');

class knjd623bController extends Controller {
    var $ModelClassName = "knjd623bModel";
    var $ProgramID      = "KNJD623B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "chgsemes":
                case "seldate":
                case "knjd623b":
                    $sessionInstance->knjd623bModel();
                    $this->callView("knjd623bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd623bCtl = new knjd623bController;
?>
