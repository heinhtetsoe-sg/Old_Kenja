<?php

require_once('for_php7.php');

require_once('knjd214mModel.inc');
require_once('knjd214mQuery.inc');

class knjd214mController extends Controller {
    var $ModelClassName = "knjd214mModel";
    var $ProgramID      = "KNJD214M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjd214m");
                    break 1;
                case "clear":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjd214m");
                    break 1;
                case "":
                case "knjd214m":
                case "semechg":
                    $sessionInstance->knjd214mModel();
                    $this->callView("knjd214mForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd214mModel();
                    $this->callView("knjd214mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd214mCtl = new knjd214mController;
var_dump($_REQUEST);
?>
