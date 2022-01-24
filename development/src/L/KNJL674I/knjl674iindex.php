<?php

require_once('for_php7.php');

require_once('knjl674iModel.inc');
require_once('knjl674iQuery.inc');

class knjl674iController extends Controller
{
    public $ModelClassName = "knjl674iModel";
    public $ProgramID      = "KNJL674I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear":
                case "edit":
                case "read":
                    $this->callView("knjl674iForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
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
$knjl674iCtl = new knjl674iController();
