<?php

require_once('for_php7.php');
require_once('knjh160Model.inc');
require_once('knjh160Query.inc');

class knjh160Controller extends Controller {
    var $ModelClassName = "knjh160Model";
    var $ProgramID      = "KNJH160";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit": 
                    $this->callView("knjh160Form2");
                    break 2;
                case "replace":
                    $this->callView("knjh160SubForm1");
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->ReplaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
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
                    //NO001
                    $search = "?PROGRAMID=" .$this->ProgramID ."&AUTH=".AUTHORITY."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH160/knjh160index.php?cmd=edit") ."&button=1";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP3/index.php" .$search;
                    $args["right_src"] = "knjh160index.php?cmd=edit";
                    $args["cols"] = "25%,*%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh160Ctl = new knjh160Controller;
?>
