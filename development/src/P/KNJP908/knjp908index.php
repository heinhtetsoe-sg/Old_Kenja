<?php

require_once('for_php7.php');

require_once('knjp908Model.inc');
require_once('knjp908Query.inc');

class knjp908Controller extends Controller {
    var $ModelClassName = "knjp908Model";
    var $ProgramID      = "KNJP908";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $this->callView("knjp908Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "sendBank":
                case "send":
                    $sessionInstance->getSendModel();
                    return;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "right":
                    $this->callView("knjp908Form1");
                    break 2;
                case "":
                    //分割フレーム作成
                    $search = "?PROGRAMID=" .$this->ProgramID ."&TARGET=right_frame&PATH=" .urlencode("/P/KNJP908/knjp908index.php?cmd=edit") ."&button=1&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&URL_SCHOOLCD={$sessionInstance->urlSchoolCd}&MN_ID={$sessionInstance->mnId}";

                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP/index.php" .$search;
                    $args["right_src"] = "knjp908index.php?cmd=edit";
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
$knjp908Ctl = new knjp908Controller;
//var_dump($_REQUEST);
?>
