<?php

require_once('for_php7.php');

require_once('knjf307Model.inc');
require_once('knjf307Query.inc');

class knjf307Controller extends Controller {
    var $ModelClassName = "knjf307Model";
    var $ProgramID      = "KNJF307";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "fixed":
                    $this->callView("knjf307Form1");
                    break 2;
                case "fixedLoad":
                    $this->callView("knjf307fixedForm1");
                    break 2;
                case "houkoku":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateEdboardModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    if ($sessionInstance->getUpdateModel()) {
                        $sessionInstance->setCmd("fixed");
                    } else {
                        $sessionInstance->setCmd("main");
                    }
                    break 1;
                case "fixedUpd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getFixedUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjf307Form1");
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
$knjf307Ctl = new knjf307Controller;
//var_dump($_REQUEST);
?>
