<?php

require_once('for_php7.php');
require_once('knjl414mModel.inc');
require_once('knjl414mQuery.inc');

class knjl414mController extends Controller
{
    public $ModelClassName = "knjl414mModel";
    public $ProgramID      = "KNJL414M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl414mForm1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl414mForm1");
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
$knjl414mCtl = new knjl414mController();
