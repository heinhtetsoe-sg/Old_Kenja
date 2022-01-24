<?php

require_once('for_php7.php');

require_once('knjf302Model.inc');
require_once('knjf302Query.inc');

class knjf302Controller extends Controller {
    var $ModelClassName = "knjf302Model";
    var $ProgramID      = "KNJF302";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "houkokuPrint":
                case "reset":
                case "syuukei":
                case "fixed":
                    $this->callView("knjf302Form1");
                    break 2;
                case "fixedLoad":
                    $this->callView("knjf302fixedForm1");
                    break 2;
                case "recalc":
                    $this->callView("knjf302Form1");
                    break 2;
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
                        $this->callView("knjf302Form1");
                    }
                    break 2;
                case "houkoku":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateEdboardModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("houkokuPrint");
                    break 1;
                case "changeYear":
                case "changeDate":
                case "read_before":
                case "read_next":
                    $sessionInstance->key_Move_Model();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjf302Ctl = new knjf302Controller;
//var_dump($_REQUEST);
?>
