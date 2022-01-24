<?php

require_once('for_php7.php');

require_once('knje443Model.inc');
require_once('knje443Query.inc');

class knje443Controller extends Controller {
    var $ModelClassName = "knje443Model";
    var $ProgramID      = "KNJE443";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knje443Model();
                    $this->callView("knje443Form1");
                    exit;
                case "knje443":
                case "fixed":
                    $this->callView("knje443Form1");
                    break 2;
                case "fixedLoad":
                    $this->callView("knje443fixedForm1");
                    break 2;
                case "houkoku":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateEdboardModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knje443");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("fixed");
                    break 1;
                case "fixedUpd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getFixedUpdateModel();
                    $sessionInstance->setCmd("knje443");
                    break 1;
                case "houkoku":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateEdboardModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje443Form1");
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
$knje443Ctl = new knje443Controller;
?>
