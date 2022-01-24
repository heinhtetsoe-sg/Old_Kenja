<?php

require_once('for_php7.php');

require_once('knjl190dModel.inc');
require_once('knjl190dQuery.inc');

class knjl190dController extends Controller
{
    public $ModelClassName = "knjl190dModel";
    public $ProgramID      = "KNJL190D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl190d":
                case "changeTest":
                case "changeRadio":
                    $this->callView("knjl190dForm1");
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
$knjl190dCtl = new knjl190dController();
