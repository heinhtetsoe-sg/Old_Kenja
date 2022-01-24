<?php

require_once('for_php7.php');

require_once('knjd614eModel.inc');
require_once('knjd614eQuery.inc');

class knjd614eController extends Controller {
    var $ModelClassName = "knjd614eModel";
    var $ProgramID      = "KNJD614E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clear";
                case "knjd614e";
                    $sessionInstance->knjd614eModel();
                    $this->callView("knjd614eForm1");
                    exit;
                case "csvOutput":
                    if (!$sessionInstance->getCsvOutputModel()){
                        $this->callView("knjd614eForm1");
                    }
                    break 2;
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd614eModel();
                    $this->callView("knjd614eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd614eCtl = new knjd614eController;
?>
