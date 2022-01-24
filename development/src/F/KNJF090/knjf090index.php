<?php

require_once('for_php7.php');
require_once('knjf090Model.inc');
require_once('knjf090Query.inc');

class knjf090Controller extends Controller {
    var $ModelClassName = "knjf090Model";
    var $ProgramID      = "knjf090";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "list":
                    $this->callView("list");
                    break 2;
                case "edit":
                case "chg_div":
                case "reset":
                    $this->callView("knjf090Form2");
                    break 2;
                case "main":
                    $this->callView("knjf090Form1");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    $this->callView("knjf090Form2");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjf090Form2");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $this->callView("knjf090Form2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/F/KNJF090/knjf090index.php?cmd=main") ."&button=1";
                    $args["right_src"] = "knjf090index.php?cmd=main";
                    $args["edit_src"] = "knjf090index.php?cmd=edit";
                    $args["cols"] = "25%,*%";
                    $args["rows"] = "30%,*%";
                    View::frame($args, "frame2.html");
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf090Ctl = new knjf090Controller;
?>
