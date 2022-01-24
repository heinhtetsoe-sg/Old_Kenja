<?php

require_once('for_php7.php');
require_once('knjh070Model.inc');
require_once('knjh070Query.inc');

class knjh070Controller extends Controller {
    var $ModelClassName = "knjh070Model";
    var $ProgramID      = "KNJH070";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh070Form2");
                    break 2;
                case "right_list":
                case "list":
                    $this->callView("knjh070Form1");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
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
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH070/knjh070index.php?cmd=right") ."&button=1";

                    $args["right_src"] = "knjh070index.php?cmd=right";
                    $args["cols"] = "30%,70%";
                    View::frame($args);
                    return;
                case "right":
                    $args["right_src"] = "knjh070index.php?cmd=right_list";
                    $args["edit_src"]  = "knjh070index.php?cmd=edit";
                    $args["rows"] = "40%,60%";
                    View::frame($args,"frame3.html");

/*                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH070/knjh070index.php?cmd=right_list") ."&button=1";
                    $args["right_src"] = "knjh070index.php?cmd=right_list";
                    $args["edit_src"]  = "knjh070index.php?cmd=edit";
                    $args["cols"] = "30%,70%";
                    $args["rows"] = "50%,50%";
                    View::frame($args,"frame2.html");
*/                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh070Ctl = new knjh070Controller;
?>
