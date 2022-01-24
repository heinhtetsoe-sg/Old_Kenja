<?php

require_once('for_php7.php');

require_once('knje355Model.inc');
require_once('knje355Query.inc');

class knje355Controller extends Controller {
    var $ModelClassName = "knje355Model";
    var $ProgramID      = "KNJE355";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "search":
                    $sessionInstance->chkCollegeOrCompanyMst($sessionInstance->field["COMPANY_CD"]);
                case "edit":
                case "reset":
                case "chenge_cd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje355Form2");
                    break 2;
                case "execute":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "pdf":     //PDFダウンロード
                    $sessionInstance->setAccessLogDetail("P", $ProgramID);
                    if (!$sessionInstance->getPdfModel()){
                        $this->callView("knje355Form2");
                    }
                    break 2;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("copyEdit");
                    break 1;
                case "copyEdit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje355Form1");
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
                    $this->callView("knje355Form1");
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
                    $args["left_src"] = "knje355index.php?cmd=list";
                    $args["right_src"] = "knje355index.php?cmd=edit";
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
$knje355Ctl = new knje355Controller;
//var_dump($_REQUEST);
?>
