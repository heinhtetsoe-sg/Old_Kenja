<?php

require_once('for_php7.php');

require_once('knjd616eModel.inc');
require_once('knjd616eQuery.inc');

class knjd616eController extends Controller {
    var $ModelClassName = "knjd616eModel";
    var $ProgramID      = "KNJD616E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd616e":
                case "change_testkindcd":
                case "change_hr_class":
                case "gakki":
                    $sessionInstance->knjd616eModel();
                    $this->callView("knjd616eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd616eCtl = new knjd616eController;
?>
