<?php

require_once('for_php7.php');

require_once('knjd214cModel.inc');
require_once('knjd214cQuery.inc');

class knjd214cController extends Controller {
    var $ModelClassName = "knjd214cModel";
    var $ProgramID      = "KNJD214C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjd214c");
                    break 1;
                case "clear":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjd214c");
                    break 1;
                case "del_rireki":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteRirekiModel();
                    $sessionInstance->setCmd("knjd214c");
                    break 1;
                case "":
                case "knjd214c":
                case "semechg":
                    $sessionInstance->knjd214cModel();
                    $this->callView("knjd214cForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd214cModel();
                    $this->callView("knjd214cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd214cCtl = new knjd214cController;
var_dump($_REQUEST);
?>
