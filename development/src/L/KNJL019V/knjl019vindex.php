<?php

require_once('for_php7.php');
require_once('knjl019vModel.inc');
require_once('knjl019vQuery.inc');

class knjl019vController extends Controller
{
    public $ModelClassName = "knjl019vModel";
    public $ProgramID      = "KNJL019V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "updread":
                case "reset":
                case "end":
                    $this->callView("knjl019vForm1");
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
$knjl019vCtl = new knjl019vController();
