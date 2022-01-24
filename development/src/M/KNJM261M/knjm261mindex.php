<?php

require_once('for_php7.php');

require_once('knjm261mModel.inc');
require_once('knjm261mQuery.inc');

class knjm261mController extends Controller {
    var $ModelClassName = "knjm261mModel";
    var $ProgramID      = "KNJM261M";

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
                    $this->callView("knjm261mForm1");
                    break 2;
                case "dsub":
                    $this->callView("knjm261mForm1");
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
$knjm261mCtl = new knjm261mController;
//var_dump($_REQUEST);
?>
