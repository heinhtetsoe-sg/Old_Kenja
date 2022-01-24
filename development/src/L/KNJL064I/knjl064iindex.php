<?php

require_once('for_php7.php');

require_once('knjl064iModel.inc');
require_once('knjl064iQuery.inc');

class knjl064iController extends Controller
{
    public $ModelClassName = "knjl064iModel";
    public $ProgramID      = "KNJL064I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                case "syougou":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl064iForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl064iCtl = new knjl064iController();
