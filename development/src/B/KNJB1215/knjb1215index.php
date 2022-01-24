<?php

require_once('for_php7.php');

require_once('knjb1215Model.inc');
require_once('knjb1215Query.inc');

class knjb1215Controller extends Controller {
    var $ModelClassName = "knjb1215Model";
    var $ProgramID      = "KNJB1215";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjb1215Form2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "change":
                    $this->callView("knjb1215Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyYearModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjb1215index.php?cmd=list";
                    $args["right_src"] = "knjb1215index.php?cmd=edit";
                    $args["cols"] = "42%,58%";
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
$knjb1215Ctl = new knjb1215Controller;
?>
