<?php
require_once('for_php7.php');

require_once('knjl672iModel.inc');
require_once('knjl672iQuery.inc');

class knjl672iController extends Controller
{
    public $ModelClassName = "knjl672iModel";
    public $ProgramID      = "KNJL672I";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                    $this->callView("knjl672iForm1");
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
$knjl672iCtl = new knjl672iController();
//var_dump($_REQUEST);
