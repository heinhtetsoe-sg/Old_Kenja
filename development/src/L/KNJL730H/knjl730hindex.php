<?php
require_once('knjl730hModel.inc');
require_once('knjl730hQuery.inc');

class knjl730hController extends Controller
{
    public $ModelClassName = "knjl730hModel";
    public $ProgramID      = "KNJL730H";

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
                case "changeExamType":
                case "edit":
                    $this->callView("knjl730hForm1");
                    break 2;
                case "halladd":
                case "edit2":
                    $this->callView("knjl730hForm2");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "hallupdate":
                    $sessionInstance->getHallUpdateModel();
                    $sessionInstance->setCmd("edit2");
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
$knjl730hCtl = new knjl730hController;
