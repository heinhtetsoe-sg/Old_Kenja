<?php

require_once('for_php7.php');
require_once('knjh150aModel.inc');
require_once('knjh150aQuery.inc');

class knjh150aController extends Controller {
    var $ModelClassName = "knjh150aModel";
    var $ProgramID      = "KNJH150A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit": 
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh150aForm2");
                    break 2;
                case "replace":
                    $this->callView("knjh150aSubForm1");
                    break 2;
                case "replace_update":
                    $sessionInstance->ReplaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "update":
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
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/H/KNJH150A/knjh150aindex.php?cmd=edit") ."&button=1";
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knjh150aindex.php?cmd=edit";
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
$knjh150aCtl = new knjh150aController;
?>
