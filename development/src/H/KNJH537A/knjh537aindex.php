<?php

require_once('for_php7.php');

require_once('knjh537aModel.inc');
require_once('knjh537aQuery.inc');

class knjh537aController extends Controller {
    var $ModelClassName = "knjh537aModel";
    var $ProgramID      = "KNJH537A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "sel":
                case "change":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh537aForm2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "list":
                case "list_update":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh537aForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "knjh537aindex.php?cmd=list";
                    $args["right_src"]  = "knjh537aindex.php?cmd=sel";
                    $args["cols"] = "45%,*";
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
$knjh537aCtl = new knjh537aController;
?>
