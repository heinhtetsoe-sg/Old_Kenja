<?php
require_once('for_php7.php');

require_once('knjl677iModel.inc');
require_once('knjl677iQuery.inc');

class knjl677iController extends Controller
{
    public $ModelClassName = "knjl677iModel";
    public $ProgramID      = "KNJL677I";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                    $this->callView("knjl677iForm1");
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
$knjl677iCtl = new knjl677iController();
//var_dump($_REQUEST);
