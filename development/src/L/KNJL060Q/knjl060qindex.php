<?php

require_once('for_php7.php');

require_once('knjl060qModel.inc');
require_once('knjl060qQuery.inc');

class knjl060qController extends Controller
{
    public $ModelClassName = "knjl060qModel";
    public $ProgramID      = "KNJL060Q";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl060qForm1");
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
$knjl060qCtl = new knjl060qController;
?>
