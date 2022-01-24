<?php
require_once('knjl017vModel.inc');
require_once('knjl017vQuery.inc');

class knjl017vController extends Controller
{
    public $ModelClassName = "knjl017vModel";
    public $ProgramID      = "KNJL017V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear":
                case "change":
                case "changeExamId":
                case "changePlaceId":
                case "edit":
                    $this->callView("knjl017vForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl017vForm1");
                    }
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
$knjl017vCtl = new knjl017vController();
