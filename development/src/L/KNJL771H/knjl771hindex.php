<?php

require_once('for_php7.php');

require_once('knjl771hModel.inc');
require_once('knjl771hQuery.inc');

class knjl771hController extends Controller
{
    public $ModelClassName = "knjl771hModel";
    public $ProgramID      = "KNJL771H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl771hForm1");
                    break 2;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjl771hCtl = new knjl771hController();
