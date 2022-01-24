<?php

require_once('for_php7.php');

require_once('knjz068bModel.inc');
require_once('knjz068bQuery.inc');

class knjz068bController extends Controller
{
    public $ModelClassName = "knjz068bModel";
    public $ProgramID      = "KNJZ068B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "reset":
                case "ibseq":
                case "check":
                case "chgYear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz068bForm2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjz068bForm1");
                    break 2;
                case "copy":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "downloadCsv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjz068cForm1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knjz068bindex.php?cmd=list";
                    $args["right_src"] = "knjz068bindex.php?cmd=edit";
                    $args["cols"] = "46%, 54%";
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
$knjz068bCtl = new knjz068bController();
