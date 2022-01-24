<?php

require_once('for_php7.php');

require_once('knjl111nModel.inc');
require_once('knjl111nQuery.inc');

class knjl111nController extends Controller {
    var $ModelClassName = "knjl111nModel";
    var $ProgramID      = "KNJL111N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjl111nForm1");
                    break 2;
                case "recalc":
                    $this->callView("knjl111nForm1");
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
                    $sessionInstance->setCmd("main");
                    break 1;
                case "uploadCsv":
                    $sessionInstance->getUploadCsvModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "downloadError":
                case "downloadCsv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl111nForm1");
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
$knjl111nCtl = new knjl111nController;
//var_dump($_REQUEST);
?>
