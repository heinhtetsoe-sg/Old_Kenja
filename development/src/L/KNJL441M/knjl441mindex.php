<?php

require_once('for_php7.php');
require_once('knjl441mModel.inc');
require_once('knjl441mQuery.inc');

class knjl441mController extends Controller
{
    public $ModelClassName = "knjl441mModel";
    public $ProgramID      = "KNJL441M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                    $this->callView("knjl441mForm1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl441mForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl441mCtl = new knjl441mController();
