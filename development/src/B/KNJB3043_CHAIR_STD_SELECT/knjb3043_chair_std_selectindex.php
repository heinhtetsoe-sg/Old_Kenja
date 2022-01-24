<?php

require_once('for_php7.php');

require_once('knjb3043_chair_std_selectModel.inc');
require_once('knjb3043_chair_std_selectQuery.inc');

class knjb3043_chair_std_selectController extends Controller {
    var $ModelClassName = "knjb3043_chair_std_selectModel";
    var $ProgramID      = "KNJB3043_CHAIR_STD_SELECT";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "reset";
                case "getStdOverlap";
                    $sessionInstance->knjb3043_chair_std_selectModel();
                    $this->callView("knjb3043_chair_std_selectForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb3043_chair_std_selectCtl = new knjb3043_chair_std_selectController;
?>
