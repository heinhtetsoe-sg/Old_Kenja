<?php

require_once('for_php7.php');

require_once('knjl333aModel.inc');
require_once('knjl333aQuery.inc');

class knjl333aController extends Controller
{
    public $ModelClassName = "knjl333aModel";
    public $ProgramID      = "KNJL333A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl333a":
                    $sessionInstance->knjl333aModel();
                    $this->callView("knjl333aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl333aCtl = new knjl333aController();
