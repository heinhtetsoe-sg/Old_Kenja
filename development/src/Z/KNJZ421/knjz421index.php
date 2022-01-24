<?php

require_once('for_php7.php');

require_once('knjz421Model.inc');
require_once('knjz421Query.inc');

class knjz421Controller extends Controller
{
    public $ModelClassName = "knjz421Model";
    public $ProgramID      = "KNJZ421";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "search":
                    $sessionInstance->chkCollegeOrCompanyMst($sessionInstance->field["COMPANY_CD"]);
                    // no break
                case "edit":
                case "reset":
                case "chenge_cd":
                case "apply_jobtype":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz421Form2");
                    break 2;
                case "execute":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "pdf":     //PDFダウンロード
                    $sessionInstance->setAccessLogDetail("P", $ProgramID);
                    if (!$sessionInstance->getPdfModel()) {
                        $this->callView("knjz421Form2");
                    }
                    break 2;
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
                        $this->callView("knjz421Form2");
                    }
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
                case "list":
                case "change_year":
                    $this->callView("knjz421Form1");
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
                    $args["left_src"] = "knjz421index.php?cmd=list";
                    $args["right_src"] = "knjz421index.php?cmd=edit";
                    $args["cols"] = "30%,70%";
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
$knjz421Ctl = new knjz421Controller();
