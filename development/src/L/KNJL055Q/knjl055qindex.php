<?php

require_once('for_php7.php');

require_once('knjl055qModel.inc');
require_once('knjl055qQuery.inc');

class knjl055qController extends Controller
{
    public $ModelClassName = "knjl055qModel";
    public $ProgramID      = "KNJL055Q";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "mainH":
                case "end":
                    $this->callView("knjl055qForm1");
                    break 2;
                case "update_H":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("mainH");
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
$knjl055qCtl = new knjl055qController;
?>
