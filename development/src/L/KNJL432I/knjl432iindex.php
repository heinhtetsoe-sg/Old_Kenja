<?php
require_once('knjl432iModel.inc');
require_once('knjl432iQuery.inc');

class knjl432iController extends Controller
{
    public $ModelClassName = "knjl432iModel";
    public $ProgramID      = "KNJL432I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl432iForm1");
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
$knjl432iCtl = new knjl432iController();
