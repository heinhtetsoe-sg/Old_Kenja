<?php

require_once('for_php7.php');

require_once('knjm251wModel.inc');
require_once('knjm251wQuery.inc');

class knjm251wController extends Controller {
    var $ModelClassName = "knjm251wModel";
    var $ProgramID      = "KNJM251W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "sel":
                    $this->callView("knjm251wForm2");
                    break 2;
                case "update":
                case "check":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "list":
                    $this->callView("knjm251wForm1");
                    break 2;
                case "clear":
                case "list2":
                    $this->callView("knjm251wForm2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjm251windex.php?cmd=list";
                    $args["right_src"] = "knjm251windex.php?cmd=sel";
                    $args["cols"] = "50%,*";
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
$knjm251wCtl = new knjm251wController;
//var_dump($_REQUEST);
?>
