<?php

require_once('for_php7.php');

require_once('knjb030Model.inc');
require_once('knjb030Query.inc');

class knjb030Controller extends Controller {
    var $ModelClassName = "knjb030Model";
    var $ProgramID      = "KNJB030";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "group":
                case "edit":
                case "reset":
                    $this->callView("knjb030Form2");
                    break 2;
                case "subform1": //受講クラス
                    $this->callView("knjb030SubForm1");
                    break 2;
                case "subform2": //科目担任
                    $this->callView("knjb030SubForm2");
                    break 2;
                case "subform3": //使用施設
                    $this->callView("knjb030SubForm3");
                    break 2;
                case "subform4": //教科書
                    $this->callView("knjb030SubForm4");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list":
                    $this->callView("knjb030Form1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjb030index.php?cmd=list";
                    $args["right_src"] = "knjb030index.php?cmd=edit";
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
$knjb030Ctl = new knjb030Controller;
//var_dump($_REQUEST);
?>
