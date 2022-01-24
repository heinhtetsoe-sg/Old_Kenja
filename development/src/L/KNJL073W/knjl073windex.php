<?php

require_once('for_php7.php');
require_once('knjl073wModel.inc');
require_once('knjl073wQuery.inc');

class knjl073wController extends Controller
{
    public $ModelClassName = "knjl073wModel";
    public $ProgramID      = "KNJL073W";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main":
                case "clear":
                    $this->callView("knjl073wForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl073wCtl = new knjl073wController;
