<?php
require_once('for_php7.php');

require_once('knjl673iModel.inc');
require_once('knjl673iQuery.inc');

class knjl673iController extends Controller
{
    public $ModelClassName = "knjl673iModel";
    public $ProgramID      = "KNJL673I";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                    $this->callView("knjl673iForm1");
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
$knjl673iCtl = new knjl673iController();
//var_dump($_REQUEST);
