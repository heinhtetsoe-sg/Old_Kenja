<?php
require_once('knjl860hModel.inc');
require_once('knjl860hQuery.inc');

class knjl860hController extends Controller
{
    public $ModelClassName = "knjl860hModel";
    public $ProgramID      = "KNJL860H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl860hForm1");
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
$knjl860hCtl = new knjl860hController();
