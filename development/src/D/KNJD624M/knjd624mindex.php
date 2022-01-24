<?php

require_once('for_php7.php');

require_once('knjd624mModel.inc');
require_once('knjd624mQuery.inc');

class knjd624mController extends Controller {
    var $ModelClassName = "knjd624mModel";
    var $ProgramID      = "KNJD624M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd624m":
                    $sessionInstance->knjd624mModel();
                    $this->callView("knjd624mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd624mCtl = new knjd624mController;
?>
