<?php

require_once('for_php7.php');

require_once('knjl437hModel.inc');
require_once('knjl437hQuery.inc');

class knjl437hController extends Controller
{
    public $ModelClassName = "knjl437hModel";
    public $ProgramID      = "KNJL437H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl437h":
                    $sessionInstance->knjl437hModel();
                    $this->callView("knjl437hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl437hCtl = new knjl437hController();
