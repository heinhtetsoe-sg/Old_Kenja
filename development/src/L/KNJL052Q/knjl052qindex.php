<?php

require_once('for_php7.php');

require_once('knjl052qModel.inc');
require_once('knjl052qQuery.inc');

class knjl052qController extends Controller
{
    public $ModelClassName = "knjl052qModel";
    public $ProgramID      = "KNJL052Q";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "mainH":
                case "reset":
                case "end":
                    $this->callView("knjl052qForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjl052qCtl = new knjl052qController;
?>
