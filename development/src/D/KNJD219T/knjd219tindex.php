<?php

require_once('for_php7.php');

require_once('knjd219tModel.inc');
require_once('knjd219tQuery.inc');

class knjd219tController extends Controller {
    var $ModelClassName = "knjd219tModel";
    var $ProgramID      = "KNJD219T";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                case "chenge_cd":
                    $this->callView("knjd219tForm2");
                    break 2;
                case "list":
                case "knjd219t":
                case "up_list":
                    $this->callView("knjd219tForm1");
                    break 2;
                case "delete":
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "csv":       //CSV出力
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd219tForm2");
                    }
                    break 2;
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjd219tindex.php?cmd=list";
                    $args["right_src"] = "knjd219tindex.php?cmd=edit";
                    $args["cols"] = "54%,46%";
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
$knjd219tCtl = new knjd219tController;
//var_dump($_REQUEST);
?>
