<?php

require_once('for_php7.php');

require_once('knjz050_3Model.inc');
require_once('knjz050_3Query.inc');

class knjz050_3Controller extends Controller {
    var $ModelClassName = "knjz050_3Model";
    var $ProgramID      = "KNJZ050";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ050_3");
                    $this->callView("knjz050_3Form2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ050_3");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":                
                    $this->callView("knjz050_3Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz050_3index.php?cmd=list";
                    $args["right_src"] = "knjz050_3index.php?cmd=edit";
                    $args["cols"] = "55%,*";
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
$knjz050_3Ctl = new knjz050_3Controller;
//var_dump($_REQUEST);
?>
