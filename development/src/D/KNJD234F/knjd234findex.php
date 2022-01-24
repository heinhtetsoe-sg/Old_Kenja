<?php

require_once('for_php7.php');

require_once('knjd234fModel.inc');
require_once('knjd234fQuery.inc');

class knjd234fController extends Controller {
    var $ModelClassName = "knjd234fModel";
    var $ProgramID      = "KNJD234F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd234f":
                case "change_testkindcd":
                case "change_hr_class":
                case "gakki":
                    $sessionInstance->knjd234fModel();
                    $this->callView("knjd234fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd234fCtl = new knjd234fController;
?>
