<?php

require_once('for_php7.php');

require_once('knjz233mModel.inc');
require_once('knjz233mQuery.inc');

class knjz233mController extends Controller {
    var $ModelClassName = "knjz233mModel";
    var $ProgramID      = "KNJZ233M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "sel":
                    $this->callView("knjz233mForm2");
                    break 2;
                case "update":
                case "check":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "list":
                    $this->callView("knjz233mForm1");
                    break 2;
                case "clear":
                case "list2":
                    $this->callView("knjz233mForm2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz233mindex.php?cmd=list";
                    $args["right_src"] = "knjz233mindex.php?cmd=sel";
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
$knjz233mCtl = new knjz233mController;
//var_dump($_REQUEST);
?>
