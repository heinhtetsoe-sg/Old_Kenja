<?php

require_once('for_php7.php');

require_once('knjh400_everyModel.inc');
require_once('knjh400_everyQuery.inc');

class knjh400_everyController extends Controller {
    var $ModelClassName = "knjh400_everyModel";
    var $ProgramID      = "KNJH400_EVERY";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "choice":  //左でデータを選択したとき
                case "new":     //新規
                case "copy":    //コピー
                case "add_comp":    //追加後
                case "up_comp":     //更新後
                case "del_comp":    //削除後
                    $this->callView("knjh400_everyForm2");
                    break 2;
                case "add":     //追加
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("add_comp");
                    break 1;
                case "delete":  //削除
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("del_comp");
                    break 1;
                case "update":  //更新
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("up_comp");
                    break 1;
                case "list":
                case "search":
                case "clear":
                    $this->callView("knjh400_everyForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjh400_everyindex.php?cmd=list";
                    $args["right_src"] = "knjh400_everyindex.php?cmd=edit";
                    $args["cols"] = "55%,45%";
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
$knjh400_everyCtl = new knjh400_everyController;
//var_dump($_REQUEST);
?>
