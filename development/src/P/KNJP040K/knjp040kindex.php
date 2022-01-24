<?php

require_once('for_php7.php');

require_once('knjp040kModel.inc');
require_once('knjp040kQuery.inc');

class knjp040kController extends Controller {
    var $ModelClassName = "knjp040kModel";
    var $ProgramID      = "knjp040k";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjp040kForm2");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "send":
                    $sessionInstance->getSendModel();
                    return;
                case "list":
                    $this->callView("knjp040kForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "right":
#NO001
#                    $args["right_src"] = "knjp040kindex.php?cmd=list";
#                    $args["edit_src"] = "knjp040kindex.php?cmd=edit";
#                    $args["rows"] = "35%,65%";
#                    View::frame($args,"frame3.html");
                    $this->callView("knjp040kForm2");
                    break 2;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/P/KNJP040K/knjp040kindex.php?cmd=right") ."&button=1";

                    $args["left_src"] = REQUESTROOT ."/X/KNJXSEARCH_BANK/knjxsearch_bankindex.php" .$search;
//                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXPK/index.php" .$search;
#NO001
#                    $args["right_src"] = "knjp040kindex.php?cmd=right";
                    $args["right_src"] = "knjp040kindex.php?cmd=edit";
                    $args["cols"] = "30%,*";
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
$knjp040kCtl = new knjp040kController;
//var_dump($_REQUEST);
?>
