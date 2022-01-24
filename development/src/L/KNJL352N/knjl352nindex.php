<?php

require_once('for_php7.php');

require_once('knjl352nModel.inc');
require_once('knjl352nQuery.inc');

class knjl352nController extends Controller
{
    public $ModelClassName = "knjl352nModel";
    public $ProgramID      = "KNJL352N";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl352n":
                    $sessionInstance->knjl352nModel();
                    $this->callView("knjl352nForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl352nForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl352nCtl = new knjl352nController();
