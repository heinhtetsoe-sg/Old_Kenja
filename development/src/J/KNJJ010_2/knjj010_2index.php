<?php

require_once('for_php7.php');

require_once('knjj010_2Model.inc');
require_once('knjj010_2Query.inc');
echo $y;
class knjj010_2Controller extends Controller {
    var $ModelClassName = "knjj010_2Model";
    var $ProgramID      = "KNJJ010";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":                
                    $sessionInstance->setAccessLogDetail("S", "KNJJ010_2");
                    $this->callView("knjj010_2Form2");
                    break 2;
                case "add":                   
                    $sessionInstance->setAccessLogDetail("I", "KNJJ010_2");
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJJ010_2");
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":				
                    $sessionInstance->setAccessLogDetail("S", "KNJJ010_2");
                    $this->callView("knjj010_2Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJJ010_2");
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "subform1":
                case "subform1_clear":
                    $sessionInstance->setAccessLogDetail("S", "KNJJ010_2");
                    $this->callView("knjj010_2SubForm1");
                    break 2;
                case "subform1_update":
                    $sessionInstance->setAccessLogDetail("U", "KNJJ010_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateSubModel1();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjj010_2index.php?cmd=list";
                    $args["right_src"] = "knjj010_2index.php?cmd=edit";
                    $args["cols"] = "55%,*%";
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
$knjj010_2Ctl = new knjj010_2Controller;
//var_dump($_REQUEST);
?>
