<?php

require_once('for_php7.php');

require_once('knjd615bModel.inc');
require_once('knjd615bQuery.inc');

class knjd615bController extends Controller {
    var $ModelClassName = "knjd615bModel";
    var $ProgramID      = "KNJD615B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd615bForm1");
                    }
                    break 2;
                case "":
                case "knjd615b":
                case "change_testkindcd":
                case "change_grade":
                case "gakki":
                    $sessionInstance->knjd615bModel();
                    $this->callView("knjd615bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd615bCtl = new knjd615bController;
?>
