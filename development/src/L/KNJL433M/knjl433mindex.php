<?php

require_once('for_php7.php');
require_once('knjl433mModel.inc');
require_once('knjl433mQuery.inc');

class knjl433mController extends Controller
{
    public $ModelClassName = "knjl433mModel";
    public $ProgramID      = "KNJL433M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl433mForm1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl433mForm1");
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
$knjl433mCtl = new knjl433mController();
