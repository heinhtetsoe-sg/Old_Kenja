<?php

require_once('for_php7.php');

require_once('knjl053qModel.inc');
require_once('knjl053qQuery.inc');

class knjl053qController extends Controller
{
    public $ModelClassName = "knjl053qModel";
    public $ProgramID      = "KNJL053Q";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "end":
                case "main":
                case "read":
                case "back":
                case "next":
                    $this->callView("knjl053qForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl053qCtl = new knjl053qController;
?>
