<?php

require_once('for_php7.php');

require_once('knja070Model.inc');
require_once('knja070Query.inc');

class knja070Controller extends Controller
{
    public $ModelClassName = "knja070Model";
    public $ProgramID      = "KNJA070";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $this->callView("knja070Form2");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "uploadCsv":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $sessionInstance->getUploadCsvModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "downloadHead":
                case "downloadCsv":
                case "downloadError":
                    $this->checkAuth(DEF_UPDATABLE);
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knja070Form2");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getInsertCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knja070Form1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knja070index.php?cmd=list";
                    $args["right_src"] = "knja070index.php?cmd=edit";
                    $args["cols"] = "55%,45%";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja070Ctl = new knja070Controller();
