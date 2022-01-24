<?php

require_once('for_php7.php');

require_once('knjd154jModel.inc');
require_once('knjd154jQuery.inc');

class knjd154jController extends Controller {
    var $ModelClassName = "knjd154jModel";
    var $ProgramID      = "KNJD154J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjd154j";
                    $sessionInstance->knjd154jModel();
                    $this->callView("knjd154jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd154jCtl = new knjd154jController;
?>
