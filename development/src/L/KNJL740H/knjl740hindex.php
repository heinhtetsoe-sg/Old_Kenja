<?php
require_once('knjl740hModel.inc');
require_once('knjl740hQuery.inc');

class knjl740hController extends Controller
{
    public $ModelClassName = "knjl740hModel";
    public $ProgramID      = "KNJL740H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear":
                case "change":
                case "changeTestDiv":
                case "edit":
                    $this->callView("knjl740hForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
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
$knjl740hCtl = new knjl740hController;
