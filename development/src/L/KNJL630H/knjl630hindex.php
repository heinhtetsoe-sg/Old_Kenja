<?php
require_once('knjl630hModel.inc');
require_once('knjl630hQuery.inc');

class knjl630hController extends Controller
{
    public $ModelClassName = "knjl630hModel";
    public $ProgramID      = "KNJL630H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                case "huban":
                    $this->callView("knjl630hForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
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
$knjl630hCtl = new knjl630hController;
?>
