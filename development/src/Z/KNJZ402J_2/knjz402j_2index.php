<?php

require_once('for_php7.php');

require_once('knjz402j_2Model.inc');
require_once('knjz402j_2Query.inc');

class knjz402j_2Controller extends Controller {
    var $ModelClassName = "knjz402j_2Model";
    var $ProgramID      = "KNJZ402J";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "class":
                case "reset":
                    $this->callView("knjz402j_2Form2");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "changeCmb":
                    $this->callView("knjz402j_2Form1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz402j_2index.php?cmd=list&year_code=".VARS::request("year_code");
                    $args["right_src"] = "knjz402j_2index.php?cmd=edit&year_code=".VARS::request("year_code");
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
$knjz402j_2Ctl = new knjz402j_2Controller;
//var_dump($_REQUEST);
?>
