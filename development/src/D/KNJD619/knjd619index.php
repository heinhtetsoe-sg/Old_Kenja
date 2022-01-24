<?php

require_once('for_php7.php');

require_once('knjd619Model.inc');
require_once('knjd619Query.inc');

class knjd619Controller extends Controller {
    var $ModelClassName = "knjd619Model";
    var $ProgramID      = "KNJD619";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd619Form1");
                    }
                    break 2;
                case "":
                case "knjd619":
                    $sessionInstance->knjd619Model();
                    $this->callView("knjd619Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd619Ctl = new knjd619Controller;
?>
