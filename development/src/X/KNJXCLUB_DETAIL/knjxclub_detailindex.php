<?php

require_once('for_php7.php');

require_once('knjxclub_detailModel.inc');
require_once('knjxclub_detailQuery.inc');

class knjxclub_detailController extends Controller {
    var $ModelClassName = "knjxclub_detailModel";
    var $ProgramID      = "KNJXCLUB_DETAIL";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                    $this->callView("knjxclub_detailForm2");
                    break 2;
                case "add":
                case "update":
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "clear":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjxclub_detailForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "viewpdf":
                    if (!$sessionInstance->getPdfModel()){
                        $this->callView("knjxclub_detailForm2");
                    }
                    break 2;
                case "delpdf":
                    $sessionInstance->deletePdfModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjxclub_detailindex.php?cmd=list";
                    $args["right_src"] = "knjxclub_detailindex.php?cmd=edit";
                    $args["cols"] = "43%,58%";
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
$knjxclub_detailCtl = new knjxclub_detailController;
?>
