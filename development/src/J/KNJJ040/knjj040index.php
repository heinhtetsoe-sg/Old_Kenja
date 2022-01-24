<?php

require_once('for_php7.php');

require_once('knjj040Model.inc');
require_once('knjj040Query.inc');

class knjj040Controller extends Controller {
    var $ModelClassName = "knjj040Model";
    var $ProgramID      = "KNJJ040";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            $sessionInstance->knjj040Model();        //コントロールマスタの呼び出し
            switch (trim($sessionInstance->cmd)) {
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->schreg_chk($sessionInstance->field["SCHREGNO"]);
                case "edit":                
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj040Form2");
                    break 2;
                case "add":                    
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "clubchange":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj040Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "subform1":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj040SubForm1");
                    break 2;
                case "sub_insert":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getSubInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "subform2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj040SubForm2");
                    break 2;
                case "sub_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getSubUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform2");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjj040index.php?cmd=list";
                    $args["right_src"] = "knjj040index.php?cmd=edit";
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
$knjj040Ctl = new knjj040Controller;
//var_dump($_REQUEST);
?>
