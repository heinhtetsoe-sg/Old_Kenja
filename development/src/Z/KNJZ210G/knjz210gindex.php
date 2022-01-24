<?php

require_once('for_php7.php');

require_once('knjz210gModel.inc');
require_once('knjz210gQuery.inc');

class knjz210gController extends Controller {
    var $ModelClassName = "knjz210gModel";
    var $ProgramID      = "KNJZ210G";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "main":
                case "chenge_cd":
                    $this->callView("knjz210gForm2");
                    break 2;
                case "list":
                    $this->callView("knjz210gForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("chenge_cd");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz210gindex.php?cmd=list";
                    $args["right_src"] = "knjz210gindex.php?cmd=edit";
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
$knjz210gCtl = new knjz210gController;
//var_dump($_REQUEST);
?>
