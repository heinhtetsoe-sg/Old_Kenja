<?php

require_once('for_php7.php');

require_once('knjz404jModel.inc');
require_once('knjz404jQuery.inc');

class knjz404jController extends Controller {
    var $ModelClassName = "knjz404jModel";
    var $ProgramID      = "KNJZ404J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $this->callView("knjz404jForm2");
                    break 2;
                case "insert":  //追加
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":  //コピー
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getInsertCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "sort":
                case "list":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjz404jForm1");
                    break 2;
                case "delete":  //削除
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
                    $args["left_src"] = "knjz404jindex.php?cmd=list";
                    $args["right_src"] = "knjz404jindex.php?cmd=edit";
                    $args["cols"] = "52%,48%";
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
$knjz404jCtl = new knjz404jController;
//var_dump($_REQUEST);
?>
