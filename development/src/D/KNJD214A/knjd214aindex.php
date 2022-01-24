<?php

require_once('for_php7.php');

require_once('knjd214aModel.inc');
require_once('knjd214aQuery.inc');

class knjd214aController extends Controller
{
    public $ModelClassName = "knjd214aModel";
    public $ProgramID      = "KNJD214A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjd214a");
                    break 1;
                case "clear":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjd214a");
                    break 1;
                case "del_rireki":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteRirekiModel();
                    $sessionInstance->setCmd("knjd214a");
                    break 1;
                case "":
                case "confirm":
                case "knjd214a":
                case "semechg":
                    $sessionInstance->knjd214aModel();
                    $this->callView("knjd214aForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd214aModel();
                    $this->callView("knjd214aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd214aCtl = new knjd214aController();
var_dump($_REQUEST);
