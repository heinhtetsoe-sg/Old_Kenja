<?php

require_once('for_php7.php');

require_once('knjd231dModel.inc');
require_once('knjd231dQuery.inc');

class knjd231dController extends Controller {
    var $ModelClassName = "knjd231dModel";
    var $ProgramID      = "KNJD231D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd231dModel();
                    $this->callView("knjd231dForm1");
                    break 2;
                case "knjd231d":
                case "chgKetten":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd231dForm1");
                    break 2;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd231dForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd231dCtl = new knjd231dController;
?>
