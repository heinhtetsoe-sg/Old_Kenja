<?php

require_once('for_php7.php');

require_once('knjl071aModel.inc');
require_once('knjl071aQuery.inc');

class knjl071aController extends Controller
{
    public $ModelClassName = "knjl071aModel";
    public $ProgramID      = "KNJL071A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl071aForm1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl071aForm1");
                    }
                    break 2;
                case "update":
                case "updateTokutaiKakutei":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl071aCtl = new knjl071aController();
