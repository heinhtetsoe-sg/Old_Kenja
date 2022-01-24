<?php

require_once('for_php7.php');

require_once('knjf301Model.inc');
require_once('knjf301Query.inc');

class knjf301Controller extends Controller {
    var $ModelClassName = "knjf301Model";
    var $ProgramID      = "KNJF301";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "fixed":
                    $this->callView("knjf301Form1");
                    break 2;
                case "fixedLoad":
                    $this->callView("knjf301fixedForm1");
                    break 2;
                case "recalc":
                    $this->callView("knjf301Form1");
                    break 2;
                case "houkoku":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateEdboardModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("fixed");
                    break 1;
                case "fixedUpd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getFixedUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf301Form1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                case "read":
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
$knjf301Ctl = new knjf301Controller;
//var_dump($_REQUEST);
?>
