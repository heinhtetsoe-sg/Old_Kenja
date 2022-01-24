<?php
require_once('knjl633fModel.inc');
require_once('knjl633fQuery.inc');

class knjl633fController extends Controller
{
    public $ModelClassName = "knjl633fModel";
    public $ProgramID      = "KNJL633F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl633f":
                    $sessionInstance->knjl633fModel();
                    $this->callView("knjl633fForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl633fForm1");
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
$knjl633fCtl = new knjl633fController;
?>
