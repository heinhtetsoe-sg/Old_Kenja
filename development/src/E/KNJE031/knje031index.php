<?php

require_once('for_php7.php');

require_once('knje031Model.inc');
require_once('knje031Query.inc');

class knje031Controller extends Controller {
    var $ModelClassName = "knje031Model";
    var $ProgramID      = "KNJE031";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "right_list":
                case "from_list":
                case "from_edit":
                    $this->callView("knje031Form1");
                    break 2;
                case "edit":
                case "edit2":
                case "updEdit":
                case "from_right":
                case "reset":
                    $this->callView("knje031Form2");
                    break 2;
                case "list":
                    $this->callView("knje031Form3");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID); 
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updEdit");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID); 
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit2");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knje031Model();
                    
                    //分割フレーム作成
                    $args["left_src"]   = "knje031index.php?cmd=list";
                    $args["right_src"]  = "knje031index.php?cmd=right_list";
                    $args["edit_src"]   = "knje031index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
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
$knje031Ctl = new knje031Controller;
?>
