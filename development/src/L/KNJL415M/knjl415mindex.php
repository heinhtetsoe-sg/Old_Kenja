<?php

require_once('for_php7.php');
require_once('knjl415mModel.inc');
require_once('knjl415mQuery.inc');

class knjl415mController extends Controller
{
    public $ModelClassName = "knjl415mModel";
    public $ProgramID      = "KNJL415M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                    $this->callView("knjl415mForm1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl415mForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl415mCtl = new knjl415mController();
