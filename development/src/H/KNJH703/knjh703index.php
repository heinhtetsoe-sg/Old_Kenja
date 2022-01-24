<?php

require_once('for_php7.php');

require_once('knjh703Model.inc');
require_once('knjh703Query.inc');

class knjh703Controller extends Controller
{
    public $ModelClassName = "knjh703Model";
    public $ProgramID      = "KNJH703";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "bunri":
                case "reset":
                    $this->callView("knjh703Form1");
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "replace_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->replaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "replace":
                case "replace_bunri":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh703SubForm1");
                    break 2;
                case "back":
                    $this->callView("knjh703Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh703Ctl = new knjh703Controller();
