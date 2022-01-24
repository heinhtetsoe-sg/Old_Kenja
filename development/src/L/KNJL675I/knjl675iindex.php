<?php
require_once('for_php7.php');

require_once('knjl675iModel.inc');
require_once('knjl675iQuery.inc');

class knjl675iController extends Controller
{
    public $ModelClassName = "knjl675iModel";
    public $ProgramID      = "KNJL675I";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                    $this->callView("knjl675iForm1");
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
$knjl675iCtl = new knjl675iController();
//var_dump($_REQUEST);
