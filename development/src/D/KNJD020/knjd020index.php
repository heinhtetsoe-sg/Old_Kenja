<?php

require_once('for_php7.php');

require_once('knjd020Model.inc');
require_once('knjd020Query.inc');

class knjd020Controller extends Controller {
    var $ModelClassName = "knjd020Model";
    var $ProgramID      = "KNJD020";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjd020Form1");
                   break 2;
                case "clear":
                case "edit":
                case "change":
                    $this->callView("knjd020Form2");
                    break 2;
                //追加、更新時は一度実施日をチェックする
                case "add_check":
                case "update_check":
                    $sessionInstance->getTermCheckModel($sessionInstance->cmd);
                    $this->callView("knjd020Form2");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                //削除前処理
                case "delete_check":
                    $sessionInstance->getDeleteCheckModel();
                    $this->callView("knjd020Form2");
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
                    //学習記録ｴｸｽﾌﾟﾛｰﾗ
                    $args["left_src"] = REQUESTROOT ."/X/KNJXGTRE/index.php?cmd=left&PROGRAMID=" .$this->ProgramID;
                    $args["right_src"] = "knjd020index.php?cmd=main";
                    $args["edit_src"]  = "knjd020index.php?cmd=edit";
                    $args["cols"] = "30%,70%";
                    $args["rows"] = "50%,50%";
                    View::frame($args,"frame2.html");
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjd020Ctl = new knjd020Controller;
?>
