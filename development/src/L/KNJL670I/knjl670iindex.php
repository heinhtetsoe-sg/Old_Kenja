<?php
require_once('for_php7.php');

require_once('knjl670iModel.inc');
require_once('knjl670iQuery.inc');

class knjl670iController extends Controller
{
    public $ModelClassName = "knjl670iModel";
    public $ProgramID      = "KNJL670I";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                    $this->callView("knjl670iForm1");
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
$knjl670iCtl = new knjl670iController();
//var_dump($_REQUEST);
