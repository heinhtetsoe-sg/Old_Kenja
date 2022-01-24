<?php

require_once('for_php7.php');

require_once('knje434Model.inc');
require_once('knje434Query.inc');

class knje434Controller extends Controller {
    var $ModelClassName = "knje434Model";
    var $ProgramID      = "KNJE434";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knje434Model();
                    $this->callView("knje434Form1");
                    exit;
                case "knje434":
                case "fixed":
                    $this->callView("knje434Form1");
                    break 2;
                case "fixedLoad":
                    $this->callView("knje434fixedForm1");
                    break 2;
                case "houkoku":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateEdboardModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knje434");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("fixed");
                    break 1;
                case "fixedUpd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getFixedUpdateModel();
                    $sessionInstance->setCmd("knje434");
                    break 1;
                case "houkoku":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateEdboardModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje434Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje434Ctl = new knje434Controller;
?>
