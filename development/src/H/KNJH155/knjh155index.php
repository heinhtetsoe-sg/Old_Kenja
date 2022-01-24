<?php

require_once('for_php7.php');
require_once('knjh155Model.inc');
require_once('knjh155Query.inc');

class knjh155Controller extends Controller {
    var $ModelClassName = "knjh155Model";
    var $ProgramID      = "KNJH155";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        if (trim($sessionInstance->prg_first) == "KNJH160"){
            $sessionInstance->setCmd("knjh160");
        }
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                case "edit2":
                    $this->callView("knjh155Form2");
                    break 2;
                case "right_list":
                case "list":
                    $this->callView("knjh155Form1");
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
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "knjh160":
                    $args["right_src"] = "knjh155index.php?cmd=list";
                    $args["edit_src"]  = "knjh155index.php?cmd=edit";
                    $args["rows"] = "50%,50%";
                    View::frame($args,"frame3.html");
                    return;
                case "":

                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH155/knjh155index.php?cmd=right_list") ."&button=1";
                    $args["right_src"] = "knjh155index.php?cmd=right_list";
                    $args["edit_src"]  = "knjh155index.php?cmd=edit";
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
$knjh155Ctl = new knjh155Controller;
?>
