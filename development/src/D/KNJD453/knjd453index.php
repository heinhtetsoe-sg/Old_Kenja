<?php

require_once('for_php7.php');
require_once('knjd453Model.inc');
require_once('knjd453Query.inc');

class knjd453Controller extends Controller {
    var $ModelClassName = "knjd453Model";
    var $ProgramID      = "KNJD453";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjd453Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd453Form1");
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $this->callView("knjd453Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = REQUESTROOT ."/X/KNJXEXP_GUI/index.php?PROGRAMID=" .$this->ProgramID ."&URL_SCHOOLKIND={$sessionInstance->urlSchoolKind}&TARGET=right_frame&PATH=" .urlencode("/D/KNJD453/knjd453index.php?cmd=edit") ."&button=3";
                    $args["right_src"] = "knjd453index.php?cmd=edit&init=1";
                    $args["cols"] = "23%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd453Ctl = new knjd453Controller;
//var_dump($_REQUEST);
?>
