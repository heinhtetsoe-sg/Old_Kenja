<?php

require_once('for_php7.php');

require_once('knjm260mModel.inc');
require_once('knjm260mQuery.inc');

class knjm260mController extends Controller {
    var $ModelClassName = "knjm260mModel";
    var $ProgramID      = "KNJM260M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "add":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("addread");
                    break 1;
                case "chdel":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "cmdStart":
                case "read":
                case "addread":
                case "main":
                case "sort":
                    $this->callView("knjm260mForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm260mForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("cmdStart");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm260mCtl = new knjm260mController;
//var_dump($_REQUEST);
?>
