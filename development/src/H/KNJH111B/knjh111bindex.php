<?php

require_once('for_php7.php');

require_once('knjh111bModel.inc');
require_once('knjh111bQuery.inc');

class knjh111bController extends Controller {
    var $ModelClassName = "knjh111bModel";
    var $ProgramID      = "KNJH111B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                case "qualifiedCd":
                case "conditionDiv":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh111bForm2");
                    break 2;
                case "right_list":
                case "list":
                    $this->callView("knjh111bForm1");
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
                case "replace1":
                case "replace_qualifiedCd":
                case "replace_conditionDiv":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh111bSubForm1");
                    break 2;
                case "replace_update1":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->ReplaceModel1();
                    $sessionInstance->setCmd("replace1");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH111B/knjh111bindex.php?cmd=right_list") ."&button=1";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knjh111bindex.php?cmd=right_list";
                    $args["edit_src"]  = "knjh111bindex.php?cmd=edit";
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
$knjh090Ctl = new knjh111bController;
?>
