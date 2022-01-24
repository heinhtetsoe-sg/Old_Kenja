<?php

require_once('for_php7.php');

require_once('knjd156eModel.inc');
require_once('knjd156eQuery.inc');

class knjd156eController extends Controller {
    var $ModelClassName = "knjd156eModel";
    var $ProgramID      = "KNJD156E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clear";
                case "knjd156e";
                    $sessionInstance->knjd156eModel();
                    $this->callView("knjd156eForm1");
                    exit;
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd156eModel();
                    $this->callView("knjd156eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd156eCtl = new knjd156eController;
?>
