<?php

require_once('for_php7.php');

require_once('knjl760hModel.inc');
require_once('knjl760hQuery.inc');

class knjl760hController extends Controller
{
    public $ModelClassName = "knjl760hModel";
    public $ProgramID      = "KNJL760H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "chgAppDiv":
                    $this->callView("knjl760hForm1");
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
$knjl760hCtl = new knjl760hController();
