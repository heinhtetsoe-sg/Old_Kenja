<?php

require_once('for_php7.php');

require_once('knjl840hModel.inc');
require_once('knjl840hQuery.inc');

class knjl840hController extends Controller
{
    public $ModelClassName = "knjl840hModel";
    public $ProgramID      = "KNJL840H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl840hForm1");
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
$knjl840hCtl = new knjl840hController();
