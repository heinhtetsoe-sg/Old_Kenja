<?php

require_once('for_php7.php');

require_once('knjd615eModel.inc');
require_once('knjd615eQuery.inc');

class knjd615eController extends Controller {
    var $ModelClassName = "knjd615eModel";
    var $ProgramID      = "KNJD615E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd615e":
                case "change_testkindcd":
                case "change_hr_class":
                case "gakki":
                    $sessionInstance->knjd615eModel();
                    $this->callView("knjd615eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd615eCtl = new knjd615eController;
?>
