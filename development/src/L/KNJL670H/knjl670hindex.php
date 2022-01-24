<?php
require_once('knjl670hModel.inc');
require_once('knjl670hQuery.inc');

class knjl670hController extends Controller
{
    public $ModelClassName = "knjl670hModel";
    public $ProgramID      = "KNJL670H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl670h":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl670hModel();
                    $this->callView("knjl670hForm1");
                    break 2;
                case "clear":
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("knjl670h");
                    break 1;
                case "fix":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl670h");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl670hCtl = new knjl670hController();
