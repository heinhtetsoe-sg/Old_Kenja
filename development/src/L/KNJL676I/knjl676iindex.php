<?php

require_once('for_php7.php');

require_once('knjl676iModel.inc');
require_once('knjl676iQuery.inc');

class knjl676iController extends Controller
{
    public $ModelClassName = "knjl676iModel";
    public $ProgramID      = "KNJL676I";

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
                    $this->callView("knjl676iForm1");
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
$knjl676iCtl = new knjl676iController();
