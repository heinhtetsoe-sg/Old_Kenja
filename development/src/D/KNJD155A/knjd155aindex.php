<?php

require_once('for_php7.php');

require_once('knjd155aModel.inc');
require_once('knjd155aQuery.inc');

class knjd155aController extends Controller
{
    public $ModelClassName = "knjd155aModel";
    public $ProgramID      = "KNJD155A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "reset":
                case "back":
                    $this->callView("knjd155aForm1");
                    break 2;
                case "main":
                    //$sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd155aForm1");
                    break 2;
                case "update":
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "replace":
                    $this->callView("knjd155aSubForm1");
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    //$sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getReplaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
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
$knjd155aCtl = new knjd155aController;
