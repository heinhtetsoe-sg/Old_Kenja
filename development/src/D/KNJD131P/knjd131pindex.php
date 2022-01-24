<?php

require_once('for_php7.php');

require_once('knjd131pModel.inc');
require_once('knjd131pQuery.inc');

class knjd131pController extends Controller {
    var $ModelClassName = "knjd131pModel";
    var $ProgramID      = "KNJD131P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd131p":
                    $sessionInstance->knjd131pModel();
                    $this->callView("knjd131pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd131pCtl = new knjd131pController;
?>
