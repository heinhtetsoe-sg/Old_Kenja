<?php

require_once('for_php7.php');

require_once('knjd615dModel.inc');
require_once('knjd615dQuery.inc');

class knjd615dController extends Controller {
    var $ModelClassName = "knjd615dModel";
    var $ProgramID      = "KNJD615D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd615dForm1");
                    }
                    break 2;
                case "":
                case "knjd615d":
                case "change_grade":
                case "gakki":
                    $sessionInstance->knjd615dModel();
                    $this->callView("knjd615dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd615dCtl = new knjd615dController;
//var_dump($_REQUEST);
?>
