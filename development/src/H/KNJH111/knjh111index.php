<?php

require_once('for_php7.php');
require_once('knjh111Model.inc');
require_once('knjh111Query.inc');

class knjh111Controller extends Controller {
    var $ModelClassName = "knjh111Model";
    var $ProgramID      = "KNJH111";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                case "contedit":
                    $this->callView("knjh111Form2");
                    break 2;
                case "right_list":
                case "list":
                    $this->callView("knjh111Form1");
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
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH111/knjh111index.php?cmd=right_list") ."&button=1";
                    $args["right_src"] = "knjh111index.php?cmd=right_list";
                    $args["edit_src"]  = "knjh111index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
                    $args["rows"] = "45%,55%";
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
$knjh090Ctl = new knjh111Controller;
?>
