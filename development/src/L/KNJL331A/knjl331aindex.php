<?php

require_once('for_php7.php');

require_once('knjl331aModel.inc');
require_once('knjl331aQuery.inc');

class knjl331aController extends Controller
{
    public $ModelClassName = "knjl331aModel";
    public $ProgramID      = "KNJL331A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl331a":
                    $this->callView("knjl331aForm1");
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
$knjl331aCtl = new knjl331aController;
