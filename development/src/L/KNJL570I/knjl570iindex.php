<?php
require_once('knjl570iModel.inc');
require_once('knjl570iQuery.inc');

class knjl570iController extends Controller
{
    public $ModelClassName = "knjl570iModel";
    public $ProgramID      = "KNJL570I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "getdef":
                case "read":
                case "updread":
                case "reset":
                case "end":
                    $this->callView("knjl570iForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updread");
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
$knjl570iCtl = new knjl570iController();
