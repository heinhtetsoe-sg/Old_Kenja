<?php

require_once('for_php7.php');

require_once('knjz402j_3Model.inc');
require_once('knjz402j_3Query.inc');

class knjz402j_3Controller extends Controller {
    var $ModelClassName = "knjz402j_3Model";
    var $ProgramID      = "KNJZ402J";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "class":
                case "reset":
                    $this->callView("knjz402j_3Form2");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz402j_3Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz402j_3index.php?cmd=list&year_code=".VARS::request("year_code");
                    $args["right_src"] = "knjz402j_3index.php?cmd=edit&year_code=".VARS::request("year_code");
                    $args["cols"] = "40%,60%";
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
$knjz402j_3Ctl = new knjz402j_3Controller;
//var_dump($_REQUEST);
?>
