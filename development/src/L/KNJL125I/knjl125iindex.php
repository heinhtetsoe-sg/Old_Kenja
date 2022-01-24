<?php
require_once('knjl125iModel.inc');
require_once('knjl125iQuery.inc');

class knjl125iController extends Controller
{
    public $ModelClassName = "knjl125iModel";
    public $ProgramID      = "KNJL125I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "judge":
                case "reset":
                case "end":
                    $this->callView("knjl125iForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("judge");
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
$knjl125iCtl = new knjl125iController();
