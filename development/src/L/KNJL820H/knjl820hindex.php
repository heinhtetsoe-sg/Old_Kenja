<?php
require_once('knjl820hModel.inc');
require_once('knjl820hQuery.inc');

class knjl820hController extends Controller
{
    public $ModelClassName = "knjl820hModel";
    public $ProgramID      = "KNJL820H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl820hForm1");
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
$knjl820hCtl = new knjl820hController;
