<?php

require_once('for_php7.php');

require_once('knjm392eModel.inc');
require_once('knjm392eQuery.inc');

class knjm392eController extends Controller {
    var $ModelClassName = "knjm392eModel";
    var $ProgramID      = "KNJM392E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("addread");
                    break 1;
                case "chdel":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                case "chg_course":
                case "change":
                case "read":
                case "addread":
                case "main":
                case "sort":
                    $this->callView("knjm392eForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm392eForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm392eCtl = new knjm392eController;
?>
