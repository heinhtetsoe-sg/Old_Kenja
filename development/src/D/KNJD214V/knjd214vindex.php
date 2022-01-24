<?php

require_once('for_php7.php');

require_once('knjd214vModel.inc');
require_once('knjd214vQuery.inc');

class knjd214vController extends Controller
{
    public $ModelClassName = "knjd214vModel";
    public $ProgramID      = "KNJD214V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("knjd214v");
                    break 1;
                case "clear":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjd214v");
                    break 1;
                case "del_rireki":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getDeleteRirekiModel();
                    $sessionInstance->setCmd("knjd214v");
                    break 1;
                case "":
                case "knjd214v":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd214vModel();
                    $this->callView("knjd214vForm1");
                    exit;
                case "semechg":
                    $sessionInstance->knjd214vModel();
                    $this->callView("knjd214vForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd214vModel();
                    $this->callView("knjd214vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd214vCtl = new knjd214vController();
var_dump($_REQUEST);
