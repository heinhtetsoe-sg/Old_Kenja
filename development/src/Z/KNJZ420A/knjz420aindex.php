<?php

require_once('for_php7.php');

require_once('knjz420aModel.inc');
require_once('knjz420aQuery.inc');

class knjz420aController extends Controller
{
    public $ModelClassName = "knjz420aModel";
    public $ProgramID      = "KNJZ420A";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "chenge_cd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz420aForm2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "uploadCsv":       //CSV取込
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUploadCsvModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "downloadHead":    //CSVヘッダ出力
                case "downloadCsv":     //CSV書出
                case "downloadError":   //CSVエラー出力
                    $this->checkAuth(DEF_UPDATABLE);
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjz420aForm2");
                    }
                    break 2;
                case "list":
                    $this->callView("knjz420aForm1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz420aindex.php?cmd=list";
                    $args["right_src"] = "knjz420aindex.php?cmd=edit";
                    $args["cols"] = "50%,50%";
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
$knjz420aCtl = new knjz420aController();
