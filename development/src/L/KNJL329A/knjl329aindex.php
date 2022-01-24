<?php
require_once('knjl329aModel.inc');
require_once('knjl329aQuery.inc');

class knjl329aController extends Controller
{
    public $ModelClassName = "knjl329aModel";
    public $ProgramID      = "KNJL329A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl329a":
                    $sessionInstance->knjl329aModel();
                    $this->callView("knjl329aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl329aCtl = new knjl329aController();
